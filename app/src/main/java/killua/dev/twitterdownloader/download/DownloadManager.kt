package killua.dev.twitterdownloader.download

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.core.app.NotificationCompat
import androidx.work.WorkInfo
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import db.Download
import db.DownloadStatus
import killua.dev.base.R
import killua.dev.base.datastore.readMaxConcurrentDownloads
import killua.dev.base.datastore.readMaxRetries
import killua.dev.base.datastore.readNotificationEnabled
import killua.dev.base.ui.SnackbarUIEffect
import killua.dev.base.utils.DownloadEventManager
import killua.dev.twitterdownloader.repository.DownloadRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
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
) {
    private val _downloadProgress = MutableStateFlow<Map<String, Int>>(emptyMap())
    val downloadProgress = _downloadProgress.asStateFlow()

    private val managerScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val channelId = "download_channel"

    private val isNotificationEnabled = MutableStateFlow(false)
    private val maxRetries = MutableStateFlow(3)

    init {
        managerScope.launch{
            isNotificationEnabled.value = context.readNotificationEnabled().first()
            maxRetries.value = context.readMaxRetries().first()
        }
        createNotificationChannel()
        queueManager.onDownloadStart = { task ->
            managerScope.launch {
                downloadFile(task)
            }
        }
    }
    private suspend fun downloadFile(task: DownloadTask) {
        val (id, url, fileName, screenName,  destinationFolder,) = task
        println("URL:$url")
        println("Name: $fileName")
        var itemUri: Uri? = null
        var output: OutputStream? = null
        var input: BufferedInputStream? = null
        var attempt = 0
        while(attempt <= maxRetries.value){
            try {
                val filePath = "$destinationFolder/$screenName"
                val contentValues = ContentValues().apply {
                    put(MediaStore.Video.Media.DISPLAY_NAME, fileName)
                    put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                    put(MediaStore.Video.Media.RELATIVE_PATH, filePath)
                    put(MediaStore.Video.Media.IS_PENDING, 1)
                }

                itemUri = context.contentResolver.insert(
                    MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY),
                    contentValues
                ) ?: throw IllegalStateException("MediaStore insert failed")

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

                            val updateValues = ContentValues().apply {
                                put(MediaStore.Video.Media.IS_PENDING, 0)
                            }
                            context.contentResolver.update(itemUri, updateValues, null, null)
                            markDownloadCompleted(task.id, itemUri, totalBytes)
                            queueManager.markComplete(task, itemUri)
                            if(isNotificationEnabled.value){
                                showDownloadComplete(task.id, itemUri, screenName)
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
                    showDownloadFailed(task.id, e.message.toString())
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

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            channelId,
            "Download Notification",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Show download's progress and status"
            setShowBadge(true)
            enableVibration(true)
        }
        notificationManager.createNotificationChannel(channel)
    }

    fun showDownloadProgress(downloadId: String, progress: Int) {
        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle("Downloading")
            .setProgress(100, progress, false)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSmallIcon(R.drawable.icon)
            .build()

        notificationManager.notify(downloadId.hashCode(), notification)
    }

    fun showDownloadComplete(downloadId: String, fileUri: Uri, name: String) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(fileUri, "video/*")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            downloadId.hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle("$name 's video is ready.")
            .setContentText("Click to open")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.drawable.icon)
            .build()

        notificationManager.notify(downloadId.hashCode(), notification)
    }

    fun showDownloadFailed(downloadId: String, error: String?) {
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.icon)
            .setContentTitle("Download failed")
            .setContentText(error ?: "ERROR")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(downloadId.hashCode(), notification)
    }

    fun cancelNotification(downloadId: String) {
        notificationManager.cancel(downloadId.hashCode())
    }

}