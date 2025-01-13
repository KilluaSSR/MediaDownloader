package killua.dev.setup

import androidx.compose.runtime.Composable
import androidx.navigation.compose.composable
import ui.animations.AnimatedNavHost

@Composable
fun Setup(){
    val navController = null
    AnimatedNavHost(
        navController = navController!!,
        startDestination = SetupRoutes.welcomePage.route,
    ) {
        composable(SetupRoutes.welcomePage.route) {
            TODO()
        }
        composable(SetupRoutes.permissionsPage.route) {
            TODO()
        }
        composable(SetupRoutes.rootPermissionsPage.route) {
            TODO()
        }
    }
}