package killua.dev.twitterdownloader.ui.ViewModels

import android.net.Uri
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import db.DownloadState
import db.DownloadStatus
import killua.dev.base.ui.BaseViewModel
import killua.dev.base.ui.SnackbarUIEffect
import killua.dev.base.ui.UIIntent
import killua.dev.base.ui.UIState
import killua.dev.twitterdownloader.Model.DownloadItem
import killua.dev.twitterdownloader.download.DownloadManager
import killua.dev.twitterdownloader.download.VideoDownloadWorker
import killua.dev.twitterdownloader.repository.DownloadRepository
import killua.dev.twitterdownloader.ui.Destinations.Download.DownloadPageDestinations
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import javax.inject.Inject

sealed class DownloadedPageUIIntent : UIIntent {
    data class ResumeDownload(val downloadId: String) : DownloadedPageUIIntent()
    data class PauseDownload(val downloadId: String) : DownloadedPageUIIntent()
    data class CancelDownload(val downloadId: String) : DownloadedPageUIIntent()
    data class DeleteDownload(val downloadId: String) : DownloadedPageUIIntent()
    object ResumeAll : DownloadedPageUIIntent()
    object PauseAll : DownloadedPageUIIntent()
    object CancelAll : DownloadedPageUIIntent()
    object NavigateToAll: DownloadedPageUIIntent()
    object NavigateToDownloading: DownloadedPageUIIntent()
    object NavigateToDownloaded: DownloadedPageUIIntent()
    object NavigateToFailed: DownloadedPageUIIntent()
}

data class DownloadPageUIState(
    val optionIndex: Int,
    val optionsType: DownloadPageDestinations,
    val isLoading: Boolean,
    val downloads: List<DownloadItem> = emptyList(),
) : UIState

@HiltViewModel
class DownloadedViewModel @Inject constructor(
    private val downloadRepository: DownloadRepository,
    private val downloadManager: DownloadManager
) : BaseViewModel<DownloadedPageUIIntent, DownloadPageUIState, SnackbarUIEffect>(
    DownloadPageUIState(
        optionIndex = 0,
        isLoading = true,
        optionsType = DownloadPageDestinations.All
    )
) {
    private val mutex = Mutex()
    private val activeDownloads = mutableSetOf<String>()
    init {
        loadDownloads()
        observeAllDownloads()
    }

    override suspend fun onEvent(state: DownloadPageUIState, intent: DownloadedPageUIIntent) {
        when (intent) {
            is DownloadedPageUIIntent.CancelDownload -> mutex.withLock {
                handleOperation(intent.downloadId) { cancelDownload(it) }
            }

            is DownloadedPageUIIntent.DeleteDownload -> mutex.withLock {
                handleOperation(intent.downloadId) { deleteDownload(it) }
            }

            is DownloadedPageUIIntent.PauseDownload -> mutex.withLock {
                handleOperation(intent.downloadId) { pauseDownload(it) }
            }

            is DownloadedPageUIIntent.ResumeDownload -> mutex.withLock {
                handleOperation(intent.downloadId) { resumeDownload(it) }
            }

            is DownloadedPageUIIntent.ResumeAll -> mutex.withLock { handleResumeAll() }
            is DownloadedPageUIIntent.PauseAll -> mutex.withLock { handlePauseAll() }
            is DownloadedPageUIIntent.CancelAll -> mutex.withLock { handleCancelAll() }
            is DownloadedPageUIIntent.NavigateToAll -> {loadDownloads()}
            is DownloadedPageUIIntent.NavigateToDownloaded -> {loadDownloaded()}
            is DownloadedPageUIIntent.NavigateToDownloading -> {loadDownloading()}
            is DownloadedPageUIIntent.NavigateToFailed -> {loadError()}
        }
    }
    private fun loadDownloadsWithFilter(
        filter: ((DownloadItem) -> Boolean)? = null
    ) = launchOnIO {
        try {
            emitState(uiState.value.copy(isLoading = true))
            var downloads = downloadRepository.getAllDownloads()
                .map { DownloadItem.fromDownload(it) }
                .also { checkActiveDownloads(it) }

            filter?.let { downloads = downloads.filter(it) }

            emitState(uiState.value.copy(
                isLoading = false,
                downloads = downloads
            ))
        } catch (e: Exception) {
            emitState(uiState.value.copy(isLoading = false))
            emitEffect(SnackbarUIEffect.ShowSnackbar("Error loading：${e.message}"))
        }
    }

    private fun loadError() = loadDownloadsWithFilter {
        it.downloadState.toDownloadStatus() == DownloadStatus.FAILED
    }

    private fun loadDownloading() = loadDownloadsWithFilter {
        it.downloadState.toDownloadStatus() == DownloadStatus.DOWNLOADING
    }

    private fun loadDownloaded() = loadDownloadsWithFilter {
        it.downloadState.toDownloadStatus() == DownloadStatus.COMPLETED
    }

    private fun loadDownloads() = loadDownloadsWithFilter()
    private fun checkActiveDownloads(downloads: List<DownloadItem>) {
        downloads.forEach { download ->
            if (download.downloadState is DownloadState.Downloading) {
                if (downloadManager.isDownloadActive(download.id)) {
                    activeDownloads.add(download.id)
                }
            }
        }
    }


    private fun observeAllDownloads() {
        downloadManager.getWorkInfoFlow()
            .onEach { workInfos ->
                workInfos.forEach { handleWorkInfoUpdate(it) }
            }
            .flowOnIO()
            .launchIn(viewModelScope)
    }

    private suspend fun handleWorkInfoUpdate(workInfo: WorkInfo) {
        val downloadId = workInfo.progress.getString(VideoDownloadWorker.KEY_DOWNLOAD_ID) ?: return

        when (workInfo.state) {
            WorkInfo.State.RUNNING -> {
                val progress = workInfo.progress.getInt(VideoDownloadWorker.PROGRESS, 0)
                updateDownload(downloadId) {
                    it.copy(downloadState = DownloadState.Downloading(progress))
                }
            }

            WorkInfo.State.SUCCEEDED -> {
                val fileUri = workInfo.outputData.getString("file_uri")?.let { Uri.parse(it) }
                val fileSize = workInfo.outputData.getLong("file_size", 0L)
                if (fileUri != null) {
                    downloadRepository.updateCompleted(downloadId, fileUri, fileSize)
                    updateDownload(downloadId) {
                        it.copy(downloadState = DownloadState.Completed(fileUri, fileSize))
                    }
                    activeDownloads.remove(downloadId)
                }
            }

            WorkInfo.State.FAILED -> {
                val error = workInfo.outputData.getString("error") ?: "Download failed"
                downloadRepository.updateError(downloadId, errorMessage = error)
                updateDownload(downloadId) {
                    it.copy(downloadState = DownloadState.Failed(error))
                }
                activeDownloads.remove(downloadId)
            }

            WorkInfo.State.CANCELLED -> {
                activeDownloads.remove(downloadId)
                updateDownload(downloadId) {
                    it.copy(downloadState = DownloadState.Pending)
                }
            }

            else -> {}
        }
    }

    private suspend inline fun handleOperation(
        downloadId: String,
        operation: (String) -> Unit
    ) {
        try {
            operation(downloadId)
        } catch (e: Exception) {
            emitEffect(SnackbarUIEffect.ShowSnackbar("Error：${e.message}"))
        }
    }

    private suspend fun handleResumeAll() {
        val pending = uiState.value.downloads.filter {
            it.downloadState is DownloadState.Pending
        }
        if (pending.isEmpty()) {
            handleError("No tasks waiting to download")
            return
        }

        pending.forEach { download ->
            try {
                resumeDownload(download.id)
            } catch (e: Exception) {
                handleError("Error: ${download.id} - ${e.message}")
            }
        }
    }

    private suspend fun handlePauseAll() {
        val active = uiState.value.downloads.filter {
            it.downloadState is DownloadState.Downloading
        }
        if (active.isEmpty()) {
            handleError("Error")
            return
        }

        active.forEach { download ->
            try {
                pauseDownload(download.id)
            } catch (e: Exception) {
                handleError("Error: ${download.id} - ${e.message}")
            }
        }
    }

    private suspend fun handleCancelAll() {
        val downloads = uiState.value.downloads.filter {
            it.downloadState !is DownloadState.Completed
        }
        if (downloads.isEmpty()) {
            handleError("Error")
            return
        }

        downloads.forEach { download ->
            try {
                cancelDownload(download.id)
            } catch (e: Exception) {
                handleError("Error: ${download.id} - ${e.message}")
            }
        }
    }

    private suspend fun resumeDownload(downloadId: String) {
        downloadRepository.getById(downloadId)?.let { download ->
            when {
                download.status == DownloadStatus.COMPLETED -> {
                    emitEffect(SnackbarUIEffect.ShowSnackbar("Completed"))
                }

                else -> {
                    downloadManager.enqueueDownload(download)
                    activeDownloads.add(downloadId)
                    updateDownload(downloadId) {
                        it.copy(downloadState = DownloadState.Downloading(0))
                    }
                }
            }
        }
    }

    private suspend fun pauseDownload(downloadId: String) {
        downloadManager.cancelDownload(downloadId)
        downloadRepository.updateDownloadProgress(downloadId, 0)
        activeDownloads.remove(downloadId)
        updateDownload(downloadId) {
            it.copy(downloadState = DownloadState.Pending)
        }
    }

    private suspend fun cancelDownload(downloadId: String) {
        downloadManager.cancelDownload(downloadId)
        downloadRepository.getById(downloadId)?.let { download ->
            downloadRepository.delete(download)
            activeDownloads.remove(downloadId)
            val downloads =
                uiState.value.downloads.filterNot { it.id == downloadId }
            emitState(
                uiState.value.copy(
                    downloads = downloads
                )
            )
        }
    }

    private suspend fun deleteDownload(downloadId: String) {
        downloadRepository.getById(downloadId)?.let { download ->
            download.fileUri?.path?.let { path ->
                val file = File(path)
                if (file.exists() && !file.delete()) {
                    emitEffect(SnackbarUIEffect.ShowSnackbar("Failed to delete the file"))
                    return
                }
            }
            downloadRepository.delete(download)
            activeDownloads.remove(downloadId)
            val downloads =
                uiState.value.downloads.filterNot { it.id == downloadId }
            emitState(
                uiState.value.copy(
                    downloads = downloads
                )
            )
        }
    }

    private suspend fun updateDownload(
        downloadId: String,
        update: (DownloadItem) -> DownloadItem
    ) {
        val downloads = uiState.value.downloads.toMutableList()
        val index = downloads.indexOfFirst { it.id == downloadId }
        if (index != -1) {
            downloads[index] = update(downloads[index])
            emitState(
                uiState.value.copy(
                    downloads = downloads
                )
            )
        }
    }

    private suspend fun handleError(message: String) {
        emitEffect(
            SnackbarUIEffect.ShowSnackbar(
                message = message
            )
        )
    }
}