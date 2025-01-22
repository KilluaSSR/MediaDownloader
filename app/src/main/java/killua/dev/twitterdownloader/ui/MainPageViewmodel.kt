package killua.dev.twitterdownloader.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import db.Download
import db.DownloadStatus
import killua.dev.base.ui.BaseViewModel
import killua.dev.base.ui.SnackbarUIEffect
import killua.dev.base.ui.UIIntent
import killua.dev.base.ui.UIState
import killua.dev.twitterdownloader.Model.DownloadItem
import killua.dev.twitterdownloader.api.Model.TwitterRequestResult
import killua.dev.twitterdownloader.api.Model.TwitterUser
import killua.dev.twitterdownloader.api.TwitterApiService
import killua.dev.twitterdownloader.download.DownloadManager
import killua.dev.twitterdownloader.repository.DownloadRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

data class DownloadUIState(
    val downloads: List<DownloadItem> = emptyList(),
    val isLoading: Boolean = false,
    val youHaveDownloadedSth: Boolean = false,
    val favouriteUserName: String = "",
    val favouriteUserScreenName: String = "",
    val downloadedTimes: Int = 0
) : UIState

sealed class MainPageUIIntent : UIIntent {
    data class ExecuteDownload(val twitterID: String) : MainPageUIIntent()
}

@HiltViewModel
class MainPageViewmodel @Inject constructor(
    private val twitterApiService: TwitterApiService,
    private val downloadRepository: DownloadRepository,
    private val downloadManager: DownloadManager,
) : BaseViewModel<MainPageUIIntent, DownloadUIState, SnackbarUIEffect>(
    DownloadUIState(isLoading = false)
) {
    private val mutex = Mutex()
    init {
        viewModelScope.launch{
            presentFavouriteCardDetails()
        }
    }
    suspend fun presentFavouriteCardDetails(){
        val mostDownloaded = downloadRepository.getMostDownloadedUser()
        if (mostDownloaded != null){
            emitState(uiState.value.copy(
                youHaveDownloadedSth = true,
                favouriteUserName = mostDownloaded.twitterName!!,
                favouriteUserScreenName = mostDownloaded.twitterScreenName!!,
                downloadedTimes = mostDownloaded.totalDownloads
            ))
        }
    }
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override suspend fun onEvent(state: DownloadUIState, intent: MainPageUIIntent) {
        when (intent) {
            is MainPageUIIntent.ExecuteDownload -> {
                mutex.withLock {
                    handleNewDownload(intent.twitterID)
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private suspend fun handleNewDownload(tweetId: String) =
        withMainContext {
            try {
                emitState(
                    uiState.value.copy(
                        isLoading = true
                    )
                )
                when (val result = twitterApiService.getTweetDetailAsync(tweetId)) {
                    is TwitterRequestResult.Success -> {
                        val user = result.data.user
                        result.data.videoUrls.forEach {
                            println(it)
                            println(user?.name)
                            createAndStartDownload(it, user)
                        }
                    }

                    is TwitterRequestResult.Error -> emitEffect(
                        SnackbarUIEffect.ShowSnackbar(result.message)
                    )
                }
            } catch (e: Exception) {
                emitEffect(
                    SnackbarUIEffect.ShowSnackbar(
                        e.message ?: "Internal Error"
                    )
                )
            } finally {
                emitState(
                    uiState.value.copy(
                        isLoading = false
                    )
                )
            }
        }

    private suspend fun createAndStartDownload(videoUrl: String, user: TwitterUser?) {
        try {
            val download = Download(
                uuid = UUID.randomUUID().toString(),
                twitterUserId = user?.id,
                twitterScreenName = user?.screenName,
                twitterName = user?.name,
                fileUri = null,
                link = videoUrl,
                fileName = generateFileName(user?.screenName),
                fileType = "video/mp4",
                fileSize = 0L,
                status = DownloadStatus.PENDING
            )

            downloadRepository.insert(download)
            downloadManager.enqueueDownload(download)

            val downloads =
                uiState.value.downloads + DownloadItem.fromDownload(
                    download
                )
            emitState(
                uiState.value.copy(
                    downloads = downloads
                )
            )
        } catch (e: Exception) {
            handleError("Failed", e)
        }
    }

    private fun generateFileName(screenName: String?): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            .format(Date())
        return "${screenName ?: "video"}_${timestamp}.mp4"
    }

    private suspend fun handleError(message: String, error: Exception) {
        emitState(
            uiState.value.copy(
                isLoading = false
            )
        )
        emitEffect(SnackbarUIEffect.ShowSnackbar("$message: ${error.message ?: "未知错误"}"))
    }
}