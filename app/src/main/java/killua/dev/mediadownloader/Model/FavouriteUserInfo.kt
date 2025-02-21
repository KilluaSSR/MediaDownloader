package killua.dev.mediadownloader.Model

data class FavouriteUserInfo(
    val name: String = "",
    val screenName: String = "",
    val downloadCount: Int = 0,
    val platform: AvailablePlatforms,
    val hasDownloaded: Boolean = false
) {
    val displayName: String
        get() = if (hasDownloaded) {
            when(platform) {
                AvailablePlatforms.Twitter,
                AvailablePlatforms.Lofter -> "$name @$screenName"
                AvailablePlatforms.Pixiv -> "@$screenName"
                else -> screenName
            }
        } else {
            "Nothing here"
        }

    val description: String
        get() = if (hasDownloaded) {
            "You've downloaded his/her media $downloadCount times"
        } else {
            ""
        }
}