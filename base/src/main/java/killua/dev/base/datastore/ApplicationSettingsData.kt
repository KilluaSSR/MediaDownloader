package killua.dev.base.datastore

data class SettingsData(
    val language: String = "en",
    val notificationEnabled: Boolean = true,
    val downloadPath: String = "",
    val maxConcurrentDownloads: Int = 3,
    val maxRetries: Int = 3,
    val wifiOnly: Boolean = false
)