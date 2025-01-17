package Model

import android.net.Uri
import db.Download
import db.DownloadState
import db.DownloadStatus

data class DownloadItem(
    val id: String,
    val twitterScreenName: String,
    val twitterName: String,
    val downloadState: DownloadState,
    val link: String,
    val progress: Int = 0,
    val fileUri: Uri? = null,
    val createdAt: Long,
    val completedAt: Long? = null
) {
    val isCompleted: Boolean
        get() = downloadState is DownloadState.Completed

    val statusText: String
        get() = when(downloadState) {
            is DownloadState.Pending -> "等待下载"
            is DownloadState.Downloading -> "下载中 ${downloadState.progress}%"
            is DownloadState.Completed -> "已完成"
            is DownloadState.Failed -> "下载失败"
        }

    companion object {
        fun fromDownload(download: Download) = DownloadItem(
            id = download.uuid,
            twitterScreenName = download.twitterScreenName ?: "",
            twitterName = download.twitterName ?: "",
            downloadState = when(download.status) {
                DownloadStatus.PENDING -> DownloadState.Pending
                DownloadStatus.DOWNLOADING -> DownloadState.Downloading(
                    download.progress
                )
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