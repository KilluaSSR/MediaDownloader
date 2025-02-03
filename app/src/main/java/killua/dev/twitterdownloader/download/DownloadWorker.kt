package killua.dev.twitterdownloader.download

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import killua.dev.base.R
import killua.dev.base.datastore.readMaxRetries
import killua.dev.base.datastore.readNotificationEnabled
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.BufferedInputStream
import java.io.OutputStream
import java.util.concurrent.TimeUnit

class VideoDownloadWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val KEY_URL = "url"
        const val KEY_DOWNLOAD_ID = "download_id"
        const val KEY_SCREEN_NAME = "screen_name"
        const val KEY_NAME = "name"
        const val KEY_FILE_NAME = "file_name"
        const val PROGRESS = "progress"
        const val KEY_RANGE_HEADER = "range_header"
        const val DOWNLOAD_TAG = "download_tag"
        const val KEY_ERROR_MESSAGE = "error"
        const val FILE_URI = "file_uri"
        const val FILE_SIZE = "file_size"
    }
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    @SuppressLint("RestrictedApi")
    override suspend fun doWork(): Result {
        val url = inputData.getString(KEY_URL) ?: return Result.failure()
        val downloadId = inputData.getString(KEY_DOWNLOAD_ID) ?: return Result.failure()
        val screenName = inputData.getString(KEY_SCREEN_NAME) ?: return Result.failure()
        val fileName = inputData.getString(KEY_FILE_NAME) ?: return Result.failure()
        val MAX_RETRIES = context.readMaxRetries().first()
        val isNotificationEnabled = context.readNotificationEnabled().first()

        return try {
            withContext(scope.coroutineContext) {
                val result = downloadVideoToMediaStore(url, fileName, screenName, downloadId)
                when {
                    result is Result.Failure && runAttemptCount < MAX_RETRIES -> Result.retry()
                    result is Result.Success -> {
                        if (isNotificationEnabled) {
                            showDownloadComplete(
                                downloadId,
                                result.outputData.getString(FILE_URI)!!.toUri(),
                                result.outputData.getString(KEY_NAME)!!
                            )
                        }
                        result
                    }
                    else -> result
                }
            }
        } catch (e: Exception) {
            Log.e("VideoDownloadWorker", "Download failed", e)
            if (runAttemptCount < MAX_RETRIES) {
                Result.retry()
            } else {
                if (isNotificationEnabled) {
                    showDownloadFailed(downloadId, e.message ?: "Failed to download")
                }
                Result.failure(
                    workDataOf(
                        KEY_DOWNLOAD_ID to downloadId,
                        KEY_ERROR_MESSAGE to (e.message ?: "Failed to download")
                    )
                )
            }
        }
    }

    private suspend fun downloadVideoToMediaStore(
        url: String,
        fileName: String,
        screenName: String,
        downloadId: String
    ): Result = withContext(scope.coroutineContext) {
        var itemUri: Uri? = null
        var output: OutputStream? = null
        var input: BufferedInputStream? = null

        try {
            val contentValues = ContentValues().apply {
                put(MediaStore.Video.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/TwitterDownloader/$screenName")
                put(MediaStore.Video.Media.IS_PENDING, 1)
            }

            itemUri = context.contentResolver.insert(
                MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY),
                contentValues
            ) ?: throw IllegalStateException("Unable to insert into MediaStore")

            val client = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build()

            client.newCall(Request.Builder().url(url).build()).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IllegalStateException("Network error: ${response.code}")
                }

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

                            output.let {
                                it.write(buffer, 0, read)
                                it.flush()
                            }

                            bytesRead += read

                            if (totalBytes > 0) {
                                val progress = ((bytesRead * 100) / totalBytes).toInt()
                                setProgress(
                                    workDataOf(
                                        PROGRESS to progress,
                                        KEY_DOWNLOAD_ID to downloadId
                                    )
                                )
                            }
                        }

                        // 完成下载，更新文件状态
                        val updateValues = ContentValues().apply {
                            put(MediaStore.Video.Media.IS_PENDING, 0)
                        }
                        context.contentResolver.update(itemUri, updateValues, null, null)

                        Result.success(
                            workDataOf(
                                KEY_DOWNLOAD_ID to downloadId,
                                FILE_URI to itemUri.toString(),
                                FILE_SIZE to totalBytes,
                                KEY_NAME to fileName
                            )
                        )
                    } finally {
                        try {
                            input?.close()
                            output?.close()
                        } catch (e: Exception) {
                            Log.e("VideoDownloadWorker", "Error closing streams", e)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // 清理未完成的文件
            try {
                input?.close()
                output?.close()
                itemUri?.let { uri ->
                    context.contentResolver.delete(uri, null, null)
                }
            } catch (cleanupError: Exception) {
                Log.e("VideoDownloadWorker", "Error during cleanup", cleanupError)
            }
            throw e
        }
    }

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val channelId = "download_channel"

    init {
        createNotificationChannel()
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