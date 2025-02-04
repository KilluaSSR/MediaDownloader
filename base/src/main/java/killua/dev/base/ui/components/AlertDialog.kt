package killua.dev.base.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import killua.dev.base.ui.tokens.SizeTokens

@Composable
fun OKAlert(
    title: String,
    mainText: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.padding(SizeTokens.Level8),
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
                        end = SizeTokens.Level12,
                        bottom = SizeTokens.Level12,
                        top = SizeTokens.Level12
                    ),
                horizontalArrangement = Arrangement.End
            ) {
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
    )
}