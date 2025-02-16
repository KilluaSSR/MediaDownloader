package killua.dev.mediadownloader.ui.animations

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost

@Composable
fun AnimatedNavHost(
    navController: NavHostController,
    startDestination: String,
    modifier: Modifier = Modifier.Companion,
    contentAlignment: Alignment = Alignment.Companion.Center,
    route: String? = null,
    builder: NavGraphBuilder.() -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        contentAlignment = contentAlignment,
        route = route,
        enterTransition = {
            slideInHorizontally(initialOffsetX = { it })
        },
        exitTransition = {
            slideOutHorizontally()
        },
        popEnterTransition = {
            slideInHorizontally()
        },
        popExitTransition = {
            slideOutHorizontally(targetOffsetX = { it })
        },
        builder = builder,
    )

}