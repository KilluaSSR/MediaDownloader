package killua.dev.base.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import killua.dev.base.ui.tokens.SizeTokens

data class InputDialogConfig(
    val title: String,
    val placeholder: String = "",
    val confirmText: String = "Confirm",
    val cancelText: String = "Cancel",
    val singleLine: Boolean = true
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
                        .fillMaxWidth()
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
                            Text(config.cancelText)
                        }

                        Spacer(modifier = Modifier.width(SizeTokens.Level8))

                        Button(
                            onClick = {
                                onConfirm(inputText)
                                onDismiss()
                            }
                        ) {
                            Text(config.confirmText)
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