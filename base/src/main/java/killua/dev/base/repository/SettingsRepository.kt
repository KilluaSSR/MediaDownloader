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
    // 用 StateFlow 存储最新的设置值，默认值是一个空的 SettingsData
    private val _settingsFlow = MutableStateFlow(SettingsData())

    init {
        scope.launch {
            preferencesDataSource.settingsData.collect { settings ->
                _settingsFlow.value = settings  // 每次更新数据
            }
        }
    }

    // 公开 Flow 给 UI 或其他模块监听
    val settingsFlow: StateFlow<SettingsData> = _settingsFlow.asStateFlow()

    // 同步读取最新值（不会返回旧值）
    fun getWifiOnlySync(): Boolean = runBlocking{settingsFlow.first().wifiOnly}
    fun getNotificationEnabledSync(): Boolean = settingsFlow.value.notificationEnabled
    fun getMaxConcurrentDownloadsSync(): Int = settingsFlow.value.maxConcurrentDownloads
    fun getMaxRetriesSync(): Int = settingsFlow.value.maxRetries

    // 挂起函数，一次性读取最新值（如果不想用同步方法）
    suspend fun getWifiOnlyOnce(): Boolean = settingsFlow.first().wifiOnly
    suspend fun getNotificationEnabledOnce(): Boolean = settingsFlow.first().notificationEnabled
    suspend fun getMaxConcurrentDownloads(): Int = settingsFlow.first().maxConcurrentDownloads
    suspend fun getMaxRetries(): Int = settingsFlow.first().maxRetries
}