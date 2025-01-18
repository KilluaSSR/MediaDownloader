package killua.dev.twitterdownloader

sealed class MainRoutes(val route: String){
    companion object{
        const val ARG_USER_ID = "userId"
    }
    data object MainPage : MainRoutes(route = "main_page")
    data object UserinfoPage : MainRoutes(route = "userinfo_page")
    data object DownloadingListPage : MainRoutes(route = "downloading_list_page")
    data object DownloadedPage : MainRoutes(route = "downloaded_page")
    data object SpecificTwitterUserPage : MainRoutes(route = "specific_twitter_user_page/{$ARG_USER_ID}") {
        fun getRoute(userId: String) = "specific_twitter_user_page/$userId"
    }
    data object ReportPage : MainRoutes(route = "report_page")
    data object SettingPage : MainRoutes(route = "setting_page")
    data object AboutPage : MainRoutes(route = "about_page")
    data object HelpPage : MainRoutes(route = "help_page")
}