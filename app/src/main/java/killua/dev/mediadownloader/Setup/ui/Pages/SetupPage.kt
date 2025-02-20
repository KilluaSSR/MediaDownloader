package killua.dev.mediadownloader.Setup.ui.Pages

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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import killua.dev.mediadownloader.Setup.ui.SetupPageViewModel
import killua.dev.mediadownloader.Setup.ui.SetupUIIntent
import killua.dev.mediadownloader.Setup.ui.components.SetupScaffold
import killua.dev.mediadownloader.states.CurrentState
import killua.dev.mediadownloader.ui.CookiesRoutes
import killua.dev.mediadownloader.ui.LocalNavController
import killua.dev.mediadownloader.ui.components.common.ClickableConfigurationButton
import killua.dev.mediadownloader.ui.components.common.Section
import killua.dev.mediadownloader.ui.components.common.SetOnResume
import killua.dev.mediadownloader.ui.tokens.SizeTokens
import killua.dev.mediadownloader.utils.getActivity
import killua.dev.mediadownloader.utils.navigateSingle
import killua.dev.mediadownloader.R
@SuppressLint("SetJavaScriptEnabled")
@ExperimentalFoundationApi
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupPage() {
    val viewModel: SetupPageViewModel = viewModel()
    val navController = LocalNavController.current!!
    val context = LocalContext.current
    val notificationState = viewModel.notificationState.collectAsStateWithLifecycle()
    val loginState = viewModel.loginState.collectAsStateWithLifecycle()
    viewModel.eligibility.collectAsStateWithLifecycle()
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
                    Text(text = stringResource(R.string.grant_all))
                }
            }
            Button(
                enabled = true,
                onClick = {
                    viewModel.launchOnIO {
                        viewModel.emitIntent(SetupUIIntent.StartApplication(context))
                    }
                    context.getActivity().finish()
                }
            ) {
                Text(text = stringResource(R.string.confirm))
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
        Section(title = stringResource(R.string.ask_for_sth)) {
            ClickableConfigurationButton(
                title = stringResource(R.string.notification_permission),
                description = stringResource(R.string.notification_permission_desc),
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
                color = notificationState.value.backgroundColor
            )
        }
        Spacer(modifier = Modifier.size(SizeTokens.Level24))
        Section(title = stringResource(R.string.invited_to_login_twitter)) {
            ClickableConfigurationButton(
                title = stringResource(R.string.log_in),
                description = stringResource(R.string.twitter_login_desc),
                state = loginState.value,
                onClick = {
                    if (loginState.value != CurrentState.Success) {
                        navController.navigateSingle(CookiesRoutes.TwitterCookiesBrowser.route)
                    }
                },
                color = loginState.value.backgroundColor
            )

        }

    }
}
