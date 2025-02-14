package killua.dev.base.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.window.DialogProperties
import killua.dev.base.ui.tokens.SizeTokens
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BaseAnimatedDialog(
    title: String,
    mainText: String,
    onDismiss: () -> Unit,
    icon: @Composable (() -> Unit)? = null,
    buttons: @Composable (dismissWithAnimation: () -> Unit) -> Unit,
    inAnimDuration: Int = 500,
    outAnimDuration: Int = 450,
    properties: DialogProperties = DialogProperties()
) {
    val scope = rememberCoroutineScope()
    var isDialogVisible by remember { mutableStateOf(false) }
    val animationSpec = tween<Float>(if (isDialogVisible) inAnimDuration else outAnimDuration)

    val dialogAlpha by animateFloatAsState(
        targetValue = if (isDialogVisible) 1f else 0f,
        animationSpec = animationSpec
    )

    val dialogRotationX by animateFloatAsState(
        targetValue = if (isDialogVisible) 0f else -70f,
        animationSpec = animationSpec
    )

    val dialogScale by animateFloatAsState(
        targetValue = if (isDialogVisible) 1f else 0f,
        animationSpec = animationSpec
    )

    val dismissWithAnimation: () -> Unit = {
        scope.launch {
            isDialogVisible = false
            delay(outAnimDuration.toLong())
            onDismiss()
        }
    }

    LaunchedEffect(Unit) {
        isDialogVisible = true
    }

    AlertDialog(
        onDismissRequest = { dismissWithAnimation() },
        properties = properties,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .padding(SizeTokens.Level8)
            .alpha(dialogAlpha)
            .scale(dialogScale)
            .graphicsLayer { rotationX = dialogRotationX },
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
                ),
                fontWeight = FontWeight.Bold
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
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
                buttons(dismissWithAnimation)
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
    BaseAnimatedDialog(
        title = title,
        mainText = mainText,
        onDismiss = onDismiss,
        buttons = { dismissWithAnimation ->
            TextButton(
                onClick = {dismissWithAnimation()}
            ) {
                Text(
                    text = "OK",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    )
}
@Composable
fun CancellableAlert(
    title: String,
    mainText: String,
    icon: @Composable (() -> Unit)? = null,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
){
    BaseAnimatedDialog(
        title = title,
        mainText = mainText,
        onDismiss = onDismiss,
        icon = icon,
        buttons = { dismissWithAnimation ->

            TextButton(onClick = { dismissWithAnimation() }) {
                Text(
                    text = "Cancel",
                    style = MaterialTheme.typography.labelLarge
                )
            }

            TextButton(onClick = {
                onConfirm()
                dismissWithAnimation()
            }) {
                Text(
                    text = "Confirm",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    )

}

