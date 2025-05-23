package killua.dev.mediadownloader.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import killua.dev.mediadownloader.R
import killua.dev.mediadownloader.ui.components.common.TopBar

@Composable
fun AdvancedPageTopAppBar(navController: NavHostController){
    TopBar(navController, stringResource(R.string.advanced), showMoreIcon = false){}
}
@Composable
fun DownloadListTopAppBar(title: String, navController: NavHostController){
    TopBar(navController,title, showMoreIcon = false){}
}

@Composable
fun UserInfoPageTopAppBar(navController: NavHostController){
    TopBar(navController,"User Info", showMoreIcon = false){}
}