package killua.dev.mediadownloader.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.ui.Modifier
import db.DownloadStatus
import killua.dev.mediadownloader.Model.MediaType

fun Modifier.mediaClickable(
    context: Context,
    status: DownloadStatus,
    fileUri: Uri?,
    fileType: MediaType,
    fileNotFoundClick: () -> Unit
) = clickable(enabled = status == DownloadStatus.COMPLETED) {
    fileUri?.let { uri ->
        try {
            context.contentResolver.openInputStream(uri)?.use {
                val mimeType = when (fileType) {
                    MediaType.VIDEO -> "video/*"
                    MediaType.PHOTO -> "image/*"
                    MediaType.PDF -> "application/pdf"
                    MediaType.GIF -> "image/gif"
                }

                val openIntent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, mimeType)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                if (openIntent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(openIntent)
                } else {
                    val chooserIntent = Intent.createChooser(openIntent, "请选择打开方式")
                    context.startActivity(chooserIntent)
                }
            } ?: run {
                fileNotFoundClick()
            }
        } catch (e: Exception) {
            fileNotFoundClick()
        }
    }
}