package killua.dev.mediadownloader

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.rememberCoroutineScope
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import killua.dev.mediadownloader.Login.BrowserPage
import killua.dev.mediadownloader.Model.AvailablePlatforms
import killua.dev.mediadownloader.ui.CookiesRoutes
import killua.dev.mediadownloader.ui.LocalNavController
import killua.dev.mediadownloader.ui.MainRoutes
import killua.dev.mediadownloader.ui.PrepareRoutes
import killua.dev.mediadownloader.ui.animations.AnimatedNavHost
import killua.dev.mediadownloader.ui.pages.AboutPage
import killua.dev.mediadownloader.ui.pages.AdvancedPage
import killua.dev.mediadownloader.ui.pages.DownloadListPage
import killua.dev.mediadownloader.ui.pages.MainPage
import killua.dev.mediadownloader.ui.pages.OtherSetupPages.Kuaikan.KuaikanPreparePage
import killua.dev.mediadownloader.ui.pages.OtherSetupPages.Lofter.LofterPreparePage
import killua.dev.mediadownloader.ui.pages.OtherSetupPages.Lofter.LofterPrepareTagsPage
import killua.dev.mediadownloader.ui.pages.OtherSetupPages.Pixiv.PixivPreparePage
import killua.dev.mediadownloader.ui.pages.OtherSetupPages.Twitter.TwitterPreparePage
import killua.dev.mediadownloader.ui.pages.SettingsPage
import killua.dev.mediadownloader.ui.pages.UserInfoPage
import killua.dev.mediadownloader.ui.theme.MediaDownloaderTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            MediaDownloaderTheme {
                val navController = rememberNavController()
                rememberCoroutineScope()
                CompositionLocalProvider(
                    LocalNavController provides navController,
                    LocalLifecycleOwner provides LocalLifecycleOwner.current
                ) {
                    AnimatedNavHost(
                        navController = navController,
                        startDestination = MainRoutes.MainPage.route
                    ) {
                        composable(MainRoutes.MainPage.route) {
                            MainPage()
                        }
                        composable(MainRoutes.DownloadListPage.route){
                            DownloadListPage()
                        }
                        composable(MainRoutes.AdvancedPage.route){
                            AdvancedPage()
                        }
                        composable(MainRoutes.UserinfoPage.route){
                            UserInfoPage()
                        }
                        composable(MainRoutes.SettingPage.route){
                            SettingsPage()
                        }
                        composable(MainRoutes.AboutPage.route){
                            AboutPage()
                        }


                        //Prepare Page
                        composable(PrepareRoutes.LofterPreparePage.route){
                            LofterPreparePage()
                        }
                        composable(PrepareRoutes.LofterPrepareTagsPage.route){
                            LofterPrepareTagsPage()
                        }
                        composable(PrepareRoutes.PixivPreparePage.route){
                            PixivPreparePage()
                        }
                        composable(PrepareRoutes.KuaikanPreparePage.route){
                            KuaikanPreparePage()
                        }

                        composable(PrepareRoutes.TwitterPreparePage.route){
                            TwitterPreparePage()
                        }

                        //Prepare Cookies
                        composable(CookiesRoutes.LofterCookiesBrowser.route){
                            BrowserPage(AvailablePlatforms.Lofter)
                        }
                        composable(CookiesRoutes.PixivCookiesBrowser.route){
                            BrowserPage(AvailablePlatforms.Pixiv)
                        }
                        composable(CookiesRoutes.KuaikanCookiesBrowser.route){
                            BrowserPage(AvailablePlatforms.Kuaikan)
                        }
                        composable(CookiesRoutes.TwitterCookiesBrowser.route){
                            BrowserPage(AvailablePlatforms.Twitter)
                        }
                    }
                }
            }
        }
    }
}
