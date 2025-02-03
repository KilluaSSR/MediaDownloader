package killua.dev.twitterdownloader.ui.ViewModels

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import db.Download
import db.DownloadStatus
import killua.dev.base.repository.SettingsRepository
import killua.dev.base.ui.BaseViewModel
import killua.dev.base.ui.SnackbarUIEffect
import killua.dev.base.ui.SnackbarUIEffect.*
import killua.dev.twitterdownloader.Model.MainPageUIIntent
import killua.dev.twitterdownloader.Model.MainPageUIState
import killua.dev.twitterdownloader.api.Model.TwitterRequestResult
import killua.dev.twitterdownloader.api.Model.TwitterUser
import killua.dev.twitterdownloader.api.TwitterApiService
import killua.dev.twitterdownloader.download.DownloadManager
import killua.dev.twitterdownloader.repository.DownloadRepository
import killua.dev.base.utils.DownloadEventManager
import killua.dev.base.utils.DownloadPreChecks
import killua.dev.base.utils.generateTwitterVideoFileName
import killua.dev.twitterdownloader.Model.TwitterDownloadItem
import killua.dev.twitterdownloader.download.DownloadQueueManager
import killua.dev.twitterdownloader.download.DownloadTask
import killua.dev.twitterdownloader.utils.NavigateTwitterProfile
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
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
                            println(it)
                            println(user?.name)
                            createAndStartDownload(it, user, tweetId)
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


    private fun createAndStartDownload(videoUrl: String, user: TwitterUser?, tweetID: String) {
        val uuid = UUID.randomUUID().toString()
        val fileName = generateTwitterVideoFileName(user?.screenName)
        try {
            val download = Download(
                uuid = uuid,
                twitterUserId = user?.id,
                twitterScreenName = user?.screenName,
                twitterName = user?.name,
                tweetID = tweetID,
                fileUri = null,
                link = videoUrl,
                fileName = fileName,
                fileType = "video",
                fileSize = 0L,
                status = DownloadStatus.PENDING,
                mimeType = "video/mp4"
            )
            viewModelScope.launch{
                downloadRepository.insert(download)
                downloadQueueManager.enqueue(DownloadTask(uuid, videoUrl, fileName, user?.screenName!!))
            }

        } catch (e: Exception) {
            handleError("Failed", e)
        }
    }



    private fun handleError(message: String, error: Exception) {
        viewModelScope.launch{
            emitEffect(ShowSnackbar("$message: ${error.message ?: "未知错误"}"))
        }
    }
}