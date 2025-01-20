package killua.dev.setup

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.Composable
import androidx.navigation.compose.composable
import killua.dev.base.ui.LocalNavController
import killua.dev.setup.ui.Pages.PermissionsPage

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Setup() {
    val navController = LocalNavController.current!!
    killua.dev.base.ui.animations.AnimatedNavHost(
        navController = navController,
        startDestination = SetupRoutes.PermissionsPage.route,
    ) {
        composable(SetupRoutes.WelcomePage.route) {
            PermissionsPage()
        }
        composable(SetupRoutes.PermissionsPage.route) {
            PermissionsPage()
        }

    }
}