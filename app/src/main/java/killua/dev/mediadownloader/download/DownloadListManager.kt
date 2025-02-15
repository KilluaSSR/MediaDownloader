package killua.dev.mediadownloader.download

import android.net.Uri
import db.Download
import db.DownloadStatus
import killua.dev.base.Model.DownloadTask
import killua.dev.base.repository.ThumbnailRepository
import killua.dev.base.utils.FileDelete
import killua.dev.base.utils.VideoDurationRepository
import killua.dev.mediadownloader.repository.DownloadRepository
import javax.inject.Inject

class DownloadListManager @Inject constructor(
    private val downloadRepository: DownloadRepository,
    private val downloadManager: DownloadManager,
    private val thumbnailRepository: ThumbnailRepository,
    private val videoDurationRepository: VideoDurationRepository,
    private val downloadQueueManager: DownloadQueueManager,
    private val fileDelete: FileDelete
) {
    val downloadProgress = downloadManager.downloadProgress

    suspend fun observeAllDownloads() = downloadRepository.observeAllDownloads()

    suspend fun getById(downloadId: String) = downloadRepository.getById(downloadId)

    suspend fun getActiveDownloads() = downloadRepository.getActiveDownloads()

    suspend fun updateStatus(downloadId: String, status: DownloadStatus) =
        downloadRepository.updateStatus(downloadId, status)

    suspend fun insert(download: Download) = downloadRepository.insert(download)

    suspend fun deleteById(downloadId: String) = downloadRepository.deleteById(downloadId)

    suspend fun getVideoDuration(uri: Uri) = videoDurationRepository.getVideoDuration(uri)

    suspend fun getThumbnail(uri: Uri) = thumbnailRepository.getThumbnail(uri)

    suspend fun deleteFile(uri: Uri) = fileDelete.deleteFile(uri)

    suspend fun enqueueDownload(task: DownloadTask) = downloadQueueManager.enqueue(task)
}