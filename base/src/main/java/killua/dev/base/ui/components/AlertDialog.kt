package killua.dev.base.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import killua.dev.base.ui.tokens.SizeTokens
@Composable
private fun BaseAlertDialog(
    title: String,
    mainText: String,
    onDismiss: () -> Unit,
    icon: @Composable (() -> Unit)? = null,
    buttons: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.padding(SizeTokens.Level8),
        icon = icon,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(
                    start = SizeTokens.Level12,
                    end = SizeTokens.Level12,
                    top = SizeTokens.Level12
                )
            )
        },
        text = {
            Column(
                modifier = Modifier.padding(
                    top = SizeTokens.Level12,
                    start = SizeTokens.Level12,
                    end = SizeTokens.Level12
                )
            ) {
                Text(
                    text = mainText,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = SizeTokens.Level12,
                        end = SizeTokens.Level8,
                        bottom = SizeTokens.Level12,
                        top = SizeTokens.Level2
                    ),
                horizontalArrangement = Arrangement.End
            ) {
                buttons()
            }
        }
    )
}
@Composable
fun OKAlert(
    title: String,
    mainText: String,
    onDismiss: () -> Unit
) {
    BaseAlertDialog(title, mainText, onDismiss, ) {
        TextButton(
            onClick = onDismiss
        ) {
            Text(
                text = "OK",
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}
@Composable
fun CancellableAlert(
    title: String,
    mainText: String,
    icon: @Composable (() -> Unit)? = null,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
){
    BaseAlertDialog(title, mainText, onDismiss, icon ) {
        TextButton(
            onClick = onDismiss
        ) {
            Text(
                text = "Cancel",
                style = MaterialTheme.typography.labelLarge
            )
        }
        TextButton(
            onClick = onConfirm
        ) {
            Text(
                text = "Confirm",
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}