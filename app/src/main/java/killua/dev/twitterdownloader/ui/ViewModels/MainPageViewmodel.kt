package killua.dev.twitterdownloader.ui.ViewModels

import android.content.Context
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
import killua.dev.twitterdownloader.api.Model.TwitterRequestResult
import killua.dev.twitterdownloader.api.Model.TwitterUser
import killua.dev.twitterdownloader.api.TwitterApiService
import killua.dev.twitterdownloader.download.DownloadManager
import killua.dev.twitterdownloader.repository.DownloadRepository
import killua.dev.twitterdownloader.utils.NavigateTwitterProfile
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

data class MainPageUIState(
    val youHaveDownloadedSth: Boolean = false,
    val favouriteUserName: String = "",
    val favouriteUserScreenName: String = "",
    val favouriteUserID: String = "",
    val downloadedTimes: Int = 0
) : UIState

sealed class MainPageUIIntent : UIIntent {
    data class ExecuteDownload(val tweetID: String) : MainPageUIIntent()
    data class NavigateToFavouriteUser(val context: Context, val userID: String, val screenName: String) : MainPageUIIntent()
}

@HiltViewModel
class MainPageViewmodel @Inject constructor(
    private val twitterApiService: TwitterApiService,
    private val downloadRepository: DownloadRepository,
    private val downloadManager: DownloadManager,
) : BaseViewModel<MainPageUIIntent, MainPageUIState, SnackbarUIEffect>(
    MainPageUIState()
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
                    handleNewDownload(intent.tweetID)
                }
            }

            is MainPageUIIntent.NavigateToFavouriteUser -> {
                withMainContext {
                    intent.context.NavigateTwitterProfile(intent.userID,intent.screenName)
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private suspend fun handleNewDownload(tweetId: String) =
        withMainContext {
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
            }finally {
                presentFavouriteCardDetails()
            }
        }

    private suspend fun createAndStartDownload(videoUrl: String, user: TwitterUser?, tweetID: String) {
        try {
            val download = Download(
                uuid = UUID.randomUUID().toString(),
                twitterUserId = user?.id,
                twitterScreenName = user?.screenName,
                twitterName = user?.name,
                tweetID = tweetID,
                fileUri = null,
                link = videoUrl,
                fileName = generateFileName(user?.screenName),
                fileType = "video",
                fileSize = 0L,
                status = DownloadStatus.PENDING,
                mimeType = "video/mp4"
            )

            downloadRepository.insert(download)
            downloadManager.enqueueDownload(download)
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
        emitEffect(SnackbarUIEffect.ShowSnackbar("$message: ${error.message ?: "未知错误"}"))
    }
}