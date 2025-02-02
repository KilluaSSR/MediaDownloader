package killua.dev.base.utils

import db.DownloadState
import db.DownloadStateUI
import killua.dev.base.Model.DownloadProgress

fun DownloadState.toUI(
    downloadProgress: Map<String, DownloadProgress>? = null,
    downloadId: String? = null
): DownloadStateUI = when (this) {
    is DownloadState.Downloading -> {
        val progress = downloadProgress?.get(downloadId)?.progress ?:
        if (totalSize > 0) ((downloadedSize * 100) / totalSize).toInt() else 0
        DownloadStateUI(
            progressInfo = DownloadStateUI.ProgressInfo(
                progress = progress,
                downloadedSize = downloadedSize,
                totalSize = totalSize
            )
        )
    }
    is DownloadState.Completed -> DownloadStateUI(
        completionInfo = DownloadStateUI.CompletionInfo(
            completedAt = System.currentTimeMillis(),
            fileUri = fileUri,
            fileSize = fileSize
        )
    )
    is DownloadState.Failed -> DownloadStateUI(
        failureInfo = DownloadStateUI.FailureInfo(error)
    )
    DownloadState.Pending -> DownloadStateUI()
}