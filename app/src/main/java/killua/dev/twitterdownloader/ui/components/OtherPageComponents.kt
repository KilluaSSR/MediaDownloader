package killua.dev.twitterdownloader.ui.components

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import killua.dev.base.ui.components.TopBar

@Composable
fun AdvancedPageTopAppBar(navController: NavHostController){
    TopBar(navController,"Advanced", showMoreIcon = false){}
}
@Composable
fun DownloadListTopAppBar(title: String, navController: NavHostController){
    TopBar(navController,title, showMoreIcon = false){}
}

@Composable
fun UserInfoPageTopAppBar(navController: NavHostController){
    TopBar(navController,"User Info", showMoreIcon = false){}
}