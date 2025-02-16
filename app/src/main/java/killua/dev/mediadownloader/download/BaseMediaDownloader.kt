package killua.dev.mediadownloader.download

import android.content.Context
import android.net.Uri
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

            val finalHeaders = headers + getHeaders(task)
            val client = buildClient(finalHeaders)

            client.newCall(
                Request.Builder()
                    .url(task.url)
                    .apply { finalHeaders.forEach { (k, v) -> addHeader(k, v) } }
                    .build()
            ).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IllegalStateException("Network error: ${response.code}")
                }

                val body = response.body ?: throw IllegalStateException("Response body is null")
                val totalBytes = body.contentLength()

                context.contentResolver.openOutputStream(itemUri)?.use { output ->
                    body.byteStream().buffered().use { input ->
                        var bytesRead = 0L
                        val buffer = ByteArray(8192)
                        var read: Int

                        while (input.read(buffer).also { read = it } != -1) {
                            output.write(buffer, 0, read)
                            output.flush()
                            bytesRead += read
                            val progress = if (totalBytes > 0) ((bytesRead * 100) / totalBytes).toInt() else 0
                            onProgress(progress)
                        }
                    }
                }

                mediaHelper.markMediaAsComplete(itemUri)
                Result.success(itemUri)
            }
        } catch (e: Exception) {
            itemUri?.let { context.contentResolver.delete(it, null, null) }
            Result.failure(e)
        }
    }
}