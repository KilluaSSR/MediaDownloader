package killua.dev.base.Model

import android.net.Uri
import db.DownloadState

data class DownloadUserInfoUI(
    val name: String,
    val username: String
)

interface DownloadInfo {
    val id: String
    val userInfoUI: DownloadUserInfoUI
    val createdAt: Long
    val downloadState: DownloadState
    val fileUri: Uri?
}