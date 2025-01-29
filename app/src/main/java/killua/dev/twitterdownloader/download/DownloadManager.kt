package killua.dev.twitterdownloader.download

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.OnLifecycleEvent
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
import killua.dev.base.utils.StorageManager
import killua.dev.twitterdownloader.DOWNLOAD_COMPLETED_ACTION
import killua.dev.twitterdownloader.download.VideoDownloadWorker.Companion.FILE_SIZE
import killua.dev.twitterdownloader.download.VideoDownloadWorker.Companion.FILE_URI
import killua.dev.twitterdownloader.download.VideoDownloadWorker.Companion.KEY_ERROR_MESSAGE
import killua.dev.twitterdownloader.repository.DownloadRepository
import killua.dev.twitterdownloader.utils.NetworkManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class DownloadManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val workManager: WorkManager,
    private val queueManager: DownloadQueueManager,
    private val storageManager: StorageManager,
    private val networkManager: NetworkManager,
    private val downloadRepository: DownloadRepository
) {
    private val BACKOFF_DELAY = 5_000L
    init {
        observeWorkInfo()
    }
    suspend fun enqueueDownload(download: Download) {
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
                    VideoDownloadWorker.Companion.KEY_FILE_NAME to download.fileName,
                    VideoDownloadWorker.Companion.KEY_RANGE_HEADER to download.rangeHeader
                )
            )
            .build()

        if (networkManager.isNetworkAvailable() && storageManager.hasEnoughSpace(download.fileSize)) {
            queueManager.enqueue(download)
            workManager.enqueueUniqueWork(
                download.uuid,
                ExistingWorkPolicy.APPEND_OR_REPLACE,
                workRequest
            )
        }
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
                        downloadRepository.updateCompleted(downloadId, fileUri!!.toUri(), fileSize)
                        queueManager.markComplete(downloadId)
                        val intent = Intent(DOWNLOAD_COMPLETED_ACTION)
                        context.sendBroadcast(intent)
                    }
                    WorkInfo.State.RUNNING -> {
                        val downloadId = workInfo.progress.getString(VideoDownloadWorker.KEY_DOWNLOAD_ID) ?: return@forEach
                        val progress = workInfo.progress.getInt(VideoDownloadWorker.PROGRESS, 0)
                        downloadRepository.updateDownloadProgress(downloadId,progress)
                    }
                    WorkInfo.State.FAILED -> {
                        val downloadId = workInfo.outputData.getString(VideoDownloadWorker.KEY_DOWNLOAD_ID) ?: return@forEach
                        downloadRepository.updateError(downloadId, errorMessage = workInfo.outputData.getString(KEY_ERROR_MESSAGE))
                        queueManager.markComplete(downloadId)
                    }
                    else -> {}
                }
            }
        }
            .flowOn(Dispatchers.IO)
            .launchIn(GlobalScope)
        }

    fun getWorkInfoFlow() = workManager.getWorkInfosForUniqueWorkFlow("download_tag")

    fun cancelDownload(downloadId: String) {
        workManager.cancelUniqueWork(downloadId)
        queueManager.markComplete(downloadId)
    }

    fun pauseAllDownloads() {
        workManager.cancelAllWork()
    }

    suspend fun resumeAllDownloads() = withContext(Dispatchers.IO) {
        queueManager.getAllPendingDownloads().forEach { download ->
            enqueueDownload(download)
        }
    }

    fun isDownloadActive(downloadId: String): Boolean {
        return workManager.getWorkInfosForUniqueWork(downloadId)
            .get()
            ?.any { info ->
                info.state == WorkInfo.State.RUNNING ||
                        info.state == WorkInfo.State.ENQUEUED
            } == true
    }
}