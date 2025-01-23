package killua.dev.setup.ui.Pages

import android.annotation.SuppressLint
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
import killua.dev.base.CurrentState
import killua.dev.base.ui.LocalNavController
import killua.dev.base.ui.components.PermissionButton
import killua.dev.base.ui.components.Section
import killua.dev.base.ui.components.SetOnResume
import killua.dev.base.ui.tokens.SizeTokens
import killua.dev.base.utils.navigateSingle
import killua.dev.setup.SetupRoutes
import killua.dev.setup.ui.SetupPageViewModel
import killua.dev.setup.ui.SetupScaffold
import killua.dev.setup.ui.SetupUIIntent

@SuppressLint("SetJavaScriptEnabled")
@ExperimentalFoundationApi
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionsPage() {
    val viewModel: SetupPageViewModel = viewModel()
    val navController = LocalNavController.current!!
    val context = LocalContext.current
    val notificationState = viewModel.notificationState.collectAsStateWithLifecycle()
    val loginState = viewModel.loginState.collectAsStateWithLifecycle()
    val eligibility = viewModel.eligibility.collectAsStateWithLifecycle()
    SetOnResume {
        viewModel.emitIntentOnIO(SetupUIIntent.OnResume(context))
    }
    SetupScaffold(
        actions = {
            AnimatedVisibility(visible = notificationState.value != CurrentState.Success) {
                OutlinedButton(
                    onClick = {
                        viewModel.launchOnIO {
                            viewModel.emitIntent(
                                SetupUIIntent.ValidateNotifications(
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
                enabled = eligibility.value,
                onClick = {
                    viewModel.launchOnIO {
                        viewModel.emitIntent(SetupUIIntent.StartApplication(context))
                    }
                }
            ) {
                Text(text = "Continue")
            }
        }
    ) {

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(SizeTokens.Level24)
        ) {

        }
        Spacer(modifier = Modifier.size(SizeTokens.Level24))
        Section(title = "We want to ask you for something.") {
            PermissionButton(
                title = "Notification Permission",
                description = "We want to send you notifications to keep you updated.",
                state = notificationState.value,
                onClick = {
                    viewModel.launchOnIO {
                        viewModel.emitIntent(
                            SetupUIIntent.ValidateNotifications(
                                context
                            )
                        )
                    }
                },
                color = if (notificationState.value == CurrentState.Idle || notificationState.value == CurrentState.Error) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer
            )
        }
        Spacer(modifier = Modifier.size(SizeTokens.Level24))
        Section(title = "You're invited to log in to your Twitter account.") {
            PermissionButton(
                title = "Log in",
                description = "We need your account's cookie to download videos.",
                state = loginState.value,
                onClick = {
                   if(loginState.value != CurrentState.Success){
                       navController.navigateSingle(SetupRoutes.BrowserPage.route)
                   }
                },
                color = if (loginState.value == CurrentState.Idle || loginState.value == CurrentState.Error) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer
            )

        }

    }
}
