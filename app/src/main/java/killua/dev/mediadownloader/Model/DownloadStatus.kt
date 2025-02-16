package db

import android.net.Uri

enum class DownloadStatus {
    PENDING,
    DOWNLOADING,
    COMPLETED,
    FAILED
}

sealed class DownloadState {
    object Pending : DownloadState()
    data class Downloading(
        val downloadedSize: Long = 0,
        val totalSize: Long = 0
    ) : DownloadState()

    data class Completed(
        val fileUri: Uri,
        val fileSize: Long
    ) : DownloadState()

    data class Failed(
        val error: String
    ) : DownloadState()

    fun toDownloadStatus(): DownloadStatus = when (this) {
        is Pending -> DownloadStatus.PENDING
        is Downloading -> DownloadStatus.DOWNLOADING
        is Completed -> DownloadStatus.COMPLETED
        is Failed -> DownloadStatus.FAILED
    }
}

data class DownloadStateUI(
    val progressInfo: ProgressInfo? = null,
    val completionInfo: CompletionInfo? = null,
    val failureInfo: FailureInfo? = null
) {
    data class ProgressInfo(
        val progress: Int,
        val downloadedSize: Long,
        val totalSize: Long
    )

    data class CompletionInfo(
        val completedAt: Long,
        val fileUri: Uri,
        val fileSize: Long
    )

    data class FailureInfo(
        val error: String
    )
}