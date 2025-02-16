package killua.dev.mediadownloader.ui

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation.NavHostController

val LocalNavController: ProvidableCompositionLocal<NavHostController?> =
    staticCompositionLocalOf { null }