package killua.dev.twitterdownloader.ui.ViewModels

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import db.Download
import db.DownloadStatus
import killua.dev.base.Model.AvailablePlatforms
import killua.dev.base.Model.DownloadTask
import killua.dev.base.Model.MediaType
import killua.dev.base.ui.BaseViewModel
import killua.dev.base.ui.SnackbarUIEffect
import killua.dev.base.ui.SnackbarUIEffect.*
import killua.dev.base.ui.UIIntent
import killua.dev.base.ui.UIState
import killua.dev.base.utils.DownloadEventManager
import killua.dev.base.utils.DownloadPreChecks
import killua.dev.base.utils.TwitterMediaFileNameStrategy
import killua.dev.twitterdownloader.Model.NetworkResult
import killua.dev.twitterdownloader.api.Twitter.Model.TwitterUser
import killua.dev.twitterdownloader.api.Twitter.TwitterDownloadSingleMedia
import killua.dev.twitterdownloader.download.DownloadQueueManager
import killua.dev.twitterdownloader.repository.DownloadRepository
import killua.dev.twitterdownloader.utils.NavigateTwitterProfile
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID
import javax.inject.Inject

sealed class MainPageUIIntent : UIIntent {
    data class ExecuteDownload(val url: String) : MainPageUIIntent()
    data class NavigateToFavouriteUser(val context: Context, val userID: String, val screenName: String) : MainPageUIIntent()
}

data class MainPageUIState(
    val youHaveDownloadedSth: Boolean = false,
    val favouriteUserName: String = "",
    val favouriteUserScreenName: String = "",
    val favouriteUserID: String = "",
    val downloadedTimes: Int = 0
) : UIState


@HiltViewModel
class MainPageViewmodel @Inject constructor(
    private val twitterDownloadSingleMedia: TwitterDownloadSingleMedia,
    private val downloadRepository: DownloadRepository,
    private val downloadQueueManager: DownloadQueueManager,
    private val downloadEventManager: DownloadEventManager,
    private val downloadPreChecks: DownloadPreChecks,
) : BaseViewModel<MainPageUIIntent, MainPageUIState, SnackbarUIEffect>(
    MainPageUIState()
) {
    private val mutex = Mutex()
    init {
        launchOnIO {
            observeDownloadCompleted()
            downloadRepository.observeAllDownloads().collect {
                presentFavouriteCardDetails()
            }
        }

    }

    private fun observeDownloadCompleted() {
        viewModelScope.launch {
            downloadEventManager.downloadCompletedFlow.collect{
                presentFavouriteCardDetails()
            }
        }
    }
    suspend fun presentFavouriteCardDetails() {
        val mostDownloaded = downloadRepository.getMostDownloadedUser()
        if (mostDownloaded != null) {
            emitState(uiState.value.copy(
                youHaveDownloadedSth = true,
                favouriteUserID = mostDownloaded.twitterUserId!!,
                favouriteUserName = mostDownloaded.twitterName!!,
                favouriteUserScreenName = mostDownloaded.twitterScreenName!!,
                downloadedTimes = mostDownloaded.totalDownloads
            ))
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override suspend fun onEvent(state: MainPageUIState, intent: MainPageUIIntent) {
        when (intent) {
            is MainPageUIIntent.ExecuteDownload -> {
                mutex.withLock {
                    val platform = classifyLinks(intent.url)
                    when(platform){
                        AvailablePlatforms.Twitter -> {
                            val tweetID = intent.url.split("?")[0].split("/").last()
                            handleNewDownload(tweetID)
                        }
                        AvailablePlatforms.Lofter -> {

                        }
                    }

                }
            }
            is MainPageUIIntent.NavigateToFavouriteUser -> {
                withMainContext {
                    intent.context.NavigateTwitterProfile(intent.userID,intent.screenName)
                }
            }
        }
    }

    private fun classifyLinks(urlLink: String): AvailablePlatforms{
        val patterns: Map<String, AvailablePlatforms> = mapOf(
            "x.com" to AvailablePlatforms.Twitter,
            "twitter.com" to AvailablePlatforms.Twitter,
            ".lofter.com/post/" to AvailablePlatforms.Lofter,
        )
        return patterns.entries.firstOrNull{ (pattern, _) ->
            urlLink.contains(pattern, ignoreCase = true)
        }?.value!!
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun handleNewDownload(tweetId: String) {
        downloadPreChecks.canStartDownload().onSuccess {
            try {
                when (val result = twitterDownloadSingleMedia.getTweetDetailAsync(tweetId)) {
                    is NetworkResult.Success -> {
                        val user = result.data.user
                        result.data.videoUrls.forEach {
                            createAndStartDownload(it, user, tweetId, MediaType.VIDEO)
                        }
                        if(result.data.photoUrls.isNotEmpty()){
                            downloadPreChecks.checkPhotosDownload().onSuccess {
                                result.data.photoUrls.forEach {
                                    createAndStartDownload(it, user, tweetId, MediaType.PHOTO)
                                }
                            }.onFailure { error ->
                                viewModelScope.launch{
                                    emitEffect(ShowSnackbar(error.message.toString(), "OKAY", true))
                                }
                            }
                        }
                    }
                    is NetworkResult.Error -> {
                        viewModelScope.launch{
                            emitEffect(ShowSnackbar("Twitter request error", withDismissAction = true, actionLabel = "OKAY"))
                        }
                    }
                }
            } catch (e: Exception) {
                viewModelScope.launch{
                    emitEffect(
                        ShowSnackbar(
                            e.message ?: "Internal Error"
                        )
                    )
                }
            }
        }.onFailure { error ->
            viewModelScope.launch{
                emitEffect(ShowSnackbar(error.message.toString(), withDismissAction = true, actionLabel = "OKAY"))
            }
        }
    }


    private fun createAndStartDownload(
        url: String,
        user: TwitterUser?,
        tweetID: String,
        mediaType: MediaType
    ) {
        val uuid = UUID.randomUUID().toString()
        val fileNameStrategy = TwitterMediaFileNameStrategy(mediaType)
        val fileName = fileNameStrategy.generate(user?.screenName)

        try {
            val download = Download(
                uuid = uuid,
                userId = user?.id,
                screenName = user?.screenName,
                name = user?.name,
                tweetID = tweetID,
                fileUri = null,
                link = url,
                fileName = fileName,
                fileType = mediaType.name.lowercase(),
                fileSize = 0L,
                status = DownloadStatus.PENDING,
                mimeType = mediaType.mimeType
            )

            viewModelScope.launch {
                downloadRepository.insert(download)
                downloadQueueManager.enqueue(
                    DownloadTask(
                        id = uuid,
                        url = url,
                        fileName = fileName,
                        screenName = user?.screenName ?: "",
                        type = mediaType
                    )
                )
            }
        } catch (e: Exception) {
            handleError("Failed", e)
        }
    }



    private fun handleError(message: String, error: Exception) {
        viewModelScope.launch{
            emitEffect(ShowSnackbar("$message: ${error.message ?: "Internal Error"}", actionLabel =  "OKAY" , withDismissAction = true))
        }
    }
}