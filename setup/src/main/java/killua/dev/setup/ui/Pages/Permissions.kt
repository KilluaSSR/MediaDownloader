package killua.dev.setup.ui.Pages

import killua.dev.twitterdownloader.Model.CurrentState
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import killua.dev.twitterdownloader.core.utils.ActivityUtil
import killua.dev.setup.ui.SetupPageViewModel
import killua.dev.setup.ui.SetupUIIntent

@ExperimentalFoundationApi
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionsPage(viewModel: SetupPageViewModel = viewModel()) {
    val navController = killua.dev.twitterdownloader.ui.LocalNavController.current!!
    val context = LocalContext.current
//    val rootState = viewModel.rootState.collectAsStateWithLifecycle()
    val notificationState = viewModel.notificationState.collectAsStateWithLifecycle()
//    val storagePermissionState = viewModel.storagePermissionState.collectAsStateWithLifecycle()
//    val allOptionsValidated = viewModel.allOptionsValidated.collectAsStateWithLifecycle()
    killua.dev.twitterdownloader.ui.components.SetOnResume {
        killua.dev.twitterdownloader.ui.BaseViewModel.emitIntentOnIO(SetupUIIntent.OnResume(context))
    }
    killua.dev.twitterdownloader.ui.components.SetupScaffold(
        actions = {
            AnimatedVisibility(visible = notificationState.value != CurrentState.Success) {
                OutlinedButton(
                    onClick = {
                        killua.dev.twitterdownloader.ui.BaseViewModel.launchOnIO {
                            //viewModel.emitIntent(SetupUIIntent.ValidatedRoot)
                            killua.dev.twitterdownloader.ui.BaseViewModel.emitIntent(
                                SetupUIIntent.ValidateNotifications(
                                    context
                                )
                            )
                            killua.dev.twitterdownloader.ui.BaseViewModel.emitIntent(
                                SetupUIIntent.ValidateStoragePermission(
                                    context
                                )
                            )
                        }
                    }
                ) {
                    Text(text = "Grant All")
                }
            }
            Button(
                enabled = notificationState.value == CurrentState.Success,
                onClick = {
                    context.startActivity(Intent(context, ActivityUtil.classMainActivity))
                }
            ) {
                Text(text = "Continue")
            }
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(killua.dev.twitterdownloader.ui.tokens.SizeTokens.Level24)
        ) {


        }
        Spacer(modifier = size(killua.dev.twitterdownloader.ui.tokens.SizeTokens.Level24))
        killua.dev.twitterdownloader.ui.components.Section(title = "We want to ask you for something.") {
            killua.dev.twitterdownloader.ui.components.PermissionButton(
                title = "Notification Permission",
                description = "We need to send you notifications to keep you updated.",
                state = notificationState.value,
                onClick = {
                    killua.dev.twitterdownloader.ui.BaseViewModel.launchOnIO {
                        killua.dev.twitterdownloader.ui.BaseViewModel.emitIntent(
                            SetupUIIntent.ValidateNotifications(
                                context
                            )
                        )
                    }
                },
                color = if (notificationState.value == CurrentState.Idle || notificationState.value == CurrentState.Error) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer
            )
        }
        Spacer(modifier = size(killua.dev.twitterdownloader.ui.tokens.SizeTokens.Level24))
        killua.dev.twitterdownloader.ui.components.Section(title = "You're invited to log in to your Twitter account.") {
            killua.dev.twitterdownloader.ui.components.PermissionButton(
                title = "Notification",
                description = "We need to send you notifications to keep you updated.",
                state = notificationState.value,
                onClick = {
                    killua.dev.twitterdownloader.ui.BaseViewModel.launchOnIO {
                        killua.dev.twitterdownloader.ui.BaseViewModel.emitIntent(
                            SetupUIIntent.ValidateNotifications(
                                context
                            )
                        )
                    }
                },
                color = if (notificationState.value == CurrentState.Idle || notificationState.value == CurrentState.Error) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer
            )
        }

    }
}