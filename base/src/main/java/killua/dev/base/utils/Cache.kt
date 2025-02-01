package killua.dev.base.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest

fun generateCacheKeyFromUri(uriString: String): String {
    val digester = MessageDigest.getInstance("MD5")
    val hashBytes = digester.digest(uriString.toByteArray())
    return hashBytes.joinToString("") { "%02x".format(it) } + ".jpg"
}

suspend fun loadCachedThumbnailOrCreate(context: Context, videoUri: Uri): Bitmap? {
    return withContext(Dispatchers.IO) {
        val cacheKey = generateCacheKeyFromUri(videoUri.toString())
        val cacheFile = File(context.cacheDir, cacheKey)
        if (cacheFile.exists()) {
            // 磁盘缓存已存在，加载并返回
            return@withContext BitmapFactory.decodeFile(cacheFile.absolutePath)
        } else {
            // 不在缓存，创建截帧
            val retriever = MediaMetadataRetriever()
            try {
                retriever.setDataSource(context, videoUri)
                val frame = retriever.getFrameAtTime(0) // 获取第一帧
                if (frame != null) {
                    // 写入
                    FileOutputStream(cacheFile).use { fos ->
                        frame.compress(Bitmap.CompressFormat.JPEG, 80, fos)
                    }
                }
                frame
            } catch (e: Exception) {
                e.printStackTrace()
                null
            } finally {
                retriever.release()
            }
        }
    }
}