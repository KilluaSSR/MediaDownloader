package killua.dev.mediadownloader.download.Platforms

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfDocument
import android.net.Uri
import killua.dev.base.Model.DownloadTask
import killua.dev.base.Model.MediaType
import killua.dev.base.utils.MediaStoreHelper
import killua.dev.mediadownloader.download.BaseMediaDownloader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

class CommonMangaDownloader(
    context: Context,
    mediaHelper: MediaStoreHelper
) : BaseMediaDownloader(context, mediaHelper) {
    companion object {
        private const val TAG = "CommonMangaDownloader"
        private fun log(message: String) {
            println("$TAG: $message")
        }

        private fun logError(message: String, error: Throwable? = null) {
            println("$TAG ERROR: $message")
            error?.let {
                println("$TAG ERROR: ${it.javaClass.simpleName}: ${it.message}")
                it.printStackTrace()
            }
        }
    }
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
            log("文件名: ${task.fileName}")
            log("目标路径: ${task.destinationFolder}")
            println("开始下载任务: ${task.id}")
            val imageUrls = task.url.split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .filter { !it.startsWith("url:") }
                .distinct()
                .also { urls ->
                    log("解析得到 ${urls.size} 个URL")
                    urls.forEachIndexed { index, url ->
                        log("URL[$index]: $url")
                    }
                }

            if (imageUrls.isEmpty()) {
                logError("没有有效的图片URL")
                return@withContext Result.failure(IOException("没有有效的图片URL"))
            }

            log("开始下载漫画章节，共 ${imageUrls.size} 页")
            val bitmaps = mutableListOf<Bitmap>()

            // 顺序下载每张图片
            imageUrls.forEachIndexed { index, url ->
                try {
                    log("开始下载第 ${index + 1}/${imageUrls.size} 张图片")
                    log("下载地址: $url")

                    val startTime = System.currentTimeMillis()
                    val imageBytes = downloadSingleFile(url, headers + getHeaders(task))
                    val downloadTime = System.currentTimeMillis() - startTime
                    log("图片下载完成，大小: ${imageBytes.size / 1024}KB，耗时: ${downloadTime}ms")

                    val decodeStartTime = System.currentTimeMillis()
                    val options = BitmapFactory.Options().apply {
                        inPreferredConfig = Bitmap.Config.RGB_565
                    }

                    val bitmap = BitmapFactory.decodeByteArray(
                        imageBytes,
                        0,
                        imageBytes.size,
                        options
                    ) ?: throw IOException("图片解码失败")

                    val decodeTime = System.currentTimeMillis() - decodeStartTime
                    log("图片解码完成，尺寸: ${bitmap.width}x${bitmap.height}，耗时: ${decodeTime}ms")

                    bitmaps.add(bitmap)
                    onProgress((index + 1) * 80 / imageUrls.size)

                } catch (e: Exception) {
                    logError("第 ${index + 1} 张图片处理失败", e)
                    throw IOException("下载失败: $url", e)
                }
            }

            log("所有图片下载完成，开始生成PDF")

            // 创建PDF文件
            val uri = mediaHelper.insertMedia(
                fileName = task.fileName,
                filePath = task.destinationFolder,
                type = MediaType.PDF
            ).also { log("创建PDF文件: $it") }

            try {
                log("开始合并图片到PDF")
                createPdfFromBitmaps(bitmaps, uri) { mergeProgress ->
                    onProgress(80 + (mergeProgress * 20 / 100))
                }
                log("PDF生成完成")

                mediaHelper.markMediaAsComplete(uri)
                log("文件标记为完成")
                Result.success(uri)
            } catch (e: Exception) {
                logError("PDF处理失败", e)
                context.contentResolver.delete(uri, null, null)
                throw e
            } finally {
                log("开始清理位图资源")
                bitmaps.forEachIndexed { index, bitmap ->
                    try {
                        if (!bitmap.isRecycled) {
                            bitmap.recycle()
                            log("清理第 ${index + 1} 张图片资源成功")
                        }
                    } catch (e: Exception) {
                        logError("清理第 ${index + 1} 张图片资源失败", e)
                    }
                }
            }
        } catch (e: Exception) {
            logError("下载过程发生致命错误", e)
            Result.failure(e)
        }
    }

    private suspend fun downloadSingleFile(
        url: String,
        headers: Map<String, String>
    ): ByteArray = withContext(Dispatchers.IO) {
        val client = buildClient(headers)
        var retryCount = 0
        val maxRetries = 3

        while (retryCount < maxRetries) {
            try {
                log("开始下载文件: $url (尝试 ${retryCount + 1}/$maxRetries)")
                val startTime = System.currentTimeMillis()

                val response = client.newCall(
                    Request.Builder()
                        .url(url)
                        .apply {
                            headers.forEach { (k, v) ->
                                addHeader(k, v)
                                log("添加请求头: $k: $v")
                            }
                        }
                        .build()
                ).execute()

                if (!response.isSuccessful) {
                    logError("请求失败: HTTP ${response.code}")
                    throw IOException("下载失败 (${response.code})")
                }

                val bytes = response.body?.bytes()
                    ?: throw IOException("响应内容为空")

                val time = System.currentTimeMillis() - startTime
                log("文件下载成功，大小: ${bytes.size / 1024}KB，耗时: ${time}ms")

                return@withContext bytes

            } catch (e: Exception) {
                retryCount++
                logError("下载失败 (尝试 $retryCount/$maxRetries)", e)

                if (retryCount >= maxRetries) {
                    throw e
                }

                log("等待1秒后重试...")
                delay(1000)
            }
        }
        throw IOException("超过最大重试次数")
    }

    private suspend fun createPdfFromBitmaps(
        bitmaps: List<Bitmap>,
        uri: Uri,
        onProgress: (Int) -> Unit
    ) = withContext(Dispatchers.IO) {
        if (bitmaps.isEmpty()) {
            throw IllegalArgumentException("没有可用的图片")
        }

        val totalHeight = bitmaps.sumOf { it.height }
        val maxWidth = bitmaps.maxOf { it.width }

        val document = PdfDocument()
        try {
            val pageInfo = PdfDocument.PageInfo.Builder(maxWidth, totalHeight, 1).create()
            val page = document.startPage(pageInfo)

            var currentHeight = 0
            bitmaps.forEachIndexed { index, bitmap ->
                page.canvas.drawBitmap(bitmap, 0f, currentHeight.toFloat(), null)
                currentHeight += bitmap.height
                onProgress((index + 1) * 100 / bitmaps.size)
            }

            document.finishPage(page)

            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                document.writeTo(outputStream)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        } finally {
            document.close()
        }
    }

}