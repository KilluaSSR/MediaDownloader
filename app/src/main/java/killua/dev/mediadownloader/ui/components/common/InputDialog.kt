package killua.dev.mediadownloader.ui.components.common

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.with
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import killua.dev.mediadownloader.R
import killua.dev.mediadownloader.ui.tokens.SizeTokens
import kotlinx.coroutines.delay

data class InputDialogConfig(
    val title: String,
    val placeholder: String = "",
    val singleLine: Boolean = true
)

data class AdvancedDialogConfig(
    val title: String,
    val placeholder: String,
    val singleLine: Boolean = true,
    val cancelText: String = "Cancel",
    val confirmText: String = "Confirm",
    val loading: Boolean = false,
    val userInfo: Triple<String, String, String>? = null
)

@Composable
fun CommonInputDialog(
    showDialog: Boolean,
    config: InputDialogConfig,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var inputText by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(showDialog) {
        if (!showDialog) {
            inputText = ""
        }
    }

    if (showDialog) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            )
        ) {
            Surface(
                shape = RoundedCornerShape(SizeTokens.Level8),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .padding(SizeTokens.Level16)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = config.title,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(SizeTokens.Level16))

                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        placeholder = { Text(config.placeholder) },
                        singleLine = config.singleLine
                    )

                    Spacer(modifier = Modifier.height(SizeTokens.Level24))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = {
                                inputText = ""
                                onDismiss()
                            }
                        ) {
                            Text(stringResource(R.string.cancel))
                        }

                        Spacer(modifier = Modifier.width(SizeTokens.Level8))

                        Button(
                            onClick = {
                                onConfirm(inputText)
                                onDismiss()
                            }
                        ) {
                            Text(stringResource(R.string.confirm))
                        }
                    }
                }
            }
        }

        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AdvancedInputDialog(
    showDialog: Boolean,
    config: AdvancedDialogConfig,
    onDismiss: () -> Unit,
    onCancel: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var inputText by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    var shouldRequestFocus by remember { mutableStateOf(false) }
    LaunchedEffect(showDialog) {
        if (!showDialog) {
            inputText = ""
        }
    }
    if (showDialog) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            )
        ) {
            Surface(
                shape = RoundedCornerShape(SizeTokens.Level8),
                color = MaterialTheme.colorScheme.surface,
            ) {
                Column(
                    modifier = Modifier.padding(SizeTokens.Level16),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = config.title,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(SizeTokens.Level16))

                    AnimatedContent(
                        targetState = when {
                            config.loading -> DialogState.Loading
                            config.userInfo != null -> DialogState.UserInfo(config.userInfo)
                            else -> DialogState.Input
                        },
                        transitionSpec = {
                            fadeIn() + scaleIn() with fadeOut() + scaleOut()
                        },
                        label = "dialogContent"
                    ) { state ->
                        when (state) {
                            DialogState.Loading -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                                shouldRequestFocus = false
                            }
                            is DialogState.UserInfo -> {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = state.info.second,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        text = "@${state.info.third}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "ID: ${state.info.first}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                shouldRequestFocus = false
                            }
                            DialogState.Input -> {
                                OutlinedTextField(
                                    value = inputText,
                                    onValueChange = { inputText = it },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .focusRequester(focusRequester),
                                    placeholder = { Text(config.placeholder) },
                                    singleLine = true
                                )
                                shouldRequestFocus = true
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(SizeTokens.Level24))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = {
                                inputText = ""
                                onCancel()
                            }
                        ) {
                            Text(stringResource(R.string.cancel))
                        }

                        Spacer(modifier = Modifier.width(SizeTokens.Level8))

                        Button(
                            onClick = {
                                if (config.userInfo != null) {
                                    onConfirm(inputText)
                                    inputText = ""
                                    onDismiss()
                                } else {
                                    onConfirm(inputText)
                                    inputText = ""
                                }
                            },
                            enabled = when {
                                config.loading -> false
                                config.userInfo != null -> true
                                else -> inputText.isNotEmpty()
                            }
                        ) {
                            Text(stringResource(R.string.confirm))
                        }
                    }
                }
            }
        }

        LaunchedEffect(shouldRequestFocus) {
            if (shouldRequestFocus) {
                delay(100)
                try {
                    focusRequester.requestFocus()
                } catch (e: Exception) {
                    // 忽略焦点请求失败
                }
            }
        }
    }
}

private sealed class DialogState {
    object Loading : DialogState()
    object Input : DialogState()
    data class UserInfo(val info: Triple<String, String, String>) : DialogState()
}