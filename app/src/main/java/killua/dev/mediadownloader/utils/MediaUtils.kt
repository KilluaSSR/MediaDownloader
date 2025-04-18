package killua.dev.mediadownloader.utils

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.LruCache
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface MeidaDurationRepository {
    suspend fun getMediaDuration(uri: Uri): Long
}
class MediaDurationRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : MeidaDurationRepository {
    private val durationCache = LruCache<String, Long>(100)
    private val retrieverPool = Semaphore(4)

    override suspend fun getMediaDuration(uri: Uri): Long = withContext(Dispatchers.IO) {
        val key = uri.toString()
        durationCache.get(key)?.let { return@withContext it }

        try {
            retrieverPool.withPermit {
                val retriever = MediaMetadataRetriever()
                retriever.use { r ->
                    r.setDataSource(context, uri)
                    val durationMs = r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong() ?: 0L
                    val durationSeconds = durationMs / 1000
                    durationCache.put(key, durationSeconds)
                    durationSeconds
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            0L
        }
    }
}

private suspend fun <T> Semaphore.withPermit(block: suspend () -> T): T {
    acquire()
    try {
        return block()
    } finally {
        release()
    }
}