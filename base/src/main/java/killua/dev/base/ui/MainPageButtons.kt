package killua.dev.base.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Help
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.DownloadDone
import androidx.compose.material.icons.rounded.Report
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.ui.graphics.vector.ImageVector


data class MainPageButtonsEssentials(
    val title: String,
    val icon: ImageVector,
    val route: String,
)

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
        "Twitter Users",
        Icons.Rounded.Star,
        MainRoutes.TwitterUserPage.route
    ),
    MainPageButtonsEssentials(
        "User Info",
        Icons.Rounded.AccountCircle,
        MainRoutes.UserinfoPage.route
    ),
)
val MainPageMenuButtons = listOf(
    MainPageButtonsEssentials(
        "Settings",
        Icons.Rounded.Settings,
        MainRoutes.SettingPage.route
    ),
    MainPageButtonsEssentials(
        "Report",
        Icons.Rounded.Report,
        MainRoutes.ReportPage.route
    ),
    MainPageButtonsEssentials(
        "About",
        Icons.AutoMirrored.Rounded.Help,
        MainRoutes.AboutPage.route
    )
)