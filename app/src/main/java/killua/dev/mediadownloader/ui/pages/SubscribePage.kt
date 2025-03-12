package killua.dev.mediadownloader.ui.pages

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import killua.dev.mediadownloader.ui.LocalNavController
import killua.dev.mediadownloader.ui.ViewModels.SubscribePageViewModel
import killua.dev.mediadownloader.ui.components.MainScaffold
import killua.dev.mediadownloader.ui.components.common.BottomSheet
import killua.dev.mediadownloader.ui.components.common.SubscribePageTopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscribePage() {
    val navController = LocalNavController.current!!
    LocalContext.current
    val viewModel: SubscribePageViewModel = hiltViewModel()
    viewModel.uiState.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }
    MainScaffold(
        topBar = {
            SubscribePageTopAppBar(
                navController,
            ) {
                showBottomSheet = true
            }
        },
        snackbarHostState = viewModel.snackbarHostState
    ) {
        if(showBottomSheet){
            BottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = sheetState
            ) {

            }
        }
    }
}