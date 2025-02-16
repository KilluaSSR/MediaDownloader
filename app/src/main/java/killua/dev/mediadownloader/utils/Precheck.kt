package killua.dev.mediadownloader.utils

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import killua.dev.mediadownloader.datastore.readApplicationUserAuth
import killua.dev.mediadownloader.datastore.readApplicationUserCt0
import killua.dev.mediadownloader.datastore.readDownloadPhotos
import killua.dev.mediadownloader.datastore.readKuaikanPassToken
import killua.dev.mediadownloader.datastore.readLofterLoginAuth
import killua.dev.mediadownloader.datastore.readLofterLoginKey
import killua.dev.mediadownloader.datastore.readOnlyWifi
import killua.dev.mediadownloader.datastore.readPixivPHPSSID
import killua.dev.mediadownloader.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

sealed class DownloadCheckError(message: String) : Exception(message) {
    object NoNetwork : DownloadCheckError("No network")
    object WifiRequired : DownloadCheckError("Wi-Fi not enabled")
    object DownloadPhotosNotEnabled: DownloadCheckError("Download Photos NOT Enabled")
    object LofterNotLoggedInError: DownloadCheckError("You need to log in your Lofter account")
    object TwitterNotLoggedInError: DownloadCheckError("You need to log in your Twitter account")
    object PixivNotLoggedInError: DownloadCheckError("You need to log in your Pixiv account")
    object KuaikanNotLoggedInError: DownloadCheckError("You need to log in your Kuaikan account")
}
class DownloadPreChecks @Inject constructor(
    @ApplicationContext private val context: Context,
    private val networkManager: NetworkManager,
    @ApplicationScope private val scope: CoroutineScope
) {

    private val _wifiOnlyFlow = context.readOnlyWifi()
        .stateIn(scope, SharingStarted.Eagerly, true)

    private val _photosDownload = context.readDownloadPhotos()
        .stateIn(scope, SharingStarted.Eagerly, true)

    private val _lofterLoggedIn = combine(
        context.readLofterLoginAuth(),
        context.readLofterLoginKey()
    ) { auth, key ->
        auth.isNotEmpty() && key.isNotEmpty()
    }.stateIn(scope, SharingStarted.Eagerly, false)

    private val _pixivLoggedIn = context.readPixivPHPSSID()
        .map { it.isNotEmpty() }
        .stateIn(scope, SharingStarted.Eagerly, false)

    private val _isKuaikanLoggedIn = context.readKuaikanPassToken().map { it.isNotEmpty() }.stateIn(scope, SharingStarted.Eagerly, false)

    private val _twitterLoggedIn = combine(
        context.readApplicationUserCt0(),
        context.readApplicationUserAuth()
    ){ ct0, auth ->
        auth.isNotEmpty() && ct0.isNotEmpty()
    }.stateIn(scope, SharingStarted.Eagerly, false)

    private val isWifiOnly get() = _wifiOnlyFlow.value
    private val isDownloadPhotosEnabled get() = _photosDownload.value
    private val isLofterLoggedIn get() = _lofterLoggedIn.value
    private val isTwitterLoggedIn get() = _twitterLoggedIn.value
    private val isPixivLoggedIn get() = _pixivLoggedIn.value
    private val isKuaikanLoggedIn get() = _isKuaikanLoggedIn.value

    fun checkTwitterLoggedIn(): Result<Unit> = when {
        isTwitterLoggedIn -> Result.success(Unit)
        else -> Result.failure(DownloadCheckError.TwitterNotLoggedInError)
    }

    fun checkPixivLoggedIn(): Result<Unit> = when {
        isPixivLoggedIn -> Result.success(Unit)
        else -> Result.failure(DownloadCheckError.PixivNotLoggedInError)
    }

    fun checkLofterLoggedIn(): Result<Unit> = when {
        isLofterLoggedIn -> Result.success(Unit)
        else -> Result.failure(DownloadCheckError.LofterNotLoggedInError)
    }

    fun checkKuaikanLoggedIn(): Result<Unit> = when{
        isKuaikanLoggedIn -> Result.success(Unit)
        else -> Result.failure(DownloadCheckError.KuaikanNotLoggedInError)
    }

    fun checkPhotosDownload(): Result<Unit> = when {
        isDownloadPhotosEnabled -> Result.success(Unit)
        else -> Result.failure(DownloadCheckError.DownloadPhotosNotEnabled)
    }

    fun canStartDownload(): Result<Unit> = when {
        !networkManager.isNetworkAvailable() ->
            Result.failure(DownloadCheckError.NoNetwork)
        isWifiOnly && !networkManager.isWifiConnected() ->
            Result.failure(DownloadCheckError.WifiRequired)
        else -> Result.success(Unit)
    }
}