package killua.dev.twitterdownloader.Model

import android.net.Uri
import db.Download
import db.DownloadState
import db.DownloadStatus
import killua.dev.base.Model.DownloadItem

data class TwitterDownloadItem(
    override val id: String,
    val twitterScreenName: String,
    val twitterName: String,
    override val downloadState: DownloadState,
    override val link: String,
    override val progress: Int = 0,
    override val fileUri: Uri? = null,
    override val createdAt: Long,
    override val completedAt: Long? = null
) : DownloadItem(
    id = id,
    downloadState = downloadState,
    link = link,
    progress = progress,
    fileUri = fileUri,
    createdAt = createdAt,
    completedAt = completedAt
) {
    companion object {
        fun fromDownload(download: Download) = TwitterDownloadItem(
            id = download.uuid,
            twitterScreenName = download.twitterScreenName ?: "",
            twitterName = download.twitterName ?: "",
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
            link = download.link ?: "",
            progress = download.progress,
            fileUri = download.fileUri,
            createdAt = download.createdAt,
            completedAt = download.completedAt
        )
    }
}