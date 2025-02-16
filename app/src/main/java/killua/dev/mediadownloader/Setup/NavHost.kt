package killua.dev.mediadownloader.Setup

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.Composable
import androidx.navigation.compose.composable
import killua.dev.mediadownloader.Login.BrowserPage
import killua.dev.mediadownloader.Model.AvailablePlatforms
import killua.dev.mediadownloader.Setup.ui.Pages.SetupPage
import killua.dev.mediadownloader.ui.LocalNavController
import killua.dev.mediadownloader.ui.animations.AnimatedNavHost

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Setup() {
    val navController = LocalNavController.current!!
    AnimatedNavHost(
        navController = navController,
        startDestination = SetupRoutes.PermissionsPage.route,
    ) {
        composable(SetupRoutes.PermissionsPage.route) {
            SetupPage()
        }
        composable(SetupRoutes.BrowserPage.route){
            BrowserPage(AvailablePlatforms.Twitter)
        }
    }
}