package killua.dev.mediadownloader.download

import android.content.Context
import android.net.Uri
import android.util.Log
import killua.dev.mediadownloader.Model.DownloadTask
import killua.dev.mediadownloader.utils.MediaStoreHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

abstract class BaseMediaDownloader(
    protected val context: Context,
    protected val mediaHelper: MediaStoreHelper
) : MediaDownloader {
    companion object {
        private const val TAG = "BaseMediaDownloader"
    }
    protected abstract fun buildClient(headers: Map<String, String>): OkHttpClient
    protected abstract fun getHeaders(task: DownloadTask): Map<String, String>

    override suspend fun download(
        task: DownloadTask,
        headers: Map<String, String>,
        onProgress: (Int) -> Unit
    ): Result<Uri> = withContext(Dispatchers.IO) {
        var itemUri: Uri? = null
        try {
            itemUri = mediaHelper.insertMedia(
                fileName = task.fileName,
                filePath = task.destinationFolder,
                type = task.type
            )
            Log.d(TAG, "Starting download for ${task.url}")
            Log.d(TAG, "File name: ${task.fileName}")
            Log.d(TAG, "Destination: ${task.destinationFolder}")
            Log.d(TAG, "Media type: ${task.type}")
            Log.d(TAG, "Media URI created: $itemUri")
            val finalHeaders = headers + getHeaders(task)
            val client = buildClient(finalHeaders)
            Log.d(TAG, "Request headers: $finalHeaders")
            Log.d(TAG, "OkHttpClient built with configuration: ${client.connectionPool}")

            client.newCall(
                Request.Builder()
                    .url(task.url)
                    .apply { finalHeaders.forEach { (k, v) -> addHeader(k, v) } }
                    .build()
            ).execute().use { response ->
                Log.d(TAG, "Response code: ${response.code}")
                Log.d(TAG, "Response message: ${response.message}")
                Log.d(TAG, "Response headers: ${response.headers}")
                if (!response.isSuccessful) {
                    throw IllegalStateException("Network error: ${response.code}")
                }

                val body = response.body ?: throw IllegalStateException("Response body is null")
                val totalBytes = body.contentLength()
                Log.d(TAG, "Total bytes to download: $totalBytes")

                context.contentResolver.openOutputStream(itemUri)?.use { output ->
                    body.byteStream().buffered().use { input ->
                        Log.d(TAG, "Input stream opened successfully")
                        var bytesRead = 0L
                        val buffer = ByteArray(8192)
                        var read: Int

                        while (input.read(buffer).also { read = it } != -1) {
                            output.write(buffer, 0, read)
                            output.flush()
                            bytesRead += read
                            val progress = if (totalBytes > 0) ((bytesRead * 100) / totalBytes).toInt() else 0
                            Log.v(TAG, "Downloaded: $bytesRead / $totalBytes bytes ($progress%)")
                            onProgress(progress)
                        }
                    }
                }
                Log.d(TAG, "Marking media as complete: $itemUri")
                mediaHelper.markMediaAsComplete(itemUri)
                Log.d(TAG, "Download successfully completed")
                Result.success(itemUri)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Download failed", e)
            Log.e(TAG, "Error details: ${e.message}")
            e.cause?.let { Log.e(TAG, "Caused by: ${it.message}") }

            if (itemUri != null) {
                Log.d(TAG, "Cleaning up failed download: $itemUri")
                context.contentResolver.delete(itemUri, null, null)
            }

            Result.failure(e)
        }
    }
}