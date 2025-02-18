package killua.dev.mediadownloader.utils

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import dagger.hilt.android.qualifiers.ApplicationContext
import killua.dev.mediadownloader.Model.AvailablePlatforms
import killua.dev.mediadownloader.Model.MediaType
import java.io.IOException
import javax.inject.Inject

class FileUtils @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun writeTextToFile(
        text: String,
        fileName: String,
        mediaType: MediaType = MediaType.TXT,
        platform: AvailablePlatforms
    ): Uri? {
        val values = ContentValues().apply {
            val path = "${mediaType.buildPath(platform)}/$fileName.${mediaType.extension}"
            put(MediaStore.MediaColumns.DISPLAY_NAME, "$fileName.${mediaType.extension}")
            put(MediaStore.MediaColumns.MIME_TYPE, mediaType.mimeType)
            put(MediaStore.MediaColumns.RELATIVE_PATH, path)
        }

        return try {
            val uri = context.contentResolver.insert(mediaType.collection, values)
            uri?.let { outputUri ->
                context.contentResolver.openOutputStream(outputUri)?.use { outputStream ->
                    outputStream.write(text.toByteArray())
                }
            }
            uri
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
}