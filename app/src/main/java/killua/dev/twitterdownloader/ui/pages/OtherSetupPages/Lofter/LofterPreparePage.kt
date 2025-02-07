package killua.dev.twitterdownloader.ui.pages.OtherSetupPages.Lofter

import android.annotation.SuppressLint
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import killua.dev.base.datastore.readLofterLoginKey
import killua.dev.base.states.CurrentState
import killua.dev.base.ui.CookiesRoutes
import killua.dev.base.ui.LocalNavController
import killua.dev.base.ui.components.PermissionButton
import killua.dev.base.ui.components.Section
import killua.dev.base.ui.components.SetOnResume
import killua.dev.base.ui.tokens.SizeTokens
import killua.dev.base.utils.navigateSingle
import killua.dev.setup.SetupRoutes
import killua.dev.setup.ui.components.SetupScaffold
import kotlinx.coroutines.flow.first

@SuppressLint("SetJavaScriptEnabled")
@ExperimentalFoundationApi
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LofterPreparePage() {
    val viewModel: LofterPreparePageViewModel = viewModel()
    val navController = LocalNavController.current!!
    val context = LocalContext.current
    val loginState = viewModel.loginState.collectAsStateWithLifecycle()
    val eligibility = viewModel.eligibility.collectAsStateWithLifecycle()
    SetOnResume {
        viewModel.emitIntentOnIO(LofterPreparePageUIIntent.OnResume(context))
    }

    SetupScaffold(
        actions = {
            Button(
                enabled = eligibility.value,
                onClick = {
                    viewModel.launchOnIO {
                        navController.popBackStack()
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
        Section(title = "Log in to your Lofter account") {
            PermissionButton(
                title = "Log in",
                description = "Your cookie is necessary when downloading pictures from Lofter.",
                state = loginState.value,
                onClick = {
                    if(loginState.value != CurrentState.Success){
                        navController.navigateSingle(CookiesRoutes.LofterCookiesBrowser.route)
                    }
                },
                color = if (loginState.value == CurrentState.Idle || loginState.value == CurrentState.Error) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer
            )
        }
        Spacer(modifier = Modifier.size(SizeTokens.Level24))
        Section(title = "Date picker") {
            PermissionButton(
                title = "Range",
                description = "We need your account's cookie to download videos.",
                state = loginState.value,
                onClick = {

                },
                color = if (loginState.value == CurrentState.Idle || loginState.value == CurrentState.Error) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer
            )
        }

        Section(title = "Tags") {
            PermissionButton(
                title = "Range",
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
