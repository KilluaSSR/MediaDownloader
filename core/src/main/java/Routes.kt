package killua.dev.core
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Help
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.ContactPage
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.DownloadDone
import androidx.compose.material.icons.rounded.Downloading
import androidx.compose.material.icons.rounded.Help
import androidx.compose.material.icons.rounded.Report
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.sharp.Download
import androidx.compose.ui.graphics.vector.ImageVector

sealed class MainRoutes(val route: String){
    companion object{
        const val ARG_USER_ID = "userId"
    }
    data object MainPage : MainRoutes(route = "main_page")
    data object UserinfoPage : MainRoutes(route = "userinfo_page")
    data object Download : MainRoutes(route = "downloading")
    data object DownloadedPage : MainRoutes(route = "downloaded_page")
    data object TwitterUserPage : MainRoutes(route = "twitter_user_page")
    data object SpecificTwitterUserPage : MainRoutes(route = "specific_twitter_user_page/{$ARG_USER_ID}") {
        fun getRoute(userId: String) = "specific_twitter_user_page/$userId"
    }
    data object ReportPage : MainRoutes(route = "report_page")
    data object SettingPage : MainRoutes(route = "setting_page")
    data object AboutPage : MainRoutes(route = "about_page")
    data object HelpPage : MainRoutes(route = "help_page")
}

data class MainPageButtonsEssentials(
    val title: String,
    val icon: ImageVector,
    val route: String,
)
val MainPageButtons = listOf(
    MainPageButtonsEssentials(
        "Download Now",
        Icons.Sharp.Download,
        MainRoutes.Download.route
    ),
    MainPageButtonsEssentials(
        "Downloaded",
        Icons.Rounded.DownloadDone,
        MainRoutes.DownloadedPage.route
    ),
    MainPageButtonsEssentials(
        "Twitter Users",
        Icons.Rounded.AccountCircle,
        MainRoutes.TwitterUserPage.route
    ),
    MainPageButtonsEssentials(
        "User Info",
        Icons.Rounded.ContactPage,
        MainRoutes.UserinfoPage.route
    ),


)
val MainPageDropdownMenuButtons = listOf(
    MainPageButtonsEssentials(
        "Settings",
        Icons.Outlined.Settings,
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