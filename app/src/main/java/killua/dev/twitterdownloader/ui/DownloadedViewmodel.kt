package killua.dev.twitterdownloader.ui

import Model.DownloadItem
import Model.SnackbarUIEffect
import android.content.Context
import android.net.Uri
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import db.DownloadState
import download.DownloadManager
import download.VideoDownloadWorker
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import repository.DownloadRepository
import ui.BaseViewModel
import ui.UIIntent
import java.io.File
import javax.inject.Inject
sealed class DownloadedPageUIIntent : UIIntent{
    data class ResumeDownload(val downloadId: String) : DownloadedPageUIIntent()
    data class PauseDownload(val downloadId: String) : DownloadedPageUIIntent()
    data class CancelDownload(val downloadId: String) : DownloadedPageUIIntent()
    data class DeleteDownload(val downloadId: String) : DownloadedPageUIIntent()
    data class OnResume(val context: Context): DownloadedPageUIIntent()
}
@HiltViewModel
class DownloadedViewmodel @Inject constructor(
    private val downloadRepository: DownloadRepository,
    private val downloadManager: DownloadManager,
) : BaseViewModel<DownloadedPageUIIntent, DownloadUIState, SnackbarUIEffect>(
    DownloadUIState()
){
    val mutex = Mutex()
    override suspend fun onEvent(state: DownloadUIState, intent: DownloadedPageUIIntent) {
        when (intent){
            is DownloadedPageUIIntent.OnResume -> {
                loadDownloads()
                observeAllDownloads()
            }
            is DownloadedPageUIIntent.CancelDownload -> {
                mutex.withLock {
                    cancelDownload(intent.downloadId)
                }
            }
            is DownloadedPageUIIntent.DeleteDownload -> {
                mutex.withLock {
                    deleteDownload(intent.downloadId)
                }
            }
            is DownloadedPageUIIntent.PauseDownload -> {
                mutex.withLock {
                    pauseDownload(intent.downloadId)
                }
            }
            is DownloadedPageUIIntent.ResumeDownload -> {
                mutex.withLock {
                    resumeDownload(intent.downloadId)
                }
            }
        }
    }
    private fun loadDownloads() = launchOnIO {
        try {
            val downloads = downloadRepository.getAllDownloads()
                .map { DownloadItem.fromDownload(it) }
            emitState(uiState.value.copy(downloads = downloads))
        } catch (e: Exception) {
            emitEffect(SnackbarUIEffect.ShowSnackbar(e.message ?: "Internal Error"))
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