package killua.dev.mediadownloader.download

import android.net.Uri
import db.Download
import db.DownloadStatus
import killua.dev.mediadownloader.Model.DownloadTask
import killua.dev.mediadownloader.repository.DownloadRepository
import killua.dev.mediadownloader.repository.ThumbnailRepository
import killua.dev.mediadownloader.utils.FileDelete
import killua.dev.mediadownloader.utils.VideoDurationRepository
import javax.inject.Inject

class DownloadListManager @Inject constructor(
    private val downloadRepository: DownloadRepository,
    downloadManager: DownloadManager,
    private val thumbnailRepository: ThumbnailRepository,
    private val videoDurationRepository: VideoDurationRepository,
    private val downloadQueueManager: DownloadQueueManager,
    private val fileDelete: FileDelete
) {
    val downloadProgress = downloadManager.downloadProgress

    fun observeAllDownloads() = downloadRepository.observeAllDownloads()

    suspend fun getById(downloadId: String) = downloadRepository.getById(downloadId)

    suspend fun getActiveDownloads() = downloadRepository.getActiveDownloads()

    suspend fun updateStatus(downloadId: String, status: DownloadStatus) =
        downloadRepository.updateStatus(downloadId, status)

    suspend fun insert(download: Download) = downloadRepository.insert(download)

    suspend fun deleteById(downloadId: String) = downloadRepository.deleteById(downloadId)

    suspend fun getVideoDuration(uri: Uri) = videoDurationRepository.getVideoDuration(uri)

    suspend fun getThumbnail(uri: Uri) = thumbnailRepository.getThumbnail(uri)

    fun deleteFile(uri: Uri) = fileDelete.deleteFile(uri)

    suspend fun enqueueDownload(task: DownloadTask) = downloadQueueManager.enqueue(task)
}