package killua.dev.base.utils

import androidx.navigation.NavController
import androidx.navigation.NavHostController

fun NavHostController.maybePopBackStack() = if (previousBackStackEntry != null) popBackStack() else false
fun NavHostController.navigateSingle(route: String) = navigate(route) { popUpTo(route) { inclusive = true } }