package ui.animations

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost

@Composable
fun AnimatedNavHost(
    navController: androidx.navigation.NavHostController,
    startDestination: String,
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier.Companion,
    contentAlignment: Alignment = Alignment.Companion.Center,
    route: String? = null,
    builder: NavGraphBuilder.() -> Unit
){
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