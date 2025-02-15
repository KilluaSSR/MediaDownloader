package killua.dev.mediadownloader.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.pdf.PdfDocument
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream

class MangaProcessor {
    private val client = OkHttpClient()

    suspend fun downloadAndMergeImages(urls: List<String>, outputPath: String) = withContext(Dispatchers.IO) {
        // 下载并解码所有图片
        val bitmaps = urls.map { url ->
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val bytes = response.body?.bytes() ?: throw Exception("Failed to download image")

            // 将字节数组解码为Bitmap
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        }

        // 计算总高度和最大宽度
        val totalHeight = bitmaps.sumOf { it.height }
        val maxWidth = bitmaps.maxOf { it.width }

        // 创建合并后的Bitmap
        val mergedBitmap = Bitmap.createBitmap(maxWidth, totalHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(mergedBitmap)

        // 拼接图片
        var currentHeight = 0
        bitmaps.forEach { bitmap ->
            canvas.drawBitmap(bitmap, 0f, currentHeight.toFloat(), null)
            currentHeight += bitmap.height
            bitmap.recycle() // 释放内存
        }

        // 创建PDF文档
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(maxWidth, totalHeight, 1).create()
        val page = document.startPage(pageInfo)

        // 将合并的图片绘制到PDF页面
        page.canvas.drawBitmap(mergedBitmap, 0f, 0f, null)
        document.finishPage(page)

        // 保存PDF文件
        val file = File(outputPath)
        document.writeTo(FileOutputStream(file))
        document.close()

        // 清理资源
        mergedBitmap.recycle()
    }
}