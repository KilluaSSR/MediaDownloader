package killua.dev.mediadownloader.Model

import android.net.Uri
import db.Download
import db.DownloadState
import db.DownloadStatus

data class DownloadedItem(
    override val id: String,
    val screenName: String,
    val name: String,
    val type: AvailablePlatforms,
    override val downloadState: DownloadState,
    override val link: String,
    override val progress: Int = 0,
    override val fileUri: Uri? = null,
    override val fileType: MediaType,
    override val createdAt: Long,
    override val completedAt: Long? = null
) : DownloadItem(
    id = id,
    downloadState = downloadState,
    link = link,
    progress = progress,
    fileUri = fileUri,
    fileType = fileType,
    createdAt = createdAt,
    completedAt = completedAt
) {
    companion object {
        fun fromDownload(download: Download) = DownloadedItem(
            id = download.uuid,
            screenName = download.screenName ?: "",
            name = download.name ?: "",
            type = download.type,
            downloadState = when (download.status) {
                DownloadStatus.PENDING -> DownloadState.Pending
                DownloadStatus.DOWNLOADING -> DownloadState.Downloading()
                DownloadStatus.COMPLETED -> DownloadState.Completed(
                    download.fileUri ?: Uri.EMPTY,
                    download.fileSize
                )
                DownloadStatus.FAILED -> DownloadState.Failed(
                    download.errorMessage ?: "Unknown error"
                )
            },
            fileType = MediaType.fromString(download.fileType),
            link = download.link ?: "",
            progress = download.progress,
            fileUri = download.fileUri,
            createdAt = download.createdAt,
            completedAt = download.completedAt
        )
    }
}