package killua.dev.twitterdownloader.download

import android.content.Context
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
import killua.dev.twitterdownloader.utils.NetworkManager
import killua.dev.twitterdownloader.utils.StorageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

class DownloadManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val workManager: WorkManager,
    private val queueManager: DownloadQueueManager,
    private val storageManager: StorageManager,
    private val networkManager: NetworkManager,
) {
    private val BACKOFF_DELAY = 5_000L
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