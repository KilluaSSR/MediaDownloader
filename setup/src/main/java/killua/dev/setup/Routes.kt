package killua.dev.setup

sealed class SetupRoutes(val route: String){
    data object PermissionsPage: SetupRoutes("permissionsPage")
    data object WelcomePage: SetupRoutes("welcomePage")
}