package killua.dev.mediadownloader.repository

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.LruCache
import dagger.hilt.android.qualifiers.ApplicationContext
import killua.dev.mediadownloader.utils.ThumbnailHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ThumbnailRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val thumbnailCache = LruCache<String, Bitmap>(100)

    suspend fun getThumbnail(uri: Uri): Bitmap? = withContext(Dispatchers.IO) {
        val key = uri.toString()
        thumbnailCache.get(key) ?: loadAndCacheThumbnail(uri, key)
    }

    private suspend fun loadAndCacheThumbnail(uri: Uri, key: String): Bitmap? {
        return try {
            val bitmap = ThumbnailHelper.loadCachedThumbnailOrCreate(context, uri)
            bitmap?.let { thumbnailCache.put(key, it) }
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}