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
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import killua.dev.base.R
import killua.dev.base.datastore.readMaxRetries
import killua.dev.base.datastore.readNotificationEnabled
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
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

    @SuppressLint("RestrictedApi")
    override suspend fun doWork(): Result {
        val url = inputData.getString(KEY_URL) ?: return Result.failure()
        val downloadId = inputData.getString(KEY_DOWNLOAD_ID) ?: return Result.failure()
        val screenName = inputData.getString(KEY_SCREEN_NAME) ?: return Result.failure()
        val fileName = inputData.getString(KEY_FILE_NAME) ?: return Result.failure()
        val MAX_RETRIES = context.readMaxRetries().first()
        val isNotificationEnabled = context.readNotificationEnabled().first()
        return try {
            val result = downloadVideoToMediaStore(url, fileName, screenName, downloadId)
            if (result is Result.Failure && runAttemptCount < MAX_RETRIES) {
                Result.retry()
            } else if (result is Result.Success){
                if(isNotificationEnabled){
                    showDownloadComplete(downloadId,result.outputData.getString(FILE_URI)!!.toUri(), result.outputData.getString(KEY_NAME)!!)
                }
                result
            }else{
                result
            }
        } catch (e: Exception) {
            if (runAttemptCount < MAX_RETRIES) {
                Result.retry()
            } else {
                if(isNotificationEnabled){
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
    ): Result = withContext(Dispatchers.IO) {
        val MAX_RETRIES = context.readMaxRetries().first()
        val contentValues = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
            put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/TwitterDownloader/$screenName")
            put(MediaStore.Video.Media.IS_PENDING, 1)
        }
        val collectionUri = MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val itemUri = context.contentResolver.insert(collectionUri, contentValues)
            ?: return@withContext Result.failure(
                workDataOf(
                    KEY_DOWNLOAD_ID to downloadId,
                    KEY_ERROR_MESSAGE to "Unable to insert into MediaStore"
                )
            )
        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        val request = Request.Builder().url(url).build()
        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.failure(
                        workDataOf(
                            KEY_DOWNLOAD_ID to downloadId,
                            KEY_ERROR_MESSAGE to "ErrorCode: ${response.code}"
                        )
                    )
                }
                val body = response.body
                    ?: return@withContext Result.failure(
                        workDataOf(
                            KEY_DOWNLOAD_ID to downloadId,
                            KEY_ERROR_MESSAGE to "Response body is null"
                        )
                    )
                val totalBytes = body.contentLength()

                context.contentResolver.openOutputStream(itemUri)?.use { output ->
                    val buffer = ByteArray(16384)
                    var bytesRead = 0L
                    val input = body.byteStream()
                    while (isActive) {
                        val read = input.read(buffer)
                        if (read == -1) break
                        output.write(buffer, 0, read)
                        bytesRead += read

                        if (totalBytes > 0) {
                            val progress = ((bytesRead * 100) / totalBytes).toInt()
                            setProgress(
                                workDataOf(
                                    PROGRESS to progress,
                                    KEY_DOWNLOAD_ID to downloadId,
                                    "bytes_downloaded" to bytesRead,
                                    "total_bytes" to totalBytes
                                )
                            )
                        }
                    }
                } ?: return@withContext Result.failure(
                    workDataOf(
                        KEY_DOWNLOAD_ID to downloadId,
                        KEY_ERROR_MESSAGE to "Failed to open outputStream"
                    )
                )

                val updateValues = ContentValues().apply {
                    put(MediaStore.Video.Media.IS_PENDING, 0)
                }
                context.contentResolver.update(itemUri, updateValues, null, null)

                val finalSize = response.body!!.contentLength()
                return@withContext Result.success(
                    workDataOf(
                        KEY_DOWNLOAD_ID to downloadId,
                        FILE_URI to itemUri.toString(),
                        FILE_SIZE to finalSize
                    )
                )
            }
        } catch (e: Exception) {
            if (runAttemptCount < MAX_RETRIES) {
                return@withContext Result.retry()
            } else {
                context.contentResolver.delete(itemUri, null, null)
                return@withContext Result.failure(
                    workDataOf(
                        KEY_DOWNLOAD_ID to downloadId,
                        KEY_ERROR_MESSAGE to e.message
                    )
                )
            }
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