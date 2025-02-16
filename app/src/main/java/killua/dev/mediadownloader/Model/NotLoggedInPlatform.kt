package killua.dev.mediadownloader.Model

data class NotLoggedInPlatform(
    val showNotLoggedInAlert: Boolean = false,
    val platforms: AvailablePlatforms? = null
)
