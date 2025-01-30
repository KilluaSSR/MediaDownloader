package killua.dev.twitterdownloader.ui.ViewModels

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.work.WorkInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import db.Download
import db.DownloadStatus
import killua.dev.base.ui.BaseViewModel
import killua.dev.base.ui.SnackbarUIEffect
import killua.dev.base.ui.UIIntent
import killua.dev.base.ui.UIState
import killua.dev.twitterdownloader.Model.DownloadItem
import killua.dev.twitterdownloader.download.DownloadManager
import killua.dev.twitterdownloader.download.VideoDownloadWorker
import killua.dev.twitterdownloader.repository.DownloadRepository
import killua.dev.twitterdownloader.repository.ThumbnailRepository
import killua.dev.twitterdownloader.ui.Destinations.Download.DownloadPageDestinations
import killua.dev.twitterdownloader.utils.NavigateTwitterProfile
import killua.dev.twitterdownloader.utils.NavigateTwitterTweet
import killua.dev.twitterdownloader.utils.loadCachedThumbnailOrCreate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID
import javax.inject.Inject


sealed interface DownloadPageUIIntent : UIIntent {
    object LoadDownloads : DownloadPageUIIntent

    data class FilterDownloads(val filter: DownloadPageDestinations) : DownloadPageUIIntent

    data class ResumeDownload(val downloadId: String) : DownloadPageUIIntent

    data class RetryDownload(val downloadId: String) : DownloadPageUIIntent

    data class PauseDownload(val downloadId: String) : DownloadPageUIIntent

    data class CancelDownload(val downloadId: String) : DownloadPageUIIntent

    data object CancelAll: DownloadPageUIIntent

    data object RetryAll: DownloadPageUIIntent

    data class GoToTwitter(val downloadId: String, val context: Context) : DownloadPageUIIntent
}

data class DownloadPageUIState(
    val optionIndex: Int,
    val optionsType: DownloadPageDestinations,
    val isLoading: Boolean,
    val downloads: List<DownloadItem> = emptyList(),
    val thumbnailCache: Map<Uri, Bitmap?> = emptyMap()
) : UIState

@HiltViewModel
class DownloadedViewModel @Inject constructor(
    private val downloadRepository: DownloadRepository,
    private val downloadManager: DownloadManager,
    private val thumbnailRepository: ThumbnailRepository
) : BaseViewModel<DownloadPageUIIntent, DownloadPageUIState, SnackbarUIEffect>(
    DownloadPageUIState(
        optionIndex = 0,
        isLoading = true,
        optionsType = DownloadPageDestinations.All
    )
) {
    private val activeDownloads = mutableSetOf<String>()
    init {
    observeDownloadsFromDB()
    observeWorkManager()
    }
    private fun observeDownloadsFromDB() {
        launchOnIO {
            downloadRepository.observeAllDownloads()
                .collect { downloads ->
                    val uris = downloads
                        .mapNotNull { it.fileUri }

                    val thumbnails = uris.associateWith { uri ->
                        thumbnailRepository.getThumbnail(uri)
                    }
                    emitState(
                        uiState.value.copy(
                            downloads = downloads.map { DownloadItem.fromDownload(it) },
                            thumbnailCache = thumbnails,
                            isLoading = false
                        )
                    )
                }
        }
    }
    /**
     * 监听 WorkManager 后台下载任务 状态变化
     */
    private fun observeWorkManager() {
        downloadManager.getWorkInfoFlow()
            .onEach { workInfos ->
                workInfos.forEach { handleWorkInfoUpdate(it) }
            }
            .flowOn(Dispatchers.IO)
            .launchIn(viewModelScope)
    }
    /**
     * 处理 WorkManager 任务状态更新，并同步到数据库 + UI
     */
    private suspend fun handleWorkInfoUpdate(workInfo: WorkInfo) {
        val downloadId = workInfo.progress.getString(VideoDownloadWorker.KEY_DOWNLOAD_ID) ?: return

        when (workInfo.state) {
            WorkInfo.State.RUNNING -> {
                val progress = workInfo.progress.getInt(VideoDownloadWorker.PROGRESS, 0)
                updateDownloadProgress(downloadId, progress)
            }

            WorkInfo.State.SUCCEEDED -> {
                val fileUri = workInfo.outputData.getString("file_uri")?.let { Uri.parse(it) }
                val fileSize = workInfo.outputData.getLong("file_size", 0L)
                if (fileUri != null) {
                    markDownloadCompleted(downloadId, fileUri, fileSize)
                }
            }

            WorkInfo.State.FAILED -> {
                val error = workInfo.outputData.getString("error") ?: "Download failed"
                markDownloadFailed(downloadId, error)
            }

            WorkInfo.State.CANCELLED -> {
                markDownloadCancelled(downloadId)
            }

            else -> {}
        }
    }
    /**
     * 更新下载进度
     */
    private suspend fun updateDownloadProgress(downloadId: String, progress: Int) {
        downloadRepository.updateDownloadProgress(downloadId, progress)
    }

    /**
     * 标记下载完成
     */
    private suspend fun markDownloadCompleted(downloadId: String, fileUri: Uri, fileSize: Long) {
        downloadRepository.updateCompleted(downloadId, fileUri, fileSize)
        activeDownloads.remove(downloadId)
    }

    /**
     * 标记下载失败
     */
    private suspend fun markDownloadFailed(downloadId: String, errorMessage: String) {
        downloadRepository.updateError(downloadId, errorMessage)
        activeDownloads.remove(downloadId)
    }

    /**
     * 标记任务取消
     */
    private suspend fun markDownloadCancelled(downloadId: String) {
        downloadRepository.updateStatus(downloadId, DownloadStatus.PENDING)
        activeDownloads.remove(downloadId)
    }

    override suspend fun onEvent(state: DownloadPageUIState, intent: DownloadPageUIIntent) {
        when (intent) {
            is DownloadPageUIIntent.LoadDownloads -> {
                observeDownloadsFromDB()
            }

            is DownloadPageUIIntent.FilterDownloads -> {
                launchOnIO {
                    val filteredDownloads = when (intent.filter) {
                        DownloadPageDestinations.All -> downloadRepository.getAllDownloads()
                        DownloadPageDestinations.Downloading -> downloadRepository.getDownloadingItems()
                        DownloadPageDestinations.Completed -> downloadRepository.getByStatus(
                            DownloadStatus.COMPLETED
                        )

                        DownloadPageDestinations.Failed -> downloadRepository.getByStatus(
                            DownloadStatus.FAILED
                        )
                    }
                    emitState(state.copy(downloads = filteredDownloads.map {
                        DownloadItem.fromDownload(
                            it
                        )
                    }))
                }
            }

            is DownloadPageUIIntent.ResumeDownload -> {
                launchOnIO {
                    downloadRepository.updateStatus(intent.downloadId, DownloadStatus.DOWNLOADING)
                }
            }

            is DownloadPageUIIntent.PauseDownload -> {
                launchOnIO {
                    downloadRepository.updateStatus(intent.downloadId, DownloadStatus.PENDING)
                }

            }

            is DownloadPageUIIntent.CancelDownload -> {
                launchOnIO {
                    downloadRepository.delete(downloadRepository.getById(intent.downloadId)!!)
                }
            }

            DownloadPageUIIntent.CancelAll -> handleCancelAll()
            DownloadPageUIIntent.RetryAll -> handleRetryAll()
            is DownloadPageUIIntent.RetryDownload -> {
                launchOnIO {
                    retryDownload(intent.downloadId)
                }
            }

            is DownloadPageUIIntent.GoToTwitter -> {
                launchOnIO {
                    val download = downloadRepository.getById(intent.downloadId)
                    if (download == null){
                        launchOnIO { emitEffect(SnackbarUIEffect.ShowSnackbar("Not Found")) }
                    }
                    val userScreenName = download?.twitterScreenName
                    val tweetID = download?.tweetID
                    withMainContext {
                        intent.context.NavigateTwitterTweet(userScreenName!!,tweetID)
                    }
                }
            }
        }
    }


    /**
     * 恢复单个下载
     */
    private suspend fun resumeDownload(downloadId: String) {
        val download = downloadRepository.getById(downloadId) ?: return
        if (download.status == DownloadStatus.COMPLETED) return
        downloadManager.enqueueDownload(download)
        activeDownloads.add(downloadId)
    }

    /**
     * 暂停单个下载
     */
    private suspend fun pauseDownload(downloadId: String) {
        downloadManager.cancelDownload(downloadId)
        downloadRepository.updateStatus(downloadId, DownloadStatus.PENDING)
        activeDownloads.remove(downloadId)
    }

    /**
     * 取消单个下载
     */
    private suspend fun cancelDownload(downloadId: String) {
        downloadManager.cancelDownload(downloadId)
        downloadRepository.deleteById(downloadId)
        activeDownloads.remove(downloadId)
    }

    /**
     * 删除下载任务
     */
    private suspend fun deleteDownload(downloadId: String) {
        val download = downloadRepository.getById(downloadId) ?: return
        download.fileUri?.path?.let { File(it).delete() }
        downloadRepository.delete(download)
        activeDownloads.remove(downloadId)
    }

    /**
     * 恢复所有暂停的下载
     */
    private suspend fun handleResumeAll() {
        val pendingDownloads = downloadRepository.getPendingDownloads()
        if (pendingDownloads.isEmpty()) return
        pendingDownloads.forEach { resumeDownload(it.uuid) }
    }

    /**
     * 暂停所有进行中的下载
     */
    private suspend fun handlePauseAll() {
        val activeDownloads = downloadRepository.getDownloadingItems()
        if (activeDownloads.isEmpty()) return
        activeDownloads.forEach { pauseDownload(it.uuid) }
    }

    /**
     * 取消所有未完成的下载
     */
    private suspend fun handleCancelAll() {
        val allDownloads = downloadRepository.getActiveDownloads()
        if (allDownloads.isEmpty()) {
            viewModelScope.launch{
                emitEffect(SnackbarUIEffect.ShowSnackbar("No active downloads"))
            }
            return
        }
        allDownloads.forEach { cancelDownload(it.uuid) }
    }

    private suspend fun retryDownload(downloadId: String){
        val download = downloadRepository.getById(downloadId) ?: return
        cancelDownload(downloadId)
        val newdownload = Download(
            uuid =download.uuid,
            twitterUserId = download.twitterUserId,
            twitterScreenName = download.twitterScreenName,
            twitterName = download.twitterName,
            tweetID = download.tweetID,
            fileUri = null,
            link = download.link,
            fileName = download.fileName,
            fileType = "video",
            fileSize = 0L,
            status = DownloadStatus.PENDING,
            mimeType = "video/mp4"
        )
        downloadRepository.insert(download)
        downloadManager.enqueueDownload(download)
    }

    private suspend fun handleRetryAll(){
        val failedDownloads = downloadRepository.getByStatus(status = DownloadStatus.FAILED)
        if (failedDownloads.isEmpty()) {
            viewModelScope.launch{
                emitEffect(SnackbarUIEffect.ShowSnackbar("No failed downloads"))
            }
            return
        }
        failedDownloads.forEach { it->
            cancelDownload(it.uuid)
            val download = Download(
                uuid =it.uuid,
                twitterUserId = it.twitterUserId,
                twitterScreenName = it.twitterScreenName,
                twitterName = it.twitterName,
                tweetID = it.tweetID,
                fileUri = null,
                link = it.link,
                fileName = it.fileName,
                fileType = "video",
                fileSize = 0L,
                status = DownloadStatus.PENDING,
                mimeType = "video/mp4"
            )
            downloadRepository.insert(download)
            downloadManager.enqueueDownload(download)
        }
    }
}
