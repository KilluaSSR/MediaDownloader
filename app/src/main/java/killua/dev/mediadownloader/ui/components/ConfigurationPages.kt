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
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import killua.dev.mediadownloader.Model.AppIcon
import killua.dev.mediadownloader.Model.AvailablePlatforms
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

    SetupScaffold(
        actions = {
            Button(
                enabled = eligibility,
                onClick = onContinue
            ) {
                Text(text = "Continue")
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
                    text = "${platform.name} Configurations"
                )
            }

            Section(title = "Log in to your ${platform.name} account") {
                ClickableConfigurationButton(
                    title = "Log in",
                    description = "Your ${platform.name}'s cookie is necessary when downloading pictures from it.",
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

            Section(title = "Log out") {
                ClickableConfigurationButton(
                    title = "Log out",
                    description = "Clear your ${platform.name} account's cookie",
                    state = CurrentState.Error,
                    onClick = { isShowReset = true },
                    color = CurrentState.Error.backgroundColor
                )
            }
        }
    }
}