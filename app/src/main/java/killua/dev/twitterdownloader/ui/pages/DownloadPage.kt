package killua.dev.twitterdownloader.ui.pages

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import killua.dev.twitterdownloader.ui.DownloadedViewModel
import killua.dev.twitterdownloader.ui.MainPageTopBar
import killua.dev.twitterdownloader.ui.MainScaffold

@Composable
fun DownloadPage(){
    val navController = rememberNavController()
    val context = LocalContext.current
    val viewModel: DownloadedViewModel = hiltViewModel()
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    MainScaffold (
        topBar = {

        },
        snackbarHostState = viewModel.snackbarHostState
    ){
        LazyColumn {



        }
    }
}