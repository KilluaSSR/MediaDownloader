package killua.dev.twitterdownloader.download

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import db.Download
import killua.dev.base.datastore.readMaxConcurrentDownloads
import killua.dev.base.repository.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
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
    private val maxDownloadsFlow = MutableStateFlow(3)

    init {
        CoroutineScope(Dispatchers.IO).launch {
            context.readMaxConcurrentDownloads()
                .collect { maxDownloads ->
                    maxDownloadsFlow.value = maxDownloads
                }
        }
    }

    fun enqueue(download: Download): Boolean {
        val maxConcurrentDownloads = maxDownloadsFlow.value
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

    private fun processNextInQueue() {
        val maxConcurrentDownloads = maxDownloadsFlow.value
        if (activeDownloads.size >= maxConcurrentDownloads) return
        val next = pendingDownloads.values.firstOrNull() ?: return
        pendingDownloads.remove(next.uuid)
        activeDownloads[next.uuid] = true
        onReadyToDownload?.invoke(next)
    }
}