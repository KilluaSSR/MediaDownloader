package killua.dev.mediadownloader.datastore
import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import killua.dev.mediadownloader.ui.theme.ThemeMode
import kotlinx.coroutines.flow.map

const val TWITTER_AUTHOR_SUBSCRIPTION_PREFIX = "twitter_author_subscription_"
val SECURE_MY_DOWNLOAD = booleanPreferencesKey("secure_my_download")
// Twitter Userdata keys
val APPLICATION_USER_SCREENNAME_KEY =
    stringPreferencesKey("app_user_screenname")
val APPLICATION_USER_NAME_KEY =
    stringPreferencesKey("app_user_name")
val APPLICATION_USER_USERCT0_KEY =
    stringPreferencesKey("app_user_userct0")
val APPLICATION_USER_USERAUTH_KEY =
    stringPreferencesKey("app_user_userauth")
val APPLICATION_USER_ID =
    stringPreferencesKey("app_user_id")
// Lofter Userdata keys

val LOFTER_LOGIN_KEY = stringPreferencesKey("lofter_login_key")
val LOFTER_LOGIN_AUTH = stringPreferencesKey("lofter_login_auth")
val LOFTER_EXPIRATION = stringPreferencesKey("lofter_expiration")
val LOFTER_START_TIME = longPreferencesKey("lofter_start_time")
val LOFTER_END_TIME = longPreferencesKey("lofter_end_time")

val PIXIV_PHPSSID = stringPreferencesKey("pixiv_phpssid")

val MISSEVAN_TOKEN = stringPreferencesKey("missevan_token")

val KUAIKAN_PASSTOKEN = stringPreferencesKey("kuaikan_passtoken")
// AppSettings keys
val LOCALE_MODE = stringPreferencesKey("locale_mode")
val THEME_MODE = stringPreferencesKey("theme_mode")
val NOTIFICATION_ENABLED =
    booleanPreferencesKey("notification_enabled")
val DOWNLOAD_PATH = stringPreferencesKey("download_path")
val MAX_CONCURRENT_DOWNLOADS =
    intPreferencesKey("max_concurrent_downloads")
val DELAY =
    intPreferencesKey("delay")
val MAX_RETRIES = intPreferencesKey("max_retries")
val WIFI = booleanPreferencesKey("wifi")
val PHOTOS_KEY = booleanPreferencesKey("photos")
// Read
fun Context.readApplicationUserScreenName() = readStoreString(key = APPLICATION_USER_SCREENNAME_KEY, defValue = "")
fun Context.readApplicationUserName() = readStoreString(key = APPLICATION_USER_NAME_KEY, defValue = "")
fun Context.readApplicationUserCt0() = readStoreString(key = APPLICATION_USER_USERCT0_KEY, defValue = "")
fun Context.readApplicationUserAuth() = readStoreString(key = APPLICATION_USER_USERAUTH_KEY, defValue = "")
fun Context.readLofterLoginKey() = readStoreString(key = LOFTER_LOGIN_KEY, defValue = "")
fun Context.readLofterLoginAuth() = readStoreString(key = LOFTER_LOGIN_AUTH, defValue = "")
fun Context.readLofterStartTime() = readStoreLong(key = LOFTER_START_TIME, defValue = 0L)
fun Context.readLofterEndTime() = readStoreLong(key = LOFTER_END_TIME, defValue = 0L)
fun Context.readPixivPHPSSID() = readStoreString(PIXIV_PHPSSID, "")
fun Context.readKuaikanPassToken() = readStoreString(KUAIKAN_PASSTOKEN, "")
fun Context.readDelay() = readStoreInt(DELAY, 2)
fun Context.readNotificationEnabled() = readStoreBoolean(key = NOTIFICATION_ENABLED, defValue = true)
fun Context.readDownloadPath() = readStoreString(key = DOWNLOAD_PATH, defValue = "")
fun Context.readMaxConcurrentDownloads() = readStoreInt(key = MAX_CONCURRENT_DOWNLOADS, defValue = 3)
fun Context.readMaxRetries() = readStoreInt(key = MAX_RETRIES, defValue = 3)
fun Context.readOnlyWifi() = readStoreBoolean(key = WIFI, defValue = true)
fun Context.readDownloadPhotos() = readStoreBoolean(key = PHOTOS_KEY, defValue = true)
fun Context.readLofterCookieExpiration() = readStoreString(key = LOFTER_EXPIRATION, defValue = "")
fun Context.readApplicationUserID() = readStoreString(key = APPLICATION_USER_ID, defValue = "")
fun Context.readSecureMyDownload() = readStoreBoolean(key = SECURE_MY_DOWNLOAD, defValue = false)
fun Context.readMissEvanToken() = readStoreString(key = MISSEVAN_TOKEN, defValue = "")
fun Context.readTheme() = readStoreString(THEME_MODE, defValue = ThemeMode.SYSTEM.name)
fun Context.readTwitterAuthorSubscription(authorScreenName: String) =
    readStoreBoolean(key = getTwitterAuthorSubscriptionKey(authorScreenName), defValue = false)
fun Context.readAllTwitterAuthorSubscriptions() = dataStore.data.map { preferences ->
    preferences.asMap().entries
        .filter { it.key.name.startsWith(TWITTER_AUTHOR_SUBSCRIPTION_PREFIX) }
        .associate {
            val authorScreenName = it.key.name.removePrefix(TWITTER_AUTHOR_SUBSCRIPTION_PREFIX)
            authorScreenName to (it.value as Boolean)
        }
}
//Write
suspend fun Context.writeApplicationUserScreenName(screenName: String) = saveStoreString(key = APPLICATION_USER_SCREENNAME_KEY, value = screenName)
suspend fun Context.writeApplicationUserName(name: String) = saveStoreString(key = APPLICATION_USER_NAME_KEY, value = name)
suspend fun Context.writeApplicationUserCt0(ct0: String) = saveStoreString(key = APPLICATION_USER_USERCT0_KEY, value = ct0)
suspend fun Context.writeApplicationUserAuth(auth: String) = saveStoreString(key = APPLICATION_USER_USERAUTH_KEY, value = auth)
suspend fun Context.writeLofterLoginKey(key: String) = saveStoreString(key = LOFTER_LOGIN_KEY, value = key)
suspend fun Context.writeLofterLoginAuth(auth: String) = saveStoreString(key = LOFTER_LOGIN_AUTH, value = auth)
suspend fun Context.writeNotificationEnabled(enabled: Boolean) = saveStoreBoolean(key = NOTIFICATION_ENABLED, value = enabled)
suspend fun Context.writeDownloadPath(path: String) = saveStoreString(key = DOWNLOAD_PATH, value = path)
suspend fun Context.writeMaxConcurrentDownloads(max: Int) = saveStoreInt(key = MAX_CONCURRENT_DOWNLOADS, value = max)
suspend fun Context.writeMaxRetries(max: Int) = saveStoreInt(key = MAX_RETRIES, value = max)
suspend fun Context.writeOnlyWifi(enabled: Boolean) = saveStoreBoolean(key = WIFI, value = enabled)
suspend fun Context.writeDownloadPhotos(enabled: Boolean) = saveStoreBoolean(key = PHOTOS_KEY, value = enabled)
suspend fun Context.writeLofterCookieExpiration(expiration: String) = saveStoreString(LOFTER_EXPIRATION, expiration)
suspend fun Context.writeLofterStartTime(time: Long) = saveStoreLong(LOFTER_START_TIME, time)
suspend fun Context.writeLofterEndTime(time: Long) = saveStoreLong(LOFTER_END_TIME, time)
suspend fun Context.writeApplicationUserID(id: String) = saveStoreString(APPLICATION_USER_ID, id)
suspend fun Context.writeDelay(delay: Int) = saveStoreInt(DELAY, delay)
suspend fun Context.writePixivPHPSSID(PixivSSID: String) = saveStoreString(PIXIV_PHPSSID, PixivSSID)
suspend fun Context.writeKuaikanPassToken(PassToken: String) = saveStoreString(KUAIKAN_PASSTOKEN, PassToken)
suspend fun Context.writeSecureMyDownload(set: Boolean) = saveStoreBoolean(SECURE_MY_DOWNLOAD, set)
suspend fun Context.writeMissEvanToken(token: String) = saveStoreString(MISSEVAN_TOKEN, token)
suspend fun Context.writeTheme(theme: String) = saveStoreString(THEME_MODE, theme)
suspend fun Context.writeTwitterAuthorSubscription(authorScreenName: String, isSubscribed: Boolean) =
    saveStoreBoolean(key = getTwitterAuthorSubscriptionKey(authorScreenName), value = isSubscribed)
