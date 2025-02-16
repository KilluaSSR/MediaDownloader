package killua.dev.mediadownloader.utils

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import killua.dev.mediadownloader.Model.MediaType

class MediaStoreHelper(private val context: Context) {
    fun insertMedia(
        fileName: String,
        filePath: String,
        type: MediaType
    ): Uri {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, type.mimeType)
            put(MediaStore.MediaColumns.RELATIVE_PATH, filePath)
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }

        return context.contentResolver.insert(type.collection, contentValues)
            ?: throw IllegalStateException("MediaStore insert failed")
    }

    fun markMediaAsComplete(uri: Uri) {
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.IS_PENDING, 0)
        }
        context.contentResolver.update(uri, values, null, null)
    }
}