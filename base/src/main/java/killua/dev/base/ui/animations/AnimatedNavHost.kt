package killua.dev.base.ui.animations

import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost

@androidx.compose.runtime.Composable
fun AnimatedNavHost(
    navController: NavHostController,
    startDestination: String,
    modifier: Modifier = Modifier.Companion,
    contentAlignment: androidx.compose.ui.Alignment = androidx.compose.ui.Alignment.Companion.Center,
    route: String? = null,
    builder: androidx.navigation.NavGraphBuilder.() -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        contentAlignment = contentAlignment,
        route = route,
        enterTransition = {
            androidx.compose.animation.slideInHorizontally(initialOffsetX = { it })
        },
        exitTransition = {
            androidx.compose.animation.slideOutHorizontally()
        },
        popEnterTransition = {
            androidx.compose.animation.slideInHorizontally()
        },
        popExitTransition = {
            androidx.compose.animation.slideOutHorizontally(targetOffsetX = { it })
        },
        builder = builder,
    )

}