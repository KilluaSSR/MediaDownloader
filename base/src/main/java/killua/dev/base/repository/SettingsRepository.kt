package killua.dev.base.repository

import killua.dev.base.datastore.PreferencesDataSource
import killua.dev.base.datastore.SettingsData
import killua.dev.base.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    private val preferencesDataSource: PreferencesDataSource,
    @ApplicationScope private val scope: CoroutineScope
) {
    private val _settingsFlow = MutableStateFlow(SettingsData())

    init {
        scope.launch {
            preferencesDataSource.settingsData.collect { settings ->
                _settingsFlow.value = settings
            }
        }
    }

    val settingsFlow: StateFlow<SettingsData> = _settingsFlow.asStateFlow()


    fun getWifiOnlySync(): Boolean = runBlocking{settingsFlow.first().wifiOnly}
    fun getNotificationEnabledSync(): Boolean = settingsFlow.value.notificationEnabled
    fun getMaxConcurrentDownloadsSync(): Int = settingsFlow.value.maxConcurrentDownloads
    fun getMaxRetriesSync(): Int = settingsFlow.value.maxRetries


    suspend fun getWifiOnlyOnce(): Boolean = settingsFlow.first().wifiOnly
    suspend fun getNotificationEnabledOnce(): Boolean = settingsFlow.first().notificationEnabled
    suspend fun getMaxConcurrentDownloads(): Int = settingsFlow.first().maxConcurrentDownloads
    suspend fun getMaxRetries(): Int = settingsFlow.first().maxRetries
}