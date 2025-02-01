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
        override val progress: Int,
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

    val isInProgress: Boolean
        get() = this is Downloading

    open val progress: Int
        get() = when (this) {
            is Downloading -> progress
            is Completed -> 100
            else -> 0
        }

    fun toDownloadStatus(): DownloadStatus = when (this) {
        is Pending -> DownloadStatus.PENDING
        is Downloading -> DownloadStatus.DOWNLOADING
        is Completed -> DownloadStatus.COMPLETED
        is Failed -> DownloadStatus.FAILED
    }
}