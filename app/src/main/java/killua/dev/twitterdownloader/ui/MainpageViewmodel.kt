package killua.dev.twitterdownloader.ui

import Model.DownloadItem
import Model.SnackbarUIEffect
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import api.Model.TwitterRequestResult
import api.Model.TwitterUser
import api.TwitterApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import db.Download
import db.DownloadStatus
import download.DownloadManager
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import repository.DownloadRepository
import ui.BaseViewModel
import ui.UIIntent
import ui.UIState
import java.util.UUID
import javax.inject.Inject

data class DownloadUIState(
    val downloads: List<DownloadItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
) : UIState

sealed class MainpageUIIntent : UIIntent{
    data class ExecuteDownload(val twitterID: String): MainpageUIIntent()
    data class OnResume(val context: Context): MainpageUIIntent()
}

@HiltViewModel
class MainpageViewmodel @Inject constructor(
    private val twitterApiService: TwitterApiService,
    private val downloadRepository: DownloadRepository,
    private val downloadManager: DownloadManager,
) : BaseViewModel<MainpageUIIntent, DownloadUIState, SnackbarUIEffect>(DownloadUIState()) {
    private val mutex = Mutex()
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override suspend fun onEvent(state: DownloadUIState, intent: MainpageUIIntent) {
        when(intent){
            is MainpageUIIntent.OnResume -> {

            }
            is MainpageUIIntent.ExecuteDownload -> {
                mutex.withLock{
                    handleNewDownload(intent.twitterID)
                }
            }
        }
    }
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private suspend fun handleNewDownload(tweetId: String) = withMainContext {
        try {
            emitState(uiState.value.copy(isLoading = true))
            when (val result = twitterApiService.getTweetDetailAsync(tweetId)) {
                is TwitterRequestResult.Success -> {
                    result.data.videoUrls.forEach { videoUrl ->
                        createAndStartDownload(videoUrl, result.data.user)
                    }
                }
                is TwitterRequestResult.Error -> emitEffect(SnackbarUIEffect.ShowSnackbar(result.message))
            }
        } catch (e: Exception) {
            emitEffect(SnackbarUIEffect.ShowSnackbar(e.message ?: "Internal Error"))
        } finally {
            emitState(uiState.value.copy(isLoading = false))
        }
    }

    private suspend fun createAndStartDownload(videoUrl: String, user: TwitterUser?) = withMainContext {
        val download = Download(
            uuid = UUID.randomUUID().toString(),
            twitterUserId = user?.id,
            twitterScreenName = user?.screenName,
            twitterName = user?.name,
            fileUri = null,
            link = videoUrl,
            fileName = "${System.currentTimeMillis()}.mp4",
            fileType = "video/mp4",
            fileSize = 0L,
            status = DownloadStatus.PENDING
        )
        downloadRepository.insert(download)
        downloadManager.enqueueDownload(download)
        updateDownloadItem(download.uuid) {
            DownloadItem.fromDownload(download)
        }
    }
    private suspend fun updateDownloadItem(
        downloadId: String,
        update: (DownloadItem) -> DownloadItem
    ) {
        val downloads = uiState.value.downloads.toMutableList()
        val index = downloads.indexOfFirst { it.id == downloadId }
        if (index != -1) {
            downloads[index] = update(downloads[index])
            emitState(uiState.value.copy(downloads = downloads))
        }
    }
}