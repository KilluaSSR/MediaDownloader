package killua.dev.base.ui

sealed class MainRoutes(val route: String) {
    companion object {
        const val ARG_USER_ID = ""
    }
    data object MainPage : MainRoutes(route = "main_page")
    data object UserinfoPage : MainRoutes(route = "userinfo_page")
    data object Download : MainRoutes(route = "downloading")
    data object DownloadPage : MainRoutes(route = "downloaded_page")
    data object TwitterUserPage : MainRoutes(route = "twitter_user_page")
    data object SpecificTwitterUserPage :
        MainRoutes(route = "specific_twitter_user_page/{$ARG_USER_ID}") {
        fun getRoute(userId: String) = "specific_twitter_user_page/$userId"
    }

    data object ReportPage : MainRoutes(route = "report_page")
    data object SettingPage : MainRoutes(route = "setting_page")
    data object AboutPage : MainRoutes(route = "about_page")
    data object HelpPage : MainRoutes(route = "help_page")
}
