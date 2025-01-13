package killua.dev.setup

sealed class SetupRoutes(val route: String){
    data object permissionsPage: SetupRoutes("permissionsPage")
    data object rootPermissionsPage: SetupRoutes("rootPermissionsPage")
    data object welcomePage: SetupRoutes("welcomePage")
}