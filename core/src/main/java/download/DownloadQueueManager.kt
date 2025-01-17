package download

import db.Download
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton


class DownloadQueueManager {
    private val maxConcurrentDownloads = 3
    private val activeDownloads = ConcurrentHashMap<String, Boolean>()
    private val pendingDownloads = ConcurrentHashMap<String, Download>()

    suspend fun enqueue(download: Download): Boolean {
        return if (activeDownloads.size < maxConcurrentDownloads) {
            activeDownloads[download.uuid] = true
            true
        } else {
            pendingDownloads[download.uuid] = download
            false
        }
    }

    fun markComplete(downloadId: String) {
        activeDownloads.remove(downloadId)
        processNextInQueue()
    }

    suspend fun getAllPendingDownloads(): List<Download> = withContext(Dispatchers.IO) {
        if (activeDownloads.size < maxConcurrentDownloads) {
            pendingDownloads.values
                .sortedWith(compareByDescending<Download> { it.status.ordinal })
                .take(maxConcurrentDownloads - activeDownloads.size)
                .onEach { download ->
                    pendingDownloads.remove(download.uuid)
                    activeDownloads[download.uuid] = true
                }
                .toList()
        } else {
            emptyList()
        }
    }

    private fun processNextInQueue() {
        if (activeDownloads.size >= maxConcurrentDownloads) return
        pendingDownloads.values
            .maxWithOrNull(compareBy<Download> { it.status.ordinal })
            ?.let { download ->
                pendingDownloads.remove(download.uuid)
                activeDownloads[download.uuid] = true
            }
    }
}