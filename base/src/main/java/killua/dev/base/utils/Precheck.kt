package killua.dev.base.utils

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import killua.dev.base.datastore.readDownloadPhotos
import killua.dev.base.datastore.readOnlyWifi
import killua.dev.base.repository.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
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
    private val wifiOnlyFlow = MutableStateFlow(true)
    private val photosDownload = MutableStateFlow(true)
    init {
        CoroutineScope(Dispatchers.IO).launch {
            context.readOnlyWifi().collect { isWifiOnly ->
                wifiOnlyFlow.value = isWifiOnly
            }
            context.readDownloadPhotos().collect{ photos ->
                photosDownload.value = photos
            }
        }
    }
    fun isDownloadPhotosEnabled() = photosDownload.value
    fun isWifiOnly() = wifiOnlyFlow.value

    fun checkPhotosDownload(): Result<Unit>{
        return when{
            isDownloadPhotosEnabled() -> Result.success(Unit)
            else -> Result.failure(Exception("Download Photos NOT Enabled"))
        }
    }
    fun canStartDownload(): Result<Unit> {
        return when {
            !networkManager.isNetworkAvailable() ->
                Result.failure(DownloadCheckError.NoNetwork)
            isWifiOnly() && !networkManager.isWifiConnected() ->
                Result.failure(DownloadCheckError.WifiRequired)
            else -> Result.success(Unit)
        }
    }
}