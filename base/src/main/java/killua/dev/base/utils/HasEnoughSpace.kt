package killua.dev.base.utils

import android.content.Context
import android.os.StatFs
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject


class StorageManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun hasEnoughSpace(requiredBytes: Long): Boolean {
        val stats = StatFs(context.getExternalFilesDir(null)?.path)
        return stats.availableBytes > requiredBytes
    }
}