package download

import android.content.Context
import android.os.StatFs
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DownloadPreChecks @Inject constructor(
    @ApplicationContext private val context: Context
) {
    suspend fun hasEnoughSpace(url: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.requestMethod = "HEAD"
            val requiredBytes = connection.contentLength.toLong()

            if (requiredBytes <= 0) {
                return@withContext true
            }

            val path = context.getExternalFilesDir(null)?.path ?: return@withContext false
            val stat = StatFs(path)
            val availableBytes = stat.availableBytes

            availableBytes > requiredBytes
        } catch (e: Exception) {
            true
        }
    }
}