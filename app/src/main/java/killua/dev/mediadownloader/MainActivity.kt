package killua.dev.mediadownloader

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import killua.dev.mediadownloader.Login.BrowserPage
import killua.dev.mediadownloader.Model.AvailablePlatforms
import killua.dev.mediadownloader.ui.CookiesRoutes
import killua.dev.mediadownloader.ui.LocalNavController
import killua.dev.mediadownloader.ui.MainRoutes
import killua.dev.mediadownloader.ui.PrepareRoutes
import killua.dev.mediadownloader.ui.ViewModels.MainPageViewmodel
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
import killua.dev.mediadownloader.utils.classifyLinks

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MainPageViewmodel by viewModels()
    @OptIn(ExperimentalFoundationApi::class)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharedLink = when{
            intent?.action == Intent.ACTION_SEND ->{
                intent.getStringExtra(Intent.EXTRA_TEXT)
            }
            else -> null
        }
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            MediaDownloaderTheme {
                val navController = rememberNavController()
                rememberCoroutineScope()
                val downloadResult by viewModel.sharedDownloadResult.collectAsStateWithLifecycle()
                LaunchedEffect(sharedLink) {
                    sharedLink?.let { url ->
                        val platform = classifyLinks(url)
                        viewModel.handleSharedLink(platform, url)
                    }
                }

                downloadResult?.let { result ->
                    LaunchedEffect(result) {
                        when {
                            result.isSuccess -> Toast.makeText(
                                this@MainActivity,
                                "Added to download list",
                                Toast.LENGTH_SHORT
                            ).show()
                            result.isFailure -> "Failed to download"
                            else -> return@LaunchedEffect
                        }
                    }
                }
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
