package killua.dev.twitterdownloader.ui.pages

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import killua.dev.base.ui.LocalNavController
import killua.dev.twitterdownloader.ui.MainScaffold
import killua.dev.twitterdownloader.ui.TwitterUsersPageTopAppBar

@Composable
fun TwitterUserInfoPage(){
    val navController = LocalNavController.current!!
    val context = LocalContext.current
    //val viewModel: DownloadedViewModel = hiltViewModel()
    //val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    MainScaffold (
        topBar = {
            TwitterUsersPageTopAppBar(navController)
        },
     //   snackbarHostState = viewModel.snackbarHostState
    ){

    }
}