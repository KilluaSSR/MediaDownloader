package Model

import ui.UIIntent

sealed interface DownloadUIIntent: UIIntent {
    data class StartDownload(val tweetId: String) : DownloadUIIntent
    data class ResumeDownload(val downloadId: String) : DownloadUIIntent
    data class PauseDownload(val downloadId: String) : DownloadUIIntent
    data class CancelDownload(val downloadId: String) : DownloadUIIntent
    data class DeleteDownload(val downloadId: String) : DownloadUIIntent
}