package killua.dev.base.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.ui.Modifier
import db.DownloadStatus

fun Modifier.mediaClickable(
    context: Context,
    status: DownloadStatus,
    fileUri: Uri?,
    fileNotFoundClick: () -> Unit
) = clickable(enabled = status == DownloadStatus.COMPLETED) {
    fileUri?.let { uri ->
        try {
            context.contentResolver.openInputStream(uri)?.use {
                val openIntent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "video/*")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(openIntent)
            } ?: run {
                fileNotFoundClick()
            }
        } catch (e: Exception) {
            fileNotFoundClick()
        }
    }
}