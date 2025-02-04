package killua.dev.twitterdownloader.ui.ViewModels

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.viewModelScope
import api.Model.TwitterUser
import dagger.hilt.android.lifecycle.HiltViewModel
import db.Download
import db.DownloadStatus
import killua.dev.base.Model.DownloadTask
import killua.dev.base.Model.MediaType
import killua.dev.base.ui.BaseViewModel
import killua.dev.base.ui.SnackbarUIEffect
import killua.dev.base.ui.SnackbarUIEffect.*
import killua.dev.base.utils.DownloadEventManager
import killua.dev.base.utils.DownloadPreChecks
import killua.dev.base.utils.TwitterMediaFileNameStrategy
import killua.dev.twitterdownloader.Model.MainPageUIIntent
import killua.dev.twitterdownloader.Model.MainPageUIState
import killua.dev.twitterdownloader.api.Model.TwitterRequestResult
import killua.dev.twitterdownloader.api.TwitterApiService
import killua.dev.twitterdownloader.download.DownloadQueueManager
import killua.dev.twitterdownloader.repository.DownloadRepository
import killua.dev.twitterdownloader.utils.NavigateTwitterProfile
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID
import javax.inject.Inject


@HiltViewModel
class MainPageViewmodel @Inject constructor(
    private val twitterApiService: TwitterApiService,
    private val downloadRepository: DownloadRepository,
    private val downloadQueueManager: DownloadQueueManager,
    private val downloadEventManager: DownloadEventManager,
    private val downloadPreChecks: DownloadPreChecks
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
            is MainPageUIIntent.ExecuteDownload -> { mutex.withLock { handleNewDownload(intent.tweetID) } }
            is MainPageUIIntent.NavigateToFavouriteUser -> {
                withMainContext {
                    intent.context.NavigateTwitterProfile(intent.userID,intent.screenName)
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun handleNewDownload(tweetId: String) {
        downloadPreChecks.canStartDownload().onSuccess {
            try {
                when (val result = twitterApiService.getTweetDetailAsync(tweetId)) {
                    is TwitterRequestResult.Success -> {
                        val user = result.data.user
                        result.data.videoUrls.forEach {
                            createAndStartDownload(it, user, tweetId, MediaType.VIDEO)
                        }
                        result.data.photoUrls.forEach{

                        }

                    }
                    is TwitterRequestResult.Error -> {
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
                twitterUserId = user?.id,
                twitterScreenName = user?.screenName,
                twitterName = user?.name,
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