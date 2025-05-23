package killua.dev.mediadownloader.ui.components.common

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.VisualTransformation
import killua.dev.mediadownloader.ui.tokens.TextFieldTokens
import killua.dev.mediadownloader.utils.DisabledAlpha

@Composable
fun SearchBar(
    modifier: Modifier = Modifier,
    enabled: Boolean,
    placeholder: String,
    onTextChange: (String) -> Unit
){
    var text by rememberSaveable { mutableStateOf("") }

    CleanableTextField(
        modifier = modifier
            .fillMaxWidth()
            .clip(CircleShape),
        value = text,
        placeholder = placeholder,
        enabled = enabled,
        leadingIcon = Icons.Rounded.Search,
        onCleanClick = {
            text = ""
            onTextChange("")
        },
        onValueChange = {
            text = it
            onTextChange(it)
        },
    )
}
@Composable
fun RoundedTextField(
    modifier: Modifier = Modifier,
    value: String,
    placeholder: String,
    enabled: Boolean,
    visualTransformation: VisualTransformation,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions,
    prefix: String?,
    onValueChange: (String) -> Unit,
) {
    OutlinedTextField(
        modifier = modifier,
        shape = CircleShape,
        value = value,
        enabled = enabled,
        placeholder = { BodyLargeText(text = placeholder) },
        onValueChange = onValueChange,
        singleLine = true,
        visualTransformation = visualTransformation,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        keyboardOptions = keyboardOptions,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = DisabledAlpha),
            errorContainerColor = MaterialTheme.colorScheme.errorContainer,
            unfocusedBorderColor = Color.Transparent,
            disabledBorderColor =  Color.Transparent,
            focusedBorderColor =  Color.Transparent,
        ),
        prefix = if (prefix != null) {
            { Text(prefix) }
        } else {
            null
        },
    )
}
@Composable
fun CleanableTextField(
    modifier: Modifier = Modifier,
    value: String,
    placeholder: String,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    prefix: String? = null,
    onCleanClick: () -> Unit,
    onValueChange: (String) -> Unit,
) {
    RoundedTextField(
        modifier = modifier,
        value = value,
        placeholder = placeholder,
        enabled = enabled,
        visualTransformation = VisualTransformation.None,
        leadingIcon = if (leadingIcon != null) {
            {
                Icon(
                    modifier = Modifier
                        .paddingStart(TextFieldTokens.LeadingIconPaddingStart)
                        .size(TextFieldTokens.IconSize),
                    imageVector = leadingIcon,
                    contentDescription = null,
                )
            }
        } else {
            null
        },
        trailingIcon = if (value.isNotEmpty() && enabled) {
            {
                IconButton(
                    modifier = Modifier.paddingEnd(TextFieldTokens.TrailingIconPaddingEnd),
                    onClick = onCleanClick
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = null
                    )
                }
            }
        } else {
            null
        },
        keyboardOptions = keyboardOptions,
        prefix = prefix,
        onValueChange = onValueChange
    )
}