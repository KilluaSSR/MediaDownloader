package killua.dev.setup.ui.Pages

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import killua.dev.core.utils.navigateSingle
import killua.dev.setup.SetupRoutes
import killua.dev.setup.ui.SetupPageViewModel
import killua.dev.setup.ui.SetupUIIntent
import ui.LocalNavController
import ui.components.SetOnResume
import ui.components.SetupScaffold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionsPage(viewModel: SetupPageViewModel = viewModel()) {
    val navController = LocalNavController.current!!
    val context = LocalContext.current
    val rootState = viewModel.rootState.collectAsStateWithLifecycle()
    val notificationState = viewModel.notificationState.collectAsStateWithLifecycle()
    val storagePermissionState = viewModel.storagePermissionState.collectAsStateWithLifecycle()
    val allOptionsValidated = viewModel.allOptionsValidated.collectAsStateWithLifecycle()
    SetOnResume {
        viewModel.emitIntentOnIO(SetupUIIntent.onResume(context))
    }


    SetupScaffold(
        actions = {
            AnimatedVisibility(visible = allOptionsValidated.value.not()) {
                OutlinedButton(
                    onClick = {
                        viewModel.launchOnIO {
                            viewModel.emitIntent(SetupUIIntent.ValidatedRoot)
                            viewModel.emitIntent(SetupUIIntent.ValidateNotifications(context))
                            viewModel.emitIntent(SetupUIIntent.ValidateStoragePermission(context))
                        }
                    }
                ) {
                    Text(text = "Grant All")
                }
            }
            Button(
                enabled = allOptionsValidated.value,
                onClick = {
                    navController.navigateSingle(SetupRoutes.welcomePage.route)
                }
            ){
                Text(text = "Continue")
            }
        }
    ) {


    }
}