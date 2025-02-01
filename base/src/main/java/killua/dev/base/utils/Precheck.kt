package killua.dev.base.utils

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import killua.dev.base.datastore.readOnlyWifi
import killua.dev.base.repository.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

sealed class DownloadCheckError(message: String) : Exception(message) {
    object NoNetwork : DownloadCheckError("No network")
    object WifiRequired : DownloadCheckError("Wi-Fi not enabled")
}
class DownloadPreChecks @Inject constructor(
    @ApplicationContext private val context: Context,
    private val networkManager: NetworkManager
) {
    suspend fun isWifiOnly() = context.readOnlyWifi().first()
    suspend fun canStartDownload(): Result<Unit> = withContext(Dispatchers.IO) {
        when {
            !networkManager.isNetworkAvailable() ->
                Result.failure(DownloadCheckError.NoNetwork)
            isWifiOnly() && !networkManager.isWifiConnected() ->
                Result.failure(DownloadCheckError.WifiRequired)
            else -> Result.success(Unit)
        }
    }
}