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

object ThumbnailHelper{
    private const val MAX_THUMBNAIL_SIZE = 512

    fun generateCacheKey(uriString: String): String {
        val digester = MessageDigest.getInstance("MD5")
        val hashBytes = digester.digest(uriString.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) } + ".jpg"
    }

    suspend fun loadCachedThumbnailOrCreate(context: Context, uri: Uri): Bitmap? {
        return withContext(Dispatchers.IO) {
            val mimeType = context.contentResolver.getType(uri)
            val cacheKey = generateCacheKey(uri.toString())
            val cacheFile = File(context.cacheDir, cacheKey)

            if (cacheFile.exists()) {
                return@withContext loadFromCache(cacheFile)
            }

            when {
                mimeType?.startsWith("video/") == true -> createVideoThumbnail(context, uri, cacheFile)
                mimeType?.startsWith("image/") == true -> createImageThumbnail(context, uri, cacheFile)

                else -> null
            }
        }
    }

    private fun loadFromCache(cacheFile: File): Bitmap? {
        return try {
            BitmapFactory.decodeFile(cacheFile.absolutePath)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun createVideoThumbnail(context: Context, uri: Uri, cacheFile: File): Bitmap? {
        return MediaMetadataRetriever().use { retriever ->
            try {
                retriever.setDataSource(context, uri)
                val frame = retriever.getFrameAtTime(0)
                frame?.let {
                    val resizedFrame = resizeBitmap(it)
                    saveThumbnail(resizedFrame, cacheFile)
                    resizedFrame
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    private fun createImageThumbnail(context: Context, uri: Uri, cacheFile: File): Bitmap? {
        return try {
            context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                BitmapFactory.decodeFileDescriptor(pfd.fileDescriptor)?.let { bitmap ->
                    val resizedBitmap = resizeBitmap(bitmap)
                    saveThumbnail(resizedBitmap, cacheFile)
                    resizedBitmap
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun resizeBitmap(source: Bitmap): Bitmap {
        val ratio = minOf(
            MAX_THUMBNAIL_SIZE.toFloat() / source.width,
            MAX_THUMBNAIL_SIZE.toFloat() / source.height
        )

        return if (ratio < 1) {
            val newWidth = (source.width * ratio).toInt()
            val newHeight = (source.height * ratio).toInt()
            Bitmap.createScaledBitmap(source, newWidth, newHeight, true)
        } else {
            source
        }
    }

    private fun saveThumbnail(bitmap: Bitmap, cacheFile: File) {
        try {
            FileOutputStream(cacheFile).use { fos ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}