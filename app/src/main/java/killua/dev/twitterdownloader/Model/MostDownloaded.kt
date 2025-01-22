package killua.dev.twitterdownloader.Model

data class MostDownloadedUser(
    val twitterUserId: String?,
    val twitterScreenName: String?,
    val twitterName: String?,
    val totalDownloads: Int
)