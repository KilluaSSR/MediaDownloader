package ui
import Model.DownloadItem
import Model.DownloadUIEffect
import Model.DownloadUIEffect.ShowError
import Model.DownloadUIIntent
import Model.DownloadUIIntent.CancelDownload
import Model.DownloadUIIntent.DeleteDownload
import Model.DownloadUIIntent.PauseDownload
import Model.DownloadUIIntent.ResumeDownload
import Model.DownloadUIIntent.StartDownload
import Model.DownloadUIState
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import api.Model.TwitterRequestResult
import api.Model.TwitterUser
import api.TwitterApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import db.Download
import db.DownloadState
import db.DownloadStatus
import download.DownloadManager
import download.DownloadPreChecks
import download.VideoDownloadWorker
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import repository.DownloadRepository
import ui.BaseViewModel
import java.io.File
import java.util.UUID
import javax.inject.Inject
@HiltViewModel
class DownloadViewModel @Inject constructor(
    private val twitterApiService: TwitterApiService,
    private val downloadRepository: DownloadRepository,
    private val downloadManager: DownloadManager,
    private val downloadPreChecks: DownloadPreChecks,
) : BaseViewModel<DownloadUIIntent, DownloadUIState, DownloadUIEffect>(DownloadUIState()) {
    init {
        loadDownloads()
        observeAllDownloads()
    }

    private fun loadDownloads() = launchOnIO {
        try {
            val downloads = downloadRepository.getAllDownloads()
                .map { DownloadItem.fromDownload(it) }
            emitState(uiState.value.copy(downloads = downloads))
        } catch (e: Exception) {
            onEffect(ShowError(e.message ?: "Internal Error"))
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override suspend fun onEvent(state: DownloadUIState, intent: DownloadUIIntent) {
        when(intent) {
            is StartDownload -> handleNewDownload(intent.tweetId)
            is ResumeDownload -> resumeDownload(intent.downloadId)
            is PauseDownload -> pauseDownload(intent.downloadId)
            is CancelDownload -> cancelDownload(intent.downloadId)
            is DeleteDownload -> deleteDownload(intent.downloadId)
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
                is TwitterRequestResult.Error -> onEffect(ShowError(result.message))
            }
        } catch (e: Exception) {
            onEffect(ShowError(e.message ?: "下载初始化失败"))
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

    private suspend fun resumeDownload(downloadId: String) = withMainContext {
        downloadRepository.getById(downloadId)?.let { download ->
            downloadManager.enqueueDownload(download)
            updateDownloadItem(downloadId) { item ->
                item.copy(downloadState = DownloadState.Downloading(item.progress))
            }
        }
    }

    private suspend fun pauseDownload(downloadId: String) = withMainContext {
        downloadManager.cancelDownload(downloadId)
        downloadRepository.updateDownloadProgress(downloadId, 0)
        updateDownloadItem(downloadId) { item ->
            item.copy(downloadState = DownloadState.Pending)
        }
    }

    private suspend fun cancelDownload(downloadId: String) = withMainContext {
        downloadManager.cancelDownload(downloadId)
        downloadRepository.delete(downloadRepository.getById(downloadId)!!)
        val downloads = uiState.value.downloads.filterNot { it.id == downloadId }
        emitState(uiState.value.copy(downloads = downloads))
    }

    private suspend fun deleteDownload(downloadId: String) = withMainContext {
        downloadRepository.getById(downloadId)?.let { download ->
            download.fileUri?.path?.let { path ->
                File(path).delete()
            }
            downloadRepository.delete(download)
            val downloads = uiState.value.downloads.filterNot { it.id == downloadId }
            emitState(uiState.value.copy(downloads = downloads))
        }
    }

    private fun observeAllDownloads() {
        downloadManager.getWorkInfoFlow().onEach { workInfos->
            workInfos.forEach { workInfo ->
                handleWorkInfoUpdate(workInfo)
            }
        }.flowOnIO().launchIn(viewModelScope)
    }

    private suspend fun handleWorkInfoUpdate(workInfo: WorkInfo) {
        val downloadId = workInfo.progress.getString(VideoDownloadWorker.KEY_DOWNLOAD_ID) ?: return

        when (workInfo.state) {
            WorkInfo.State.RUNNING -> {
                val progress = workInfo.progress.getInt(VideoDownloadWorker.PROGRESS, 0)
                updateDownloadItem(downloadId) { item ->
                    item.copy(downloadState = DownloadState.Downloading(progress))
                }
            }
            WorkInfo.State.SUCCEEDED -> {
                val fileUri = workInfo.outputData.getString("file_uri")?.let { Uri.parse(it) }
                val fileSize = workInfo.outputData.getLong("file_size", 0L)
                if (fileUri != null) {
                    downloadRepository.updateCompleted(downloadId, fileUri, fileSize)
                    updateDownloadItem(downloadId) { item ->
                        item.copy(downloadState = DownloadState.Completed(fileUri, fileSize))
                    }
                }
            }
            WorkInfo.State.FAILED -> {
                val error = workInfo.outputData.getString("error") ?: "下载失败"
                downloadRepository.updateError(downloadId, errorMessage = error)
                updateDownloadItem(downloadId) { item ->
                    item.copy(downloadState = DownloadState.Failed(error))
                }
            }
            else -> {}
        }
    }

    private suspend fun updateDownloadProgress(downloadId: String, progress: Int) {
        downloadRepository.updateDownloadProgress(downloadId, progress)
        updateDownloadItem(downloadId) { item ->
            item.copy(downloadState = DownloadState.Downloading(progress))
        }
    }

    private suspend fun handleDownloadSuccess(downloadId: String, workInfo: WorkInfo) {
        val fileUri = workInfo.outputData.getString("file_uri")?.let { Uri.parse(it) }
        val fileSize = workInfo.outputData.getLong("file_size", 0L)
        if (fileUri != null) {
            downloadRepository.updateCompleted(downloadId, fileUri, fileSize)
            updateDownloadItem(downloadId) { item ->
                item.copy(downloadState = DownloadState.Completed(fileUri, fileSize))
            }
        }
    }

    private suspend fun handleDownloadFailure(downloadId: String, error: String) {
        downloadRepository.updateError(uuid = downloadId, errorMessage = error)
        updateDownloadItem(downloadId) { item ->
            item.copy(downloadState = DownloadState.Failed(error))
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