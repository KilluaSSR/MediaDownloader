package killua.dev.twitterdownloader.download

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import killua.dev.base.Model.DownloadTask
import killua.dev.base.datastore.readMaxConcurrentDownloads
import killua.dev.base.di.ApplicationScope
import killua.dev.twitterdownloader.repository.DownloadRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.LinkedList
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class DownloadQueueManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: DownloadRepository,
    @ApplicationScope private val scope: CoroutineScope
) {

    private val maxConcurrentDownloads = MutableStateFlow(3)
    private val activeDownloads = ConcurrentHashMap<String, Boolean>()
    private val pendingDownloads = LinkedList<DownloadTask>()


    init {
        scope.launch{
            maxConcurrentDownloads.value = context.readMaxConcurrentDownloads().first()
        }
    }

    var onDownloadStart: ((DownloadTask) -> Unit)? = null
    var onDownloadComplete: ((DownloadTask, Uri) -> Unit)? = null
    var onDownloadFailed: ((DownloadTask, String) -> Unit)? = null

    suspend fun enqueue(downloadTask: DownloadTask): Boolean {
            return if (activeDownloads.size < maxConcurrentDownloads.value) {
                startDownload(downloadTask)
                true
            } else {
                pendingDownloads.add(downloadTask)
                false
            }
    }

    private suspend fun startDownload(downloadTask: DownloadTask) {
        activeDownloads[downloadTask.id] = true
        updateMarkedDownloading(downloadTask)
        onDownloadStart?.invoke(downloadTask)
    }

    private suspend fun updateMarkedDownloading(downloadTask: DownloadTask){
        repository.updateDownloadingStatus(downloadTask.id)
    }

    suspend fun markComplete(downloadTask: DownloadTask, fileUri: Uri) {
        activeDownloads.remove(downloadTask.id)
        onDownloadComplete?.invoke(downloadTask, fileUri)
        processNextInQueue()
    }

    suspend fun markFailed(downloadTask: DownloadTask, error: String) {
        activeDownloads.remove(downloadTask.id)
        onDownloadFailed?.invoke(downloadTask, error)
        processNextInQueue()
    }

    private suspend fun processNextInQueue() {
        if (activeDownloads.size >= maxConcurrentDownloads.value) return
        val nextTask = pendingDownloads.poll() ?: return
        startDownload(nextTask)
    }
}