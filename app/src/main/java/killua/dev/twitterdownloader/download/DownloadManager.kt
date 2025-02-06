package killua.dev.twitterdownloader.download

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import db.DownloadStatus
import killua.dev.base.Model.DownloadTask
import killua.dev.base.datastore.readMaxRetries
import killua.dev.base.datastore.readNotificationEnabled
import killua.dev.base.ui.SnackbarUIEffect
import killua.dev.base.utils.MediaStoreHelper
import killua.dev.base.utils.ShowNotification
import killua.dev.twitterdownloader.repository.DownloadRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.BufferedInputStream
import java.io.OutputStream
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DownloadManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val queueManager: DownloadQueueManager,
    private val downloadRepository: DownloadRepository,
    private val showNotification: ShowNotification,
    private val downloaderFactory: MediaDownloaderFactory
) {
    private val _downloadProgress = MutableStateFlow<Map<String, Int>>(emptyMap())
    val downloadProgress = _downloadProgress.asStateFlow()

    private val managerScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val isNotificationEnabled = MutableStateFlow(false)
    private val maxRetries = MutableStateFlow(3)

    init {
        managerScope.launch{
            isNotificationEnabled.value = context.readNotificationEnabled().first()
            maxRetries.value = context.readMaxRetries().first()
        }
        showNotification.createNotificationChannel()
        queueManager.onDownloadStart = { task ->
            managerScope.launch {
                downloadFile(task)
            }
        }
    }

    private suspend fun downloadFile(task: DownloadTask) {
        var attempt = 0
        while(attempt <= maxRetries.value) {
            try {
                val downloader = downloaderFactory.create(task.from)
                val result = downloader.download(task, emptyMap()) { progress ->
                    _downloadProgress.update { it + (task.id to progress) }
                }
                result.fold(
                    onSuccess = { uri ->
                        markDownloadCompleted(task.id, uri, 0L)
                        queueManager.markComplete(task, uri)
                        if(isNotificationEnabled.value) {
                            showNotification.showDownloadComplete(task.id, uri, task.screenName, task.type)
                        }
                    },
                    onFailure = { throw it }
                )
                break
            } catch (e: Exception) {
                markDownloadFailed(task.id, e.message.toString())
                queueManager.markFailed(task, e.message ?: "Unknown error")
                if(isNotificationEnabled.value) {
                    showNotification.showDownloadFailed(task.id, e.message.toString())
                }

                attempt++
                if (attempt > maxRetries.value) break

                SnackbarUIEffect.ShowSnackbar(
                    "${task.screenName}'s ${task.type} failed to download. Retrying $attempt",
                    actionLabel = "STOP!",
                    withDismissAction = true
                ) {
                    attempt = maxRetries.value + 1
                }
            }
        }
    }
    private fun markDownloadCompleted(downloadId: String, fileUri: Uri, fileSize: Long) {
        managerScope.launch{
            downloadRepository.updateCompleted(downloadId, fileUri, fileSize)
        }
    }

    private fun markDownloadFailed(downloadId: String, errorMessage: String) {
        managerScope.launch{
            downloadRepository.updateError(downloadId, errorMessage)
        }
    }


    private fun markDownloadCancelled(downloadId: String) {
        managerScope.launch{
            downloadRepository.updateStatus(downloadId, DownloadStatus.PENDING)
        }
    }
}