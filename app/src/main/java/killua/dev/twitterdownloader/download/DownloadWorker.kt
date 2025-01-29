package killua.dev.twitterdownloader.download

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.provider.MediaStore
import androidx.core.net.toUri
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import kotlinx.coroutines.Dispatchers
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
        const val KEY_FILE_NAME = "file_name"
        const val PROGRESS = "progress"
        const val KEY_RANGE_HEADER = "range_header"
        const val DOWNLOAD_TAG = "download_tag"
        const val KEY_ERROR_MESSAGE = "error"
        const val FILE_URI = "file_uri"
        const val FILE_SIZE = "file_size"
        private const val MAX_RETRIES = 3
    }

    @SuppressLint("RestrictedApi")
    override suspend fun doWork(): Result {
        val url = inputData.getString(KEY_URL) ?: return Result.failure()
        val downloadId = inputData.getString(KEY_DOWNLOAD_ID) ?: return Result.failure()
        val screenName = inputData.getString(KEY_SCREEN_NAME) ?: return Result.failure()
        val fileName = inputData.getString(KEY_FILE_NAME) ?: return Result.failure()
        return try {
            val result = downloadVideoToMediaStore(url, fileName, screenName, downloadId)
            if (result is Result.Failure && runAttemptCount < MAX_RETRIES) {
                Result.retry()
            } else {
                result
            }
        } catch (e: Exception) {
            if (runAttemptCount < MAX_RETRIES) {
                Result.retry()
            } else {
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
}