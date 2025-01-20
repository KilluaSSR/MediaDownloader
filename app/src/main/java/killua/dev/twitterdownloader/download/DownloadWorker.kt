package killua.dev.twitterdownloader.download
import android.annotation.SuppressLint
import android.content.Context
import androidx.core.net.toUri
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import killua.dev.twitterdownloader.core.utils.FileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
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
        private const val MAX_RETRIES = 3
    }

    @SuppressLint("RestrictedApi")
    override suspend fun doWork(): Result {
        val url = inputData.getString(KEY_URL) ?: return Result.failure()
        val downloadId = inputData.getString(KEY_DOWNLOAD_ID) ?: return Result.failure()
        val screenName = inputData.getString(KEY_SCREEN_NAME) ?: return Result.failure()
        val fileName = inputData.getString(KEY_FILE_NAME) ?: return Result.failure()
        val rangeHeader = inputData.getString(KEY_RANGE_HEADER)
        return try {
            val outputFile = File(FileUtils.getUserVideoDir(context, screenName), fileName)
            val startByte = if (rangeHeader != null && outputFile.exists()) {
                outputFile.length()
            } else 0L

            val result = downloadWithResume(url, outputFile, startByte, downloadId)
            if (result is Result.Failure && runAttemptCount < MAX_RETRIES) {
                Result.retry()
            } else {
                result
            }
        } catch (e: Exception) {
            if (runAttemptCount < MAX_RETRIES) {
                Result.retry()
            } else {
                Result.failure(workDataOf(
                    KEY_DOWNLOAD_ID to downloadId,
                    "error" to (e.message ?: "Failed to download")
                ))
            }
        }
    }

    private suspend fun downloadWithResume(
        url: String,
        outputFile: File,
        startByte: Long,
        downloadId: String
    ): Result = withContext(Dispatchers.IO) {
        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val original = chain.request()
                val builder = original.newBuilder()
                if (startByte > 0) {
                    builder.addHeader("Range", "bytes=$startByte-")
                }
                chain.proceed(builder.build())
            }
            .build()

        val request = Request.Builder().url(url).build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.failure(workDataOf(
                        KEY_DOWNLOAD_ID to downloadId,
                        "error" to "ErrorCode: ${response.code}"
                    ))
                }

                val body = response.body ?: return@withContext Result.failure(
                    workDataOf(
                        KEY_DOWNLOAD_ID to downloadId,
                        "error" to "Response body is null"
                    )
                )

                val totalBytes = body.contentLength() + startByte

                FileOutputStream(outputFile, startByte > 0).use { output ->
                    var bytesRead = startByte
                    val buffer = ByteArray(16384)
                    val input = body.byteStream()

                    while (isActive) {
                        val read = input.read(buffer)
                        if (read == -1) break
                        output.write(buffer, 0, read)
                        bytesRead += read

                        val progress = ((bytesRead * 100) / totalBytes).toInt()
                        setProgress(workDataOf(
                            PROGRESS to progress,
                            KEY_DOWNLOAD_ID to downloadId,
                            "bytes_downloaded" to bytesRead,
                            "total_bytes" to totalBytes
                        ))
                    }
                }

                Result.success(workDataOf(
                    KEY_DOWNLOAD_ID to downloadId,
                    "file_uri" to outputFile.toUri().toString(),
                    "file_size" to outputFile.length()
                ))
            }
        } catch (e: Exception) {
            if (runAttemptCount < MAX_RETRIES) Result.retry()
            else Result.failure(
                workDataOf(
                    KEY_DOWNLOAD_ID to downloadId,
                    "error" to e.message
                )
            )
        }
    }
}

