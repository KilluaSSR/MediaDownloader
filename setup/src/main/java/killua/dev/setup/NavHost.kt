package killua.dev.setup

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.Composable
import androidx.navigation.compose.composable
import killua.dev.base.ui.LocalNavController
import killua.dev.base.ui.animations.AnimatedNavHost
import killua.dev.setup.ui.Pages.BrowserPage
import killua.dev.setup.ui.Pages.PermissionsPage

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Setup() {
    val navController = LocalNavController.current!!
    AnimatedNavHost(
        navController = navController,
        startDestination = SetupRoutes.PermissionsPage.route,
    ) {
        composable(SetupRoutes.PermissionsPage.route) {
            PermissionsPage()
        }
        composable(SetupRoutes.BrowserPage.route){
            BrowserPage()
        }
    }
}