package killua.dev.twitterdownloader.Model

import killua.dev.base.Model.AvailablePlatforms

data class MostDownloadedUser(
    val userID: String?,
    val screenName: String?,
    val name: String?,
    val totalDownloads: Int,
    val platforms: AvailablePlatforms
)