package killua.dev.twitterdownloader.download

import android.content.Context
import android.os.StatFs
import dagger.hilt.android.qualifiers.ApplicationContext
import killua.dev.base.datastore.readMaxConcurrentDownloads
import killua.dev.base.datastore.readMaxRetries
import killua.dev.base.datastore.readNotificationEnabled
import killua.dev.base.datastore.readOnlyWifi
import killua.dev.twitterdownloader.utils.NetworkManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject


class DownloadPreChecks @Inject constructor(
    @ApplicationContext private val context: Context,
    private val networkManager: NetworkManager
) {
    suspend fun isNotificationEnabled() = context.readNotificationEnabled().first()

    suspend fun loadMaxRetried() = context.readMaxRetries().first()

    suspend fun loadMaxConcurrentDownloads() = context.readMaxConcurrentDownloads().first()

    suspend fun isWifiOnly() = context.readOnlyWifi().first()

    suspend fun isWifiConnected() = networkManager.isWifiConnected()

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

    suspend fun canStartDownload(url: String): Result<Unit> = withContext(Dispatchers.IO) {
        when {
            !networkManager.isNetworkAvailable() ->
                Result.failure(Exception("无网络连接"))
            isWifiOnly() && !isWifiConnected() ->
                Result.failure(Exception("需要WiFi连接"))
            !hasEnoughSpace(url) ->
                Result.failure(Exception("存储空间不足"))
            else -> Result.success(Unit)
        }
    }
}