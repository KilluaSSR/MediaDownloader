package killua.dev.twitterdownloader.download

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import dagger.hilt.android.qualifiers.ApplicationContext
import db.Download
import db.DownloadStatus
import killua.dev.base.datastore.readNotificationEnabled
import killua.dev.base.utils.DownloadEventManager
import killua.dev.twitterdownloader.download.VideoDownloadWorker.Companion.FILE_SIZE
import killua.dev.twitterdownloader.download.VideoDownloadWorker.Companion.FILE_URI
import killua.dev.twitterdownloader.download.VideoDownloadWorker.Companion.KEY_ERROR_MESSAGE
import killua.dev.twitterdownloader.repository.DownloadRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import javax.inject.Inject

var setCompleted: Set<String> = setOf()
var setDownloading: Set<String> = setOf()
var setFailed: Set<String> = setOf()

class DownloadManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val workManager: WorkManager,
    private val queueManager: DownloadQueueManager,
    private val downloadRepository: DownloadRepository,
    private val downloadEventManager: DownloadEventManager,

) {
    private val _downloadProgress = MutableSharedFlow<Pair<String, Int>>(replay = 0)
    val downloadProgress = _downloadProgress.asSharedFlow()
    private val BACKOFF_DELAY = 5_000L

    private val managerScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    init {
        queueManager.onReadyToDownload = { download ->
            startWork(download)
        }
        observeWorkInfo()
    }

    suspend fun enqueueDownload(download: Download) {
        val canStartNow = queueManager.enqueue(download)
        if (canStartNow) {
            startWork(download)
        }
    }

    private fun startWork(download: Download) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresStorageNotLow(true)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<VideoDownloadWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                BACKOFF_DELAY,
                TimeUnit.MILLISECONDS
            )
            .addTag("download_tag")
            .setInputData(
                workDataOf(
                    VideoDownloadWorker.Companion.KEY_URL to download.link,
                    VideoDownloadWorker.Companion.KEY_DOWNLOAD_ID to download.uuid,
                    VideoDownloadWorker.Companion.KEY_SCREEN_NAME to download.twitterScreenName,
                    VideoDownloadWorker.Companion.KEY_NAME to download.twitterName,
                    VideoDownloadWorker.Companion.KEY_FILE_NAME to download.fileName,
                    VideoDownloadWorker.Companion.KEY_RANGE_HEADER to download.rangeHeader
                )
            )
            .build()

        // 使用 UniqueWork 避免重复
        workManager.enqueueUniqueWork(
            download.uuid,
            ExistingWorkPolicy.APPEND_OR_REPLACE,
            workRequest
        )
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun observeWorkInfo() {
        workManager.getWorkInfosByTagFlow("download_tag").onEach {workInfos ->
            workInfos.forEach { workInfo ->
                when (workInfo.state) {
                    WorkInfo.State.SUCCEEDED -> {
                        val downloadId = workInfo.outputData.getString(VideoDownloadWorker.KEY_DOWNLOAD_ID) ?: return@forEach
                        val fileUri = workInfo.outputData.getString(FILE_URI)
                        val fileSize = workInfo.outputData.getLong(FILE_SIZE, 0L)
                        if(!setCompleted.contains(downloadId)){
                            downloadRepository.updateCompleted(downloadId, fileUri!!.toUri(), fileSize)
                            queueManager.markComplete(downloadId)
                            downloadEventManager.notifyDownloadCompleted()
                        }
                        setCompleted += downloadId

                    }
                    WorkInfo.State.RUNNING -> {
                        val downloadId = workInfo.progress.getString(VideoDownloadWorker.KEY_DOWNLOAD_ID) ?: return@forEach
                        var progress = workInfo.progress.getInt(VideoDownloadWorker.PROGRESS, 0)
                        if(!setDownloading.contains(downloadId)){
                            updateMarkedDownloading(downloadId)
                        }
                        _downloadProgress.emit(downloadId to progress)
                        setDownloading += downloadId
                    }
                    WorkInfo.State.FAILED -> {
                        val downloadId = workInfo.outputData.getString(VideoDownloadWorker.KEY_DOWNLOAD_ID) ?: return@forEach
                        if(!setFailed.contains(downloadId)){
                            downloadRepository.updateError(downloadId, errorMessage = workInfo.outputData.getString(KEY_ERROR_MESSAGE))
                            queueManager.markComplete(downloadId)
                        }
                        setFailed += downloadId
                    }
                    else -> {}
                }
            }
        }
            .flowOn(Dispatchers.IO)
            .launchIn(GlobalScope)
    }


    suspend fun cancelDownload(downloadId: String) {
        workManager.cancelUniqueWork(downloadId)
        queueManager.markComplete(downloadId)
    }

    fun isDownloadActive(downloadId: String): Boolean {
        return workManager.getWorkInfosForUniqueWork(downloadId)
            .get()
            ?.any { info ->
                info.state == WorkInfo.State.RUNNING ||
                        info.state == WorkInfo.State.ENQUEUED
            } == true
    }

    private suspend fun updateDownloadProgress(downloadId: String, progress: Int) {
        downloadRepository.updateDownloadProgress(downloadId, progress)
    }

    private suspend fun updateMarkedDownloading(downloadId: String){
        downloadRepository.updateDownloadingStatus(downloadId)
    }

    /**
     * 标记下载完成
     */
    private suspend fun markDownloadCompleted(downloadId: String, fileUri: Uri, fileSize: Long) {
        downloadRepository.updateCompleted(downloadId, fileUri, fileSize)
    }

    /**
     * 标记下载失败
     */
    private suspend fun markDownloadFailed(downloadId: String, errorMessage: String) {
        downloadRepository.updateError(downloadId, errorMessage)
    }

    /**
     * 标记任务取消
     */
    private suspend fun markDownloadCancelled(downloadId: String) {
        downloadRepository.updateStatus(downloadId, DownloadStatus.PENDING)
    }
}