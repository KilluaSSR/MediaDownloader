package killua.dev.base.utils

import androidx.navigation.NavController

fun NavController.navigateSingle(route: String) = navigate(route) {
    popUpTo(route) {
        inclusive = true
    }
}