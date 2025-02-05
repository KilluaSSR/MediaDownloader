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
    private val showNotification: ShowNotification
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
        val (id, url, _, type, _, screenName, _) = task
        var itemUri: Uri? = null
        var output: OutputStream? = null
        var input: BufferedInputStream? = null
        var attempt = 0
        while(attempt <= maxRetries.value){
            try {
                val mediaHelper = MediaStoreHelper(context)
                val itemUri = mediaHelper.insertMedia(
                    fileName = task.fileName,
                    filePath = task.destinationFolder,
                    type = task.type
                )
                val client = OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build()

                client.newCall(Request.Builder().url(url).build()).execute().use { response ->
                    if (!response.isSuccessful) throw IllegalStateException("Network error: ${response.code}")

                    val responseBody = response.body ?: throw IllegalStateException("Response body is null")
                    val totalBytes = responseBody.contentLength()
                    var bytesRead = 0L

                    withContext(Dispatchers.IO) {
                        try {
                            output = context.contentResolver.openOutputStream(itemUri)
                                ?: throw IllegalStateException("Failed to open output stream")
                            input = responseBody.byteStream().buffered()

                            val buffer = ByteArray(8192)
                            while (isActive) {
                                val read = input.read(buffer)
                                if (read == -1) break

                                output.write(buffer, 0, read)
                                output.flush()

                                bytesRead += read
                                val progress = if (totalBytes > 0) ((bytesRead * 100) / totalBytes).toInt() else 0
                                _downloadProgress.update { it + (id to progress) }
                                //showDownloadProgress(id, progress)
                            }

                            mediaHelper.markMediaAsComplete(itemUri)
                            markDownloadCompleted(task.id, itemUri, totalBytes)
                            queueManager.markComplete(task, itemUri)
                            if(isNotificationEnabled.value){
                                showNotification.showDownloadComplete(task.id, itemUri, screenName, type)
                            }
                        } finally {
                            input?.close()
                            output?.close()
                        }
                    }

                }
                break
            } catch (e: Exception) {
                input?.close()
                output?.close()
                itemUri?.let { context.contentResolver.delete(it, null, null) }
                markDownloadFailed(task.id, e.message.toString())
                queueManager.markFailed(task, e.message ?: "Unknown error")
                SnackbarUIEffect.ShowSnackbar("$screenName 's video is failed to download. Retrying time $attempt", actionLabel = "STOP!" ,withDismissAction = true, onActionPerformed = {
                    attempt = maxRetries.value + 1
                    return@ShowSnackbar
                })
                if(isNotificationEnabled.value){
                    showNotification.showDownloadFailed(task.id, e.message.toString())
                }
                if (attempt >= maxRetries.value) {
                    break
                }
                attempt ++
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