package killua.dev.twitterdownloader.download

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import db.Download
import killua.dev.base.datastore.readMaxConcurrentDownloads
import killua.dev.base.repository.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject


class DownloadQueueManager @Inject constructor(
   private val settingsRepository: SettingsRepository,
    @ApplicationContext private val context: Context
) {
    private val activeDownloads = ConcurrentHashMap<String, Boolean>()
    private val pendingDownloads = ConcurrentHashMap<String, Download>()
    var onReadyToDownload: ((Download) -> Unit)? = null

    suspend fun enqueue(download: Download): Boolean {
        val maxConcurrentDownloads = context.readMaxConcurrentDownloads().first()
        return if (activeDownloads.size < maxConcurrentDownloads) {
            activeDownloads[download.uuid] = true
            true
        } else {
            pendingDownloads[download.uuid] = download
            false
        }
    }

    suspend fun markComplete(downloadId: String) {
        activeDownloads.remove(downloadId)
        processNextInQueue()
    }

//    suspend fun getAllPendingDownloads(): List<Download> = withContext(Dispatchers.IO) {
//        val maxConcurrentDownloads = context.readMaxConcurrentDownloads().first()
//        if (activeDownloads.size < maxConcurrentDownloads) {
//            val limit = maxConcurrentDownloads - activeDownloads.size
//            val available = pendingDownloads.values
//                .sortedBy { it.status.ordinal }
//                .take(limit)
//                .toList()
//            available.forEach {
//                pendingDownloads.remove(it.uuid)
//                activeDownloads[it.uuid] = true
//            }
//            available
//        } else {
//            emptyList()
//        }
//    }

    private suspend fun processNextInQueue() {
        val maxConcurrentDownloads = context.readMaxConcurrentDownloads().first()
        if (activeDownloads.size >= maxConcurrentDownloads) return
        val next = pendingDownloads.values.firstOrNull() ?: return
        pendingDownloads.remove(next.uuid)
        activeDownloads[next.uuid] = true
        onReadyToDownload?.invoke(next)
    }
}