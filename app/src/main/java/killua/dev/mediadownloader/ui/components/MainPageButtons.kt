package killua.dev.mediadownloader.ui.components

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Help
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.DownloadDone
import androidx.compose.material.icons.rounded.Report
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Star
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import killua.dev.mediadownloader.R
import killua.dev.mediadownloader.ui.MainRoutes

data class ButtonsEssentials(
    @StringRes val titleRes: Int,
    val icon: ImageVector,
    val route: String,
    val color: Color = Color.Unspecified
)

data class MainPageBottomButtonsEssentials(
    @StringRes val titleRes: Int,
    val icon: ImageVector,
    val action: MainPageButtonsAction
)

sealed class MainPageButtonsAction {
    data class Navigate(val route: String) : MainPageButtonsAction()
    object ShowDialog : MainPageButtonsAction()
}
val MainPageButtons = listOf(
    ButtonsEssentials(
        R.string.download_by_link,
        Icons.Rounded.Download,
        MainRoutes.Download.route
    ),
    ButtonsEssentials(
        R.string.download_list,
        Icons.Rounded.DownloadDone,
        MainRoutes.DownloadListPage.route
    ),
    ButtonsEssentials(
        R.string.advanced,
        Icons.Rounded.Star,
        MainRoutes.AdvancedPage.route
    ),
//    ButtonsEssentials(
//        "User Info",
//        Icons.Rounded.AccountCircle,
//        MainRoutes.UserinfoPage.route
//    ),
)

val MainPageMenuButtons = listOf(
    MainPageBottomButtonsEssentials(
        R.string.settings,
        Icons.Rounded.Settings,
        MainPageButtonsAction.Navigate(MainRoutes.SettingPage.route)
    ),
    MainPageBottomButtonsEssentials(
        R.string.report,
        Icons.Rounded.Report,
        MainPageButtonsAction.ShowDialog
    ),
    MainPageBottomButtonsEssentials(
        R.string.about,
        Icons.AutoMirrored.Rounded.Help,
        MainPageButtonsAction.Navigate(MainRoutes.AboutPage.route)
    )
)