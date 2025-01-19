package killua.dev.twitterdownloader.ui.Pages

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import killua.dev.core.MainRoutes
import killua.dev.twitterdownloader.ui.MainpageViewmodel
import ui.LocalNavController
import ui.animations.AnimatedNavHost
import ui.components.MainPageTopBar
import ui.components.MainScaffold
import ui.components.Section

@Composable
fun Home(){
    val navController = LocalNavController.current!!
    AnimatedNavHost(
        navController = navController,
        startDestination = MainRoutes.MainPage.route,
    ) {
        composable(MainRoutes.MainPage.route) {
            Mainpage()
        }
        composable(MainRoutes.UserinfoPage.route) {
            Mainpage()
        }
        composable(MainRoutes.Download.route) {
            Mainpage()
        }
        composable(MainRoutes.DownloadedPage.route) {
            Mainpage()
        }
        composable(MainRoutes.TwitterUserPage.route) {
            Mainpage()
        }
        composable(MainRoutes.SpecificTwitterUserPage.route) {
            Mainpage()
        }
        composable(MainRoutes.ReportPage.route) {
            Mainpage()
        }
        composable(MainRoutes.SettingPage.route) {
            Mainpage()
        }
        composable(MainRoutes.AboutPage.route) {
            Mainpage()
        }
        composable(MainRoutes.HelpPage.route) {
            Mainpage()
        }
    }
}
@Composable
fun Mainpage(
    mainpageViewmodel: MainpageViewmodel = viewModel()
) {
    val navController = rememberNavController()
    MainScaffold(
        topBar = {
            MainPageTopBar(navController)
        }
    ) {
        Section("Your summary") {
            Text("This is your summary")
        }

    }
}

@Preview
@Composable
fun MainpagePreview() {
    Mainpage()
}