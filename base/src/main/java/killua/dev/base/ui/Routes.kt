package killua.dev.base.ui

sealed class MainRoutes(val route: String) {
    companion object {
        const val ARG_USER_ID = ""
    }
    data object MainPage : MainRoutes(route = "main_page")
    data object UserinfoPage : MainRoutes(route = "userinfo_page")
    data object Download : MainRoutes(route = "downloading")
    data object DownloadListPage : MainRoutes(route = "downloaded_page")
    data object AdvancedPage : MainRoutes(route = "advanced_page")
    data object SpecificTwitterUserPage :
        MainRoutes(route = "specific_twitter_user_page/{$ARG_USER_ID}") {
        fun getRoute(userId: String) = "specific_twitter_user_page/$userId"
    }

    data object ReportPage : MainRoutes(route = "report_page")
    data object SettingPage : MainRoutes(route = "setting_page")
    data object AboutPage : MainRoutes(route = "about_page")
    data object HelpPage : MainRoutes(route = "help_page")
}

sealed class AdvancedpageRoutes(val route: String){
    data object TwitterGetMyBookmarks: AdvancedpageRoutes(route = "twitter_my_bookmarks")
    data object TwitterSubscribe: AdvancedpageRoutes(route = "twitter_subscribe")
    data object TwitterGetAll: AdvancedpageRoutes(route = "twitter_get_all")
    data object LofterAuthors: AdvancedpageRoutes(route = "lofter_authors")
    data object LofterGetAuthorImagesByTags: AdvancedpageRoutes(route = "lofter_get_author_images_by_tags")
}

sealed class PrepareRoutes(val route: String){
    data object LofterPreparePage: PrepareRoutes(route = "lofter_prepare_page")
    data object PixivPreparePage: PrepareRoutes(route = "pixiv_prepare_page")
    data object KuaikanPreparePage: PrepareRoutes(route = "kuaikan_prepare_page")
    data object LofterPrepareTagsPage: PrepareRoutes(route = "lofter_prepare_tags_page")
}
sealed class CookiesRoutes(val route: String){
    data object LofterCookiesBrowser: CookiesRoutes(route = "lofter_cookies_browser")
    data object PixivCookiesBrowser: CookiesRoutes(route = "pixiv_cookies_browser")
    data object KuaikanCookiesBrowser: CookiesRoutes(route = "kuaikan_cookies_browser")
}