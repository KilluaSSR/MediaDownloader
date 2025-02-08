package killua.dev.base.utils

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.LruCache
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface VideoDurationRepository {
    suspend fun getVideoDuration(uri: Uri): Long
}
class VideoDurationRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : VideoDurationRepository {
    private val durationCache = LruCache<String, Long>(100)
    private val retrieverPool = Semaphore(4) // 限制同时进行的元数据提取数量

    override suspend fun getVideoDuration(uri: Uri): Long = withContext(Dispatchers.IO) {
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