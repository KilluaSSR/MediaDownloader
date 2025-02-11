package killua.dev.twitterdownloader.ui.pages.OtherSetupPages.Pixiv

import android.annotation.SuppressLint
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import killua.dev.base.Model.AppIcon
import killua.dev.base.Model.AvailablePlatforms
import killua.dev.base.datastore.writePixivPHPSSID
import killua.dev.base.states.CurrentState
import killua.dev.base.ui.CookiesRoutes
import killua.dev.base.ui.LocalNavController
import killua.dev.base.ui.components.CancellableAlert
import killua.dev.base.ui.components.ClickableConfigurationButton
import killua.dev.base.ui.components.HeadlineMediumText
import killua.dev.base.ui.components.Section
import killua.dev.base.ui.components.paddingTop
import killua.dev.base.ui.tokens.SizeTokens
import killua.dev.base.utils.navigateSingle
import killua.dev.setup.ui.components.SetupScaffold
import killua.dev.twitterdownloader.ui.pages.OtherSetupPages.PreparePageUIIntent
import killua.dev.twitterdownloader.ui.pages.OtherSetupPages.PreparePageViewModel
import kotlinx.coroutines.launch

@SuppressLint("SetJavaScriptEnabled")
@ExperimentalFoundationApi
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PixivPreparePage() {
    val viewModel: PreparePageViewModel = hiltViewModel()
    val navController = LocalNavController.current!!
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val loginState = viewModel.pixivLoginState.collectAsStateWithLifecycle()
    val eligibility = viewModel.pixivEligibility.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        viewModel.emitIntentOnIO(PreparePageUIIntent.OnEntryPixiv(context))
    }
    SetupScaffold(
        actions = {
            Button(
                enabled = eligibility.value,
                onClick = {
                    navController.popBackStack()
                }
            ) {
                Text(text = "Continue")
            }
        }
    ) {
        var isShowReset by remember { mutableStateOf(false) }
        if(isShowReset){
            CancellableAlert(
                title = "Reset now?",
                mainText = "Your login information will be cleared, and you will need to log in again to continue using Pixiv's functions",
                onDismiss = {isShowReset = false},
                icon = {
                    Icon(
                        Icons.Outlined.Warning,
                        contentDescription = null ,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .size(SizeTokens.Level48))
                }
            ) {
                scope.launch{
                    context.writePixivPHPSSID("")
                    viewModel.emitIntent(PreparePageUIIntent.OnPixivLoggedOut)
                }
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(SizeTokens.Level24)
        ) {
            Column(
                modifier = Modifier
                    .paddingTop(SizeTokens.Level100),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AppIcon(AvailablePlatforms.Pixiv)
                HeadlineMediumText(modifier = Modifier.paddingTop(SizeTokens.Level12), text = "Pixiv Configurations")
            }

            Section(title = "Log in to your Pixiv account") {
                ClickableConfigurationButton(
                    title = "Log in",
                    description = "Your Pixiv's cookie is necessary when downloading pictures from it.",
                    state = loginState.value,
                    onClick = {
                        if(loginState.value != CurrentState.Success){
                            navController.navigateSingle(CookiesRoutes.PixivCookiesBrowser.route)
                        }
                    },
                    color = if (loginState.value == CurrentState.Idle || loginState.value == CurrentState.Error) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer
                )
            }

            Section(title = "Log out") {
                ClickableConfigurationButton(
                    title = "Log out",
                    description = "Clear your Pixiv account's cookie",
                    state = CurrentState.Error,
                    onClick = {
                        isShowReset = true
                    },
                    color = MaterialTheme.colorScheme.errorContainer
                )
            }
        }
    }
}
