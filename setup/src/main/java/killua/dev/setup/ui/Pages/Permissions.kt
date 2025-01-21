package killua.dev.setup.ui.Pages

import android.annotation.SuppressLint
import android.content.Intent
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import killua.dev.base.ActivityUtil
import killua.dev.base.CurrentState
import killua.dev.base.ui.LocalNavController
import killua.dev.base.ui.components.PermissionButton
import killua.dev.base.ui.components.Section
import killua.dev.base.ui.components.SetOnResume
import killua.dev.base.ui.tokens.SizeTokens
import killua.dev.setup.ui.SetupPageViewModel
import killua.dev.setup.ui.SetupScaffold
import killua.dev.setup.ui.SetupUIIntent

@SuppressLint("SetJavaScriptEnabled")
@ExperimentalFoundationApi
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionsPage(viewModel: SetupPageViewModel = viewModel()) {
    val navController = LocalNavController.current!!
    val context = LocalContext.current
    val notificationState = viewModel.notificationState.collectAsStateWithLifecycle()
    val loginState = viewModel.loginState.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
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
                enabled = notificationState.value == CurrentState.Success,
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
                description = "We need to send you notifications to keep you updated.",
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
                    viewModel.launchOnIO {
                        viewModel.emitIntent(
                            SetupUIIntent.ShowWebView(true,context)
                        )
                    }
                },
                color = if (loginState.value == CurrentState.Idle || loginState.value == CurrentState.Error) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer
            )
            if (uiState.isWebViewVisible) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    AndroidView(
                        factory = { context ->
                            WebView(context).apply {
                                settings.apply {
                                    javaScriptEnabled = true
                                    domStorageEnabled = true
                                    javaScriptCanOpenWindowsAutomatically = true
                                }
                                webViewClient = WebViewClient()
                                loadUrl("https://www.baidu.com")
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    // 关闭按钮
                    IconButton(
                        onClick = { viewModel.launchOnIO {
                            viewModel.emitIntent(SetupUIIntent.ShutWebView())
                        } },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "关闭"
                        )
                    }
                }
            }
        }

    }
}