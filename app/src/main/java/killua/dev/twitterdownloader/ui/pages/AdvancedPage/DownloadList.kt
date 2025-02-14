package killua.dev.twitterdownloader.ui.pages.AdvancedPage

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import killua.dev.base.ui.LocalNavController
import killua.dev.twitterdownloader.ui.ViewModels.AdvancedPageViewModel
import killua.dev.twitterdownloader.ui.components.DownloadListTopAppBar
import killua.dev.twitterdownloader.ui.components.MainScaffold

@Composable
fun DownloadList(){
    val navController = LocalNavController.current!!
    val viewModel: AdvancedPageViewModel = hiltViewModel()
    viewModel.uiState.collectAsStateWithLifecycle()
    rememberCoroutineScope()
    MainScaffold (
        topBar = {
            DownloadListTopAppBar("",navController)
        },
        snackbarHostState = viewModel.snackbarHostState
    ){

    }
}