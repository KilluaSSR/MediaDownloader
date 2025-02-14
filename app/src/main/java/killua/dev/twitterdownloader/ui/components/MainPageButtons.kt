package killua.dev.twitterdownloader.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Help
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.DownloadDone
import androidx.compose.material.icons.rounded.Report
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Star
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import killua.dev.base.ui.MainRoutes


data class ButtonsEssentials(
    val title: String,
    val icon: ImageVector,
    val route: String,
    val color: Color = Color.Unspecified
)

data class MainPageBottomButtonsEssentials(
    val title: String,
    val icon: ImageVector,
    val action: MainPageButtonsAction
)

sealed class MainPageButtonsAction {
    data class Navigate(val route: String) : MainPageButtonsAction()
    object ShowDialog : MainPageButtonsAction()
}
val MainPageButtons = listOf(
    ButtonsEssentials(
        "Download by Link",
        Icons.Rounded.Download,
        MainRoutes.Download.route
    ),
    ButtonsEssentials(
        "Download List",
        Icons.Rounded.DownloadDone,
        MainRoutes.DownloadListPage.route
    ),
    ButtonsEssentials(
        "Advanced",
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
        "Settings",
        Icons.Rounded.Settings,
        MainPageButtonsAction.Navigate(MainRoutes.SettingPage.route)
    ),
    MainPageBottomButtonsEssentials(
        "Report",
        Icons.Rounded.Report,
        MainPageButtonsAction.ShowDialog
    ),
    MainPageBottomButtonsEssentials(
        "About",
        Icons.AutoMirrored.Rounded.Help,
        MainPageButtonsAction.Navigate(MainRoutes.AboutPage.route)
    )
)