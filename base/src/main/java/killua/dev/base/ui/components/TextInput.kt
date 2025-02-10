package killua.dev.base.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.VisualTransformation
import killua.dev.base.ui.tokens.TextFieldTokens

@Composable
fun MainInputDialog(
    title: String,
    placeholder: String,
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val config = InputDialogConfig(
        title = title,
        placeholder = placeholder,
        singleLine = true
    )

    CommonInputDialog(
        showDialog = showDialog,
        config = config,
        onDismiss = onDismiss,
        onConfirm = onConfirm
    )
}

@Composable
fun AdvancedInputDialog(
    title: String,
    placeholder: String,
    showDialog: Boolean,
    loading: Boolean,
    userInfo: Triple<String, String, String>?,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val config = AdvancedDialogConfig(
        title = title,
        placeholder = placeholder,
        singleLine = true,
        loading = loading,
        userInfo = userInfo
    )

    AdvancedInputDialog(
        showDialog = showDialog,
        config = config,
        onDismiss = onDismiss,
        onConfirm = onConfirm
    )
}

@Composable
fun SetupTextField(
    modifier: Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    value: String,
    leadingIcon: ImageVector,
    trailingIcon: ImageVector? = null,
    onTrailingIconClick: (() -> Unit)? = null,
    prefix: String? = null,
    label: String,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    onClick: (() -> Unit)? = null,
    singleLine: Boolean = true,
    interactionSource: MutableInteractionSource = if (onClick == null)
        remember { MutableInteractionSource() }
    else
        remember { MutableInteractionSource() }
            .also { src ->
                LaunchedEffect(src) {
                    src.interactions.collect {
                        if (it is PressInteraction.Release) {
                            onClick()
                        }
                    }
                }
            },
    onValueChange: (String) -> Unit,
) {
    OutlinedTextField(
        modifier = modifier,
        enabled = enabled,
        readOnly = readOnly,
        value = value,
        singleLine = singleLine,
        leadingIcon = {
            Icon(
                modifier = Modifier
                    .paddingStart(TextFieldTokens.LeadingIconPaddingStart)
                    .size(TextFieldTokens.IconSize),
                imageVector = leadingIcon,
                contentDescription = null,
            )
        },
        trailingIcon = if (trailingIcon == null) null else {
            {
                IconButton(modifier = Modifier.paddingEnd(TextFieldTokens.TrailingIconPaddingEnd), onClick = { onTrailingIconClick?.invoke() }) {
                    Icon(imageVector = trailingIcon, contentDescription = null)
                }
            }
        },
        prefix = if (prefix == null) null else {
            {
                Text(
                    text = prefix
                )
            }
        },
        keyboardOptions = keyboardOptions,
        visualTransformation = visualTransformation,
        shape = CircleShape,
        onValueChange = onValueChange,
        label = { Text(text = label) },
        interactionSource = interactionSource,
    )
}