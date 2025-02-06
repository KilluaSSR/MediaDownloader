package killua.dev.base.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Help
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.DownloadDone
import androidx.compose.material.icons.rounded.QuestionMark
import androidx.compose.material.icons.rounded.Report
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Star
import androidx.compose.ui.graphics.vector.ImageVector


data class MainPageButtonsEssentials(
    val title: String,
    val icon: ImageVector,
    val route: String,
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
    MainPageButtonsEssentials(
        "Download Now",
        Icons.Rounded.Download,
        MainRoutes.Download.route
    ),
    MainPageButtonsEssentials(
        "Downloaded",
        Icons.Rounded.DownloadDone,
        MainRoutes.DownloadPage.route
    ),
    MainPageButtonsEssentials(
        "Authors",
        Icons.Rounded.Star,
        MainRoutes.AuthorPage.route
    ),
    MainPageButtonsEssentials(
        "User Info",
        Icons.Rounded.AccountCircle,
        MainRoutes.UserinfoPage.route
    ),
    MainPageButtonsEssentials(
        "Experiment",
        Icons.Rounded.QuestionMark,
        MainRoutes.UserinfoPage.route
    ),
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