package killua.dev.base.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesDataSource @Inject constructor(
    private val preferences: DataStore<Preferences>
) {
    val applicationUserDataTwitter = preferences.data.map { prefs ->
        ApplicationUserDataTwitter(
            ct0 = prefs[APPLICATION_USER_USERCT0_KEY] ?: "",
            auth = prefs[APPLICATION_USER_USERAUTH_KEY] ?: ""
        )
    }
    val settingsData = preferences.data.map { prefs ->
        SettingsData(
            language = prefs[LANGUAGE_KEY] ?: "en",
            notificationEnabled = prefs[NOTIFICATION_ENABLED] != false,
            downloadPath = prefs[DOWNLOAD_PATH] ?: "",
            maxConcurrentDownloads = prefs[MAX_CONCURRENT_DOWNLOADS] ?: 3,
            maxRetries = prefs[MAX_RETRIES] ?: 3,
            wifiOnly = prefs[WIFI] == true,
            photos = prefs[PHOTOS_KEY] == true
        )
    }

    suspend fun <T> edit(key: Preferences.Key<T>, value: T) {
        preferences.edit { it[key] = value }
    }
}