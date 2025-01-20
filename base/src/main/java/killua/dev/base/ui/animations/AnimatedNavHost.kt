package killua.dev.base.ui.animations

@androidx.compose.runtime.Composable
fun AnimatedNavHost(
    navController: androidx.navigation.NavHostController,
    startDestination: String,
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier.Companion,
    contentAlignment: androidx.compose.ui.Alignment = androidx.compose.ui.Alignment.Companion.Center,
    route: String? = null,
    builder: androidx.navigation.NavGraphBuilder.() -> Unit
) {
    androidx.navigation.compose.NavHost(
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