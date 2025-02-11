package killua.dev.base.utils

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class FileDelete @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun deleteFile(uri: Uri): Boolean {
        return try {
            val deletedRows = context.contentResolver.delete(uri, null, null)
            deletedRows > 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}