package killua.dev.base.ui

sealed class MainRoutes(val route: String) {
    companion object {
        const val ARG_USER_ID = ""
    }
    data object MainPage : MainRoutes(route = "main_page")
    data object UserinfoPage : MainRoutes(route = "userinfo_page")
    data object Download : MainRoutes(route = "downloading")
    data object DownloadPage : MainRoutes(route = "downloaded_page")
    data object AuthorPage : MainRoutes(route = "author_page")
    data object SpecificTwitterUserPage :
        MainRoutes(route = "specific_twitter_user_page/{$ARG_USER_ID}") {
        fun getRoute(userId: String) = "specific_twitter_user_page/$userId"
    }

    data object ReportPage : MainRoutes(route = "report_page")
    data object SettingPage : MainRoutes(route = "setting_page")
    data object AboutPage : MainRoutes(route = "about_page")
    data object HelpPage : MainRoutes(route = "help_page")
}

sealed class UserpageRoutes(val route: String){
    data object TwitterSubscribe: UserpageRoutes(route = "twitter_subscribe")
    data object TwitterGetAll: UserpageRoutes(route = "twitter_get_all")
    data object LofterAuthors: UserpageRoutes(route = "lofter_authors")
    data object LofterGetAuthorImagesByTags: UserpageRoutes(route = "lofter_get_author_images_by_tags")
}

sealed class PrepareRoutes(val route: String){
    data object LofterPreparePage: PrepareRoutes(route = "lofter_prepare_page")
    data object LofterPrepareTagsPage: PrepareRoutes(route = "lofter_prepare_tags_page")
}
sealed class CookiesRoutes(val route: String){
    data object LofterCookiesBrowser: CookiesRoutes(route = "lofter_cookies_browser")
}