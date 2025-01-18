package killua.dev.core
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.ContactPage
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Downloading
import androidx.compose.ui.graphics.vector.ImageVector

sealed class MainRoutes(val route: String){
    companion object{
        const val ARG_USER_ID = "userId"
    }
    data object MainPage : MainRoutes(route = "main_page")
    data object UserinfoPage : MainRoutes(route = "userinfo_page")
    data object DownloadingListPage : MainRoutes(route = "downloading_list_page")
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
        MainRoutes.DownloadingListPage.route,
        Icons.Rounded.Downloading,
        MainRoutes.DownloadingListPage.route
    ),
    MainPageButtonsEssentials(
        MainRoutes.DownloadedPage.route,
        Icons.Rounded.Download,
        MainRoutes.DownloadedPage.route
    ),
    MainPageButtonsEssentials(
        MainRoutes.TwitterUserPage.route,
        Icons.Rounded.AccountCircle,
        MainRoutes.TwitterUserPage.route
    ),
    MainPageButtonsEssentials(
        MainRoutes.UserinfoPage.route,
        Icons.Rounded.ContactPage,
        MainRoutes.UserinfoPage.route
    ),

)
val MainPageDropdownMenuButtons = listOf(
    MainPageButtonsEssentials(
        MainRoutes.SettingPage.route,
        Icons.Rounded.AccountCircle,
        MainRoutes.SettingPage.route
    ),
    MainPageButtonsEssentials(
        MainRoutes.ReportPage.route,
        Icons.Rounded.AccountCircle,
        MainRoutes.ReportPage.route
    ),
    MainPageButtonsEssentials(
        MainRoutes.AboutPage.route,
        Icons.Rounded.AccountCircle,
        MainRoutes.AboutPage.route
    ),
    MainPageButtonsEssentials(
        MainRoutes.HelpPage.route,
        Icons.Rounded.AccountCircle,
        MainRoutes.HelpPage.route
    ),
)