package killua.dev.setup

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.navigation.compose.composable
import killua.dev.setup.ui.Pages.PermissionsPage
import ui.LocalNavController
import ui.animations.AnimatedNavHost

@Composable
fun Setup(){
    val navController = LocalNavController.current!!
    AnimatedNavHost(
        navController = navController,
        startDestination = SetupRoutes.permissionsPage.route,
    ) {
        composable(SetupRoutes.welcomePage.route) {
            PermissionsPage()
        }
        composable(SetupRoutes.permissionsPage.route) {
            PermissionsPage()
        }

    }
}