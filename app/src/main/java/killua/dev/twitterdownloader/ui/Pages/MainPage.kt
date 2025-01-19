package killua.dev.twitterdownloader.ui.Pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import killua.dev.core.MainPageButtons
import killua.dev.core.MainRoutes
import killua.dev.core.ui.components.paddingTop
import killua.dev.twitterdownloader.ui.MainPageViewmodel
import ui.LocalNavController
import ui.animations.AnimatedNavHost
import ui.components.ActionsBotton
import ui.components.MainPageTopBar
import ui.components.MainScaffold
import ui.components.Section
import ui.tokens.SizeTokens

@Composable
fun Home(){
    val navController = LocalNavController.current!!
    AnimatedNavHost(
        navController = navController,
        startDestination = MainRoutes.MainPage.route,
    ) {
        composable(MainRoutes.MainPage.route) {
            MainPage()
        }
        composable(MainRoutes.UserinfoPage.route) {
            MainPage()
        }
        composable(MainRoutes.Download.route) {
            MainPage()
        }
        composable(MainRoutes.DownloadedPage.route) {
            MainPage()
        }
        composable(MainRoutes.TwitterUserPage.route) {
            MainPage()
        }
        composable(MainRoutes.SpecificTwitterUserPage.route) {
            MainPage()
        }
        composable(MainRoutes.ReportPage.route) {
            MainPage()
        }
        composable(MainRoutes.SettingPage.route) {
            MainPage()
        }
        composable(MainRoutes.AboutPage.route) {
            MainPage()
        }
        composable(MainRoutes.HelpPage.route) {
            MainPage()
        }
    }
}
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MainPage(
) {
    val navController = rememberNavController()
    MainScaffold(
        topBar = {
            MainPageTopBar(navController)
        }
    ) {
        Column (
            modifier = Modifier
                .paddingTop(SizeTokens.Level8),
            verticalArrangement = Arrangement.spacedBy(SizeTokens.Level24)
        ){
            Section(title = "Actions") {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(SizeTokens.Level8),
                    verticalArrangement = Arrangement.spacedBy(SizeTokens.Level8),
                    maxItemsInEachRow = 2,
                ) {
                    MainPageButtons.forEach { items->
                        ActionsBotton(
                            modifier = Modifier.weight(1f),
                            enabled = true,
                            title = items.title,
                            icon = items.icon
                        ) { }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun MainpagePreview() {
    MainPage()
}