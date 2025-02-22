package killua.dev.mediadownloader.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import killua.dev.mediadownloader.Model.AppIcon
import killua.dev.mediadownloader.Model.AvailablePlatforms
import killua.dev.mediadownloader.Model.platformName
import killua.dev.mediadownloader.R
import killua.dev.mediadownloader.ui.PrepareRoutes
import killua.dev.mediadownloader.ui.components.common.CancellableAlert
import killua.dev.mediadownloader.utils.navigateSingle

@Composable
fun NotLoggedInAlert(
    platform: AvailablePlatforms,
    navController: NavHostController,
    onDismiss: ()-> Unit
){
    val context = LocalContext.current
    CancellableAlert(
        "${context.getString(platformName[platform]!!)} ${stringResource(R.string.not_logged_in)}",
        stringResource(R.string.cookie_necessary, context.getString(platformName[platform]!!)),
        icon = {
            AppIcon(platform)
        },
        onDismiss = onDismiss,
    ) {
        when(platform){
            AvailablePlatforms.Twitter -> {
                navController.navigateSingle(PrepareRoutes.TwitterPreparePage.route)
            }
            AvailablePlatforms.Lofter -> {
                navController.navigateSingle(PrepareRoutes.LofterPreparePage.route)
            }

            AvailablePlatforms.Pixiv -> {
                navController.navigateSingle(PrepareRoutes.PixivPreparePage.route)
            }

            AvailablePlatforms.Kuaikan -> {
                navController.navigateSingle(PrepareRoutes.KuaikanPreparePage.route)
            }

            AvailablePlatforms.MissEvan -> {
                navController.navigateSingle(PrepareRoutes.MissEvavnPreparePage.route)
            }
        }
    }
}