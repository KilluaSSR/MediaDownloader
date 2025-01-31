package killua.dev.twitterdownloader.ui


import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import killua.dev.base.datastore.readStoreBoolean
import killua.dev.base.datastore.saveStoreBoolean
import killua.dev.base.ui.animations.AnimatedTextContainer
import killua.dev.base.ui.components.TitleLargeText
import killua.dev.base.ui.components.TitleSmallText
import killua.dev.base.ui.components.paddingBottom
import killua.dev.base.ui.components.paddingHorizontal
import killua.dev.base.ui.components.paddingTop
import killua.dev.base.ui.components.paddingVertical
import killua.dev.base.ui.tokens.SizeTokens
import killua.dev.base.utils.withState
import kotlinx.coroutines.launch

@Composable
fun Clickable(
    enabled: Boolean = true,
    desc: String? = null,
    descPadding: Boolean = false,
    onClick: () -> Unit, indication: Indication? = ripple(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable BoxScope.() -> Unit
) {
    Column {
        Surface(
            enabled = enabled,
            modifier = Modifier.Companion
                .fillMaxWidth()
                .heightIn(min = SizeTokens.Level80),
            onClick = onClick,
            interactionSource = interactionSource
        ) {
            Box(
                modifier = if (descPadding) {
                    Modifier.Companion
                        .paddingHorizontal(SizeTokens.Level24)
                        .paddingTop(SizeTokens.Level16)
                } else {
                    Modifier.Companion
                        .paddingHorizontal(SizeTokens.Level24)
                        .paddingVertical(SizeTokens.Level16)
                },
                contentAlignment = Alignment.Companion.Center
            ) {
                content()
            }
        }
        if (desc != null)
            TitleSmallText(
                modifier = if (descPadding) {
                    Modifier.Companion
                        .paddingHorizontal(SizeTokens.Level24)
                        .paddingBottom(SizeTokens.Level16)
                } else {
                    Modifier.Companion.paddingHorizontal(SizeTokens.Level24)
                },
                enabled = enabled,
                text = desc,
                fontWeight = FontWeight.Companion.Normal
            )
    }
}

@ExperimentalAnimationApi
@Composable
fun Clickable(
    enabled: Boolean = true,
    readOnly: Boolean = false,
    title: String,
    value: String? = null,
    desc: String? = null,
    leadingContent: (@Composable RowScope.() -> Unit)? = null,
    trailingContent: (@Composable RowScope.() -> Unit)? = null,
    onClick: () -> Unit = {}
) {
    Clickable(
        enabled = enabled,
        desc = desc,
        onClick = onClick,
        indication = if (readOnly) null else ripple()
    ) {
        Row(
            modifier = Modifier.Companion.height(IntrinsicSize.Min),
            verticalAlignment = Alignment.Companion.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(
                SizeTokens.Level16
            )
        ) {
            if (leadingContent != null) leadingContent()
            Column(
                modifier = Modifier.Companion.weight(
                    1f
                )
            ) {
                AnimatedTextContainer(targetState = title) { text ->
                    TitleLargeText(
                        enabled = enabled,
                        text = text,
                        fontWeight = FontWeight.Companion.Normal
                    )
                }
                if (value != null) AnimatedTextContainer(targetState = value) { text ->
                    TitleSmallText(
                        enabled = enabled,
                        text = text,
                        fontWeight = FontWeight.Companion.Normal
                    )
                }
            }
            if (trailingContent != null) trailingContent()
        }
    }
}

@ExperimentalAnimationApi
@Composable
fun Clickable(
    enabled: Boolean = true,
    icon: ImageVector? = null,
    title: String,
    value: String? = null,
    desc: String? = null,
    onClick: () -> Unit = {}
) {
    Clickable(
        enabled = enabled,
        title = title,
        value = value,
        desc = desc,
        leadingContent = {
            if (icon != null) Icon(
                imageVector = icon,
                contentDescription = null
            )
        },
        onClick = onClick
    )
}

@ExperimentalAnimationApi
@Composable
fun Clickable(
    enabled: Boolean = true,
    title: String, value: String? = null,
    desc: String? = null,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable (ColumnScope.() -> Unit)? = null,
    onClick: () -> Unit = {}
) {
    Clickable(
        enabled = enabled,
        desc = desc,
        onClick = onClick,
        indication = ripple(),
        interactionSource = interactionSource
    ) {
        Row(
            modifier = Modifier.Companion.height(IntrinsicSize.Min),
            verticalAlignment = Alignment.Companion.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(
                SizeTokens.Level16
            )
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = LocalContentColor.current.withState(enabled)
                )
            }
            Column(
                modifier = Modifier.Companion.weight(
                    1f
                )
            ) {
                AnimatedTextContainer(targetState = title) { text ->
                    TitleLargeText(
                        enabled = enabled,
                        text = text,
                        fontWeight = FontWeight.Companion.Normal
                    )
                }
                if (value != null) AnimatedTextContainer(targetState = value) { text ->
                    TitleSmallText(
                        enabled = enabled,
                        text = text,
                        fontWeight = FontWeight.Companion.Normal
                    )
                }
                content?.invoke(this)
            }
            if (trailingIcon != null) {
                Icon(
                    imageVector = trailingIcon,
                    contentDescription = null,
                    tint = LocalContentColor.current.withState(enabled)
                )
            }
        }
    }
}

@ExperimentalAnimationApi
@Composable
fun Clickable(
    enabled: Boolean = true,
    title: String, value: String? = null,
    desc: String? = null,
    leadingIcon: @Composable (RowScope.() -> Unit)? = null,
    trailingIcon: @Composable (RowScope.() -> Unit)? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable (ColumnScope.() -> Unit)? = null,
    onClick: () -> Unit = {}
) {
    Clickable(
        enabled = enabled,
        desc = desc,
        onClick = onClick,
        indication = ripple(),
        interactionSource = interactionSource
    ) {
        Row(
            modifier = Modifier.Companion.height(IntrinsicSize.Min),
            verticalAlignment = Alignment.Companion.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(
                SizeTokens.Level16
            )
        ) {
            leadingIcon?.invoke(this)
            Column(
                modifier = Modifier.Companion.weight(
                    1f
                )
            ) {
                AnimatedTextContainer(targetState = title) { text ->
                    TitleLargeText(
                        enabled = enabled,
                        text = text,
                        fontWeight = FontWeight.Companion.Normal
                    )
                }
                if (value != null) AnimatedTextContainer(targetState = value) { text ->
                    TitleSmallText(
                        enabled = enabled,
                        text = text,
                        fontWeight = FontWeight.Companion.Normal
                    )
                }
                content?.invoke(this)
            }
            trailingIcon?.invoke(this)
        }
    }
}


@ExperimentalAnimationApi
@Composable
fun Selectable(
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    title: String,
    value: String? = null,
    desc: String? = null,
    current: String,
    onClick: suspend () -> Unit = suspend {}
) {
    val scope = rememberCoroutineScope()
    Clickable(
        enabled = enabled,
        title = title,
        value = value,
        desc = desc,
        leadingContent = if (leadingIcon == null) null else {
            {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null
                )
            }
        },
        trailingContent = {
            FilledTonalButton(
                enabled = enabled,
                onClick = { scope.launch { onClick() } }) {

                Text(text = current)
            }
        },
        onClick = { scope.launch { onClick() } }
    )
}

@ExperimentalAnimationApi
@Composable
fun Switchable(
    enabled: Boolean = true,
    checked: Boolean,
    icon: ImageVector? = null,
    title: String,
    checkedText: String,
    notCheckedText: String = checkedText,
    desc: String? = null,
    onCheckedChange: (Boolean) -> Unit = {}
) {
    Clickable(
        enabled = enabled,
        title = title,
        value = if (checked) checkedText else notCheckedText,
        desc = desc,
        leadingContent = {
            if (icon != null) Icon(
                imageVector = icon,
                contentDescription = null
            )
        },
        trailingContent = {
            HorizontalDivider(
                modifier = Modifier
                    .height(SizeTokens.Level36)
                    .width(SizeTokens.Level1)
                    .fillMaxHeight()
            )
            Switch(
                modifier = Modifier.Companion,
                enabled = enabled,
                checked = checked,
                onCheckedChange = { onCheckedChange.invoke(checked) }
            )
        },
        onClick = {
            onCheckedChange.invoke(checked)
        }
    )
}

@ExperimentalAnimationApi
@Composable
fun Switchable(
    enabled: Boolean = true,
    key: Preferences.Key<Boolean>,
    defValue: Boolean = true,
    icon: ImageVector? = null,
    title: String,
    checkedText: String,
    notCheckedText: String = checkedText,
    desc: String? = null,
    onCheckedChange: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val stored by context.readStoreBoolean(key = key, defValue = defValue)
        .collectAsStateWithLifecycle(initialValue = defValue)
    val onClick: suspend (Boolean) -> Unit = {
        context.saveStoreBoolean(key = key, value = it.not())
        onCheckedChange(it.not())
    }

    Switchable(
        enabled = enabled,
        checked = stored,
        icon = icon,
        title = title,
        checkedText = checkedText,
        notCheckedText = notCheckedText,
        desc = desc,
        onCheckedChange = {
            scope.launch {
                onClick(stored)
            }
        }
    )
}

@ExperimentalAnimationApi
@Composable
private fun Slideable(
    enabled: Boolean = true,
    readOnly: Boolean = false,
    title: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    desc: String? = null,
    leadingContent: (@Composable RowScope.() -> Unit)? = null,
    trailingContent: (@Composable RowScope.() -> Unit)? = null,
    onClick: () -> Unit = {}
) {
    Clickable(
        enabled = enabled,
        desc = desc,
        descPadding = true,
        onClick = onClick,
        indication = if (readOnly) null else ripple()
    ) {
        Row(
            modifier = Modifier.Companion.height(IntrinsicSize.Min),
            verticalAlignment = Alignment.Companion.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(
                SizeTokens.Level16
            )
        ) {
            if (leadingContent != null) leadingContent()
            Column(
                modifier = Modifier.Companion.weight(
                    1f
                )
            ) {
                AnimatedTextContainer(targetState = title) { text ->
                    TitleLargeText(
                        enabled = enabled,
                        text = text,
                        fontWeight = FontWeight.Companion.Normal
                    )
                }
                Slider(
                    value = value,
                    onValueChange = onValueChange,
                    steps = steps,
                    valueRange = valueRange
                )
            }
            if (trailingContent != null) trailingContent()
        }
    }
}

@ExperimentalAnimationApi
@Composable
fun Slideable(
    enabled: Boolean = true,
    icon: ImageVector? = null,
    title: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    desc: String? = null,
    onValueChange: (Float) -> Unit,
) {
    Slideable(
        enabled = enabled,
        title = title,
        value = value,
        valueRange = valueRange,
        steps = steps,
        desc = desc,
        onValueChange = onValueChange,
        leadingContent = {
            if (icon != null) Icon(
                imageVector = icon,
                contentDescription = null
            )
        },
        onClick = {}
    )
}

@ExperimentalAnimationApi
@Composable
fun Checkable(
    enabled: Boolean = true,
    checked: Boolean,
    icon: ImageVector? = null,
    title: String,
    value: String,
    desc: String? = null,
    onCheckedChange: (Boolean) -> Unit = {}
) {
    Clickable(
        enabled = enabled,
        title = title,
        value = value,
        desc = desc,
        leadingContent = {
            if (icon != null) Icon(
                imageVector = icon,
                contentDescription = null
            )
        },
        onClick = {
            onCheckedChange(checked)
        }
    )
}

@Composable
fun Title(
    enabled: Boolean = true,
    title: String,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    color: Color = MaterialTheme.colorScheme.primary,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column {
        TitleSmallText(
            modifier = Modifier.Companion
                .paddingHorizontal(SizeTokens.Level24)
                .paddingVertical(SizeTokens.Level12),
            enabled = enabled,
            text = title,
            fontWeight = FontWeight.Companion.Medium,
            color = color
        )
        Column(verticalArrangement = verticalArrangement) {
            content()
        }
    }
}
