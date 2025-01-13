package killua.dev.setup.ui.Pages

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import killua.dev.setup.ui.SetupPageViewModel
import killua.dev.setup.ui.SetupUIIntent
import ui.LocalNavHostController
import ui.components.SetOnResume

@ExperimentalMaterial3Api
@ExperimentalFoundationApi
@Composable
fun PermissionsPage(viewModel: SetupPageViewModel = viewModel()) {
    val navController = LocalNavHostController.current!!
    val context = LocalContext.current
    val rootState = viewModel.rootState.collectAsState()
    val notificationState = viewModel.notificationState.collectAsState()
    val storagePermissionState = viewModel.storagePermissionState.collectAsState()

    SetOnResume {
        viewModel.emitIntentOnIO(SetupUIIntent.onResume)
    }

}