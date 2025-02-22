package killua.dev.mediadownloader.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import killua.dev.mediadownloader.Model.AppIcon
import killua.dev.mediadownloader.Model.AvailablePlatforms
import killua.dev.mediadownloader.Model.platformName
import killua.dev.mediadownloader.R
import killua.dev.mediadownloader.Setup.ui.components.SetupScaffold
import killua.dev.mediadownloader.states.CurrentState
import killua.dev.mediadownloader.ui.components.common.CancellableAlert
import killua.dev.mediadownloader.ui.components.common.ClickableConfigurationButton
import killua.dev.mediadownloader.ui.components.common.HeadlineMediumText
import killua.dev.mediadownloader.ui.components.common.Section
import killua.dev.mediadownloader.ui.components.common.paddingTop
import killua.dev.mediadownloader.ui.tokens.SizeTokens
import kotlinx.coroutines.flow.StateFlow

@Composable
fun ConfigurationPage(
    platform: AvailablePlatforms,
    loginStateFlow: StateFlow<CurrentState>,
    eligibilityFlow: StateFlow<Boolean>,
    snackbarHostState: SnackbarHostState? = null,
    onLogin: () -> Unit,
    onLogout: () -> Unit,
    onContinue: () -> Unit,
    additionalSections: @Composable () -> Unit = {}
) {
    val loginState by loginStateFlow.collectAsStateWithLifecycle()
    val eligibility by eligibilityFlow.collectAsStateWithLifecycle()
    rememberCoroutineScope()
    var isShowReset by remember { mutableStateOf(false) }
    val context = LocalContext.current
    SetupScaffold(
        actions = {
            Button(
                enabled = eligibility,
                onClick = onContinue
            ) {
                Text(text = stringResource(R.string.confirm))
            }
        },
        snackbarHostState = snackbarHostState
    ) {
        if (isShowReset) {
            CancellableAlert(
                title = stringResource(R.string.reset_title),
                mainText = stringResource(R.string.reset_desc),
                onDismiss = { isShowReset = false },
                icon = {
                    Icon(
                        Icons.Outlined.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(SizeTokens.Level48)
                    )
                }
            ) {
                onLogout()
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(SizeTokens.Level24)
        ) {
            Column(
                modifier = Modifier.paddingTop(SizeTokens.Level100),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AppIcon(platform)
                HeadlineMediumText(
                    modifier = Modifier.paddingTop(SizeTokens.Level12),
                    text = stringResource(R.string.platform_config_str, context.getString(platformName[platform]!!))
                )
            }

            Section(title = stringResource(R.string.login_to_your_platform_account, context.getString(platformName[platform]!!))) {
                ClickableConfigurationButton(
                    title = stringResource(R.string.log_in),
                    description = stringResource(R.string.cookie_necessary_in_settings, context.getString(platformName[platform]!!)),
                    state = loginState,
                    onClick = {
                        if (loginState != CurrentState.Success) {
                            onLogin()
                        }
                    },
                    color = loginState.backgroundColor
                )
            }

            additionalSections()

            Section(title = stringResource(R.string.log_out)) {
                ClickableConfigurationButton(
                    title = stringResource(R.string.log_out),
                    description = stringResource(R.string.clear_your_platform_cookie, context.getString(platformName[platform]!!)),
                    state = CurrentState.Error,
                    onClick = { isShowReset = true },
                    color = CurrentState.Error.backgroundColor
                )
            }
        }
    }
}