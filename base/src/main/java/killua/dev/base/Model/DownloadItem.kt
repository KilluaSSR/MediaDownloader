package killua.dev.base.Model

import android.net.Uri
import db.DownloadState

abstract class DownloadItem(
    open val id: String,
    open val downloadState: DownloadState,
    open val link: String,
    open val progress: Int = 0,
    open val fileUri: Uri? = null,
    open val createdAt: Long,
    open val completedAt: Long? = null
) {
    val isCompleted: Boolean
        get() = downloadState is DownloadState.Completed
}