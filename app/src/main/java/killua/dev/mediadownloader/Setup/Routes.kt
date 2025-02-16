package killua.dev.mediadownloader.Setup

sealed class SetupRoutes(val route: String) {
    data object PermissionsPage : SetupRoutes("permissionsPage")
    data object BrowserPage: SetupRoutes("browserPage")
}