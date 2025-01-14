package killua.dev.setup

sealed class SetupRoutes(val route: String){
    data object permissionsPage: SetupRoutes("permissionsPage")
    data object welcomePage: SetupRoutes("welcomePage")
}