package killua.dev.mediadownloader.download

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import db.Download
import db.DownloadStatus
import killua.dev.mediadownloader.Model.AvailablePlatforms
import killua.dev.mediadownloader.Model.DownloadTask
import killua.dev.mediadownloader.repository.DownloadRepository
import killua.dev.mediadownloader.repository.ThumbnailRepository
import killua.dev.mediadownloader.utils.FileDelete
import killua.dev.mediadownloader.utils.VideoDurationRepository
import killua.dev.mediadownloader.utils.navigateToLink
import killua.dev.mediadownloader.utils.navigateTwitterTweet
import javax.inject.Inject

class DownloadListManager @Inject constructor(
    private val downloadRepository: DownloadRepository,
    private val downloadManager: DownloadManager,
    private val thumbnailRepository: ThumbnailRepository,
    private val videoDurationRepository: VideoDurationRepository,
    private val downloadQueueManager: DownloadQueueManager,
    private val fileDelete: FileDelete,
    @ApplicationContext private val context: Context
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

    fun handleNavigation(download: Download){
        when(download.platform) {
            AvailablePlatforms.Twitter -> {
                context.navigateTwitterTweet(
                    download.screenName,
                    download.uniqueID,
                    download.link
                )
            }

            else -> {
                context.navigateToLink(
                    download.uniqueID
                )
            }
        }
    }
}