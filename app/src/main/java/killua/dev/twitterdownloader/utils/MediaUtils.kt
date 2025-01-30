package killua.dev.twitterdownloader.utils

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.LruCache
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface VideoDurationRepository {
    suspend fun getVideoDuration(uri: Uri): Long
}
class VideoDurationRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : VideoDurationRepository {
    private val durationCache = LruCache<String, Long>(100)

    override suspend fun getVideoDuration(uri: Uri): Long = withContext(Dispatchers.IO) {
        val key = uri.toString()
        durationCache.get(key)?.let { return@withContext it }

        try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, uri)

            val durationMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong() ?: 0L
            val durationSeconds = durationMs / 1000

            retriever.release()
            durationCache.put(key, durationSeconds)
            durationSeconds
        } catch (e: Exception) {
            e.printStackTrace()
            0L
        }
    }
}