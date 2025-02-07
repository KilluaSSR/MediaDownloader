package killua.dev.twitterdownloader

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
import killua.dev.base.Login.BrowserPage
import killua.dev.base.Model.AvailablePlatforms
import killua.dev.base.ui.CookiesRoutes
import killua.dev.base.ui.LocalNavController
import killua.dev.base.ui.MainRoutes
import killua.dev.base.ui.animations.AnimatedNavHost
import killua.dev.twitterdownloader.ui.pages.AboutPage
import killua.dev.twitterdownloader.ui.pages.DownloadPage
import killua.dev.twitterdownloader.ui.pages.MainPage
import killua.dev.twitterdownloader.ui.pages.SettingsPage
import killua.dev.twitterdownloader.ui.pages.AuthorsPage
import killua.dev.twitterdownloader.ui.theme.TwitterDownloaderTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            TwitterDownloaderTheme {
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
                        composable(MainRoutes.DownloadPage.route){
                            DownloadPage()
                        }
                        composable(MainRoutes.AuthorPage.route){
                            AuthorsPage()
                        }
                        composable(MainRoutes.SettingPage.route){
                            SettingsPage()
                        }
                        composable(MainRoutes.AboutPage.route){
                            AboutPage()
                        }


                        //Prepare Cookies
                        composable(CookiesRoutes.LofterCookiesBrowser.route){
                            BrowserPage(AvailablePlatforms.Lofter)
                        }
                    }
                }
            }
        }
    }
}
