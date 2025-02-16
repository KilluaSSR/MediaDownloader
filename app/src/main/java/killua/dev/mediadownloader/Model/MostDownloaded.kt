package killua.dev.mediadownloader.Model

data class MostDownloadedUser(
    val userID: String?,
    val screenName: String?,
    val name: String?,
    val totalDownloads: Int,
    val platforms: AvailablePlatforms
)