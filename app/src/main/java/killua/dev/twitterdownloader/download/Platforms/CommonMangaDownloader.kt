package killua.dev.twitterdownloader.download.Platforms

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.pdf.PdfDocument
import android.net.Uri
import killua.dev.base.Model.DownloadTask
import killua.dev.base.Model.MediaType
import killua.dev.base.utils.MediaStoreHelper
import killua.dev.twitterdownloader.download.BaseMediaDownloader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

class CommonMangaDownloader(
    context: Context,
    mediaHelper: MediaStoreHelper
) : BaseMediaDownloader(context, mediaHelper) {

    override fun buildClient(headers: Map<String, String>): OkHttpClient {
        return OkHttpClient.Builder()
            .followRedirects(true)
            .build()
    }

    override fun getHeaders(task: DownloadTask): Map<String, String> {
        return mapOf(
            "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"
        )
    }

    override suspend fun download(
        task: DownloadTask,
        headers: Map<String, String>,
        onProgress: (Int) -> Unit
    ): Result<Uri> = withContext(Dispatchers.IO) {
        try {
            // 假设task.url是包含所有图片URL的列表
            val imageUrls = task.url.split(",")
            val totalImages = imageUrls.size
            val bitmaps = mutableListOf<Bitmap>()

            // 下载所有图片
            imageUrls.forEachIndexed { index, url ->
                val response = buildClient(headers).newCall(
                    Request.Builder().url(url).build()
                ).execute()

                if (!response.isSuccessful) throw IOException("Failed to download: $url")

                val bitmap = BitmapFactory.decodeStream(response.body?.byteStream())
                bitmaps.add(bitmap)

                // 更新总体下载进度
                onProgress((index + 1) * 100 / totalImages)
            }

            // 计算总高度和最大宽度
            val totalHeight = bitmaps.sumOf { it.height }
            val maxWidth = bitmaps.maxOf { it.width }

            // 创建合并的Bitmap
            val mergedBitmap = Bitmap.createBitmap(maxWidth, totalHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(mergedBitmap)

            // 拼接图片
            var currentHeight = 0
            bitmaps.forEach { bitmap ->
                canvas.drawBitmap(bitmap, 0f, currentHeight.toFloat(), null)
                currentHeight += bitmap.height
                bitmap.recycle()
            }

            // 创建PDF文件
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(maxWidth, totalHeight, 1).create()
            val page = pdfDocument.startPage(pageInfo)

            page.canvas.drawBitmap(mergedBitmap, 0f, 0f, null)
            pdfDocument.finishPage(page)

            // 保存PDF文件
            val uri = mediaHelper.insertMedia(
                fileName = task.fileName,
                filePath = task.destinationFolder,
                type = MediaType.PDF
            )

            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                pdfDocument.writeTo(outputStream)
            }

            pdfDocument.close()
            mergedBitmap.recycle()

            mediaHelper.markMediaAsComplete(uri)
            Result.success(uri)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}