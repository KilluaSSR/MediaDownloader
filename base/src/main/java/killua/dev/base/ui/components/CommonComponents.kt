package killua.dev.base.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowOutward
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import db.DownloadStatus
import killua.dev.base.Model.DownloadPageCommands
import killua.dev.base.states.CurrentState
import killua.dev.base.ui.getColorRelationship
import killua.dev.base.ui.tokens.SizeTokens

@Composable
fun Section(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier.Companion.padding(
            SizeTokens.Level16,
            0.dp
        ),
        verticalArrangement = Arrangement.spacedBy(SizeTokens.Level8)
    ) {
        LabelLargeText(
            text = title,
            fontWeight = FontWeight.Companion.SemiBold
        )
        content()
    }
}

@Composable
fun MainButtonSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier.Companion.padding(
            SizeTokens.Level16,
            0.dp
        ),
        verticalArrangement = Arrangement.spacedBy(SizeTokens.Level8),
        horizontalAlignment = Alignment.Companion.CenterHorizontally
    ) {
        LabelLargeText(
            text = title,
            fontWeight = FontWeight.Companion.SemiBold
        )
        content()
    }
}

@Composable
fun ActionButtonContent(
    modifier: Modifier = Modifier.Companion,
    enabled: Boolean = true,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit = {},
    interactionSource: MutableInteractionSource,
    trainlingIcon: @Composable (RowScope.() -> Unit)? = null,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = modifier
            .padding(SizeTokens.Level12)
            .fillMaxWidth(),
        verticalAlignment = Alignment.Companion.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SizeTokens.Level10)
    ) {
        Surface(
            modifier = Modifier.Companion.size(SizeTokens.Level36),
            shape = CircleShape,
            enabled = enabled,
            color = color,
            onClick = onClick,
            contentColor = color,
            interactionSource = interactionSource
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.Companion.size(
                    SizeTokens.Level4
                ),
                tint = getColorRelationship(color)
            )
        }
        content()
        trainlingIcon?.invoke(this)
    }
}


@Composable
fun ActionButton(
    modifier: Modifier = Modifier.Companion,
    enabled: Boolean = true,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit = {},
    trainlingIcon: @Composable (RowScope.() -> Unit)? = null,
    content: @Composable RowScope.() -> Unit,
) {

    val interactionSource =
        remember { MutableInteractionSource() }
    Card(
        modifier = modifier.wrapContentHeight(),
        shape = RoundedCornerShape(SizeTokens.Level16),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = color,
        ),
        enabled = enabled,
        interactionSource = interactionSource,
    ) {
        ActionButtonContent(
            modifier = Modifier.Companion,
            enabled = enabled,
            icon = icon,
            color = color,
            onClick = onClick,
            interactionSource = interactionSource,
            trainlingIcon = trainlingIcon,
            content = content
        )
    }
}

@Composable
fun ClickableConfigurationButton(
    enabled: Boolean = true,
    onClick: () -> Unit,
    title: String,
    state: CurrentState,
    color: Color,
    description: String,
    onSetting: (() -> Unit)? = null
) {
    ActionButton(
        enabled = enabled,
        icon = state.leadingIcon,
        onClick = onClick,
        color = color,
        trainlingIcon = {
            if (onSetting != null) {
                Surface(
                    modifier = Modifier.Companion.size(SizeTokens.Level36),
                    shape = CircleShape,
                    onClick = onSetting
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = null
                    )
                }
            }
        },
    ) {
        Column(
            modifier = Modifier.Companion.weight(
                1f
            )
        ) {
            LabelLargeText(
                text = title,
                color = state.textColor
            )
            BodySmallText(
                text = description,
                color = state.textColor
            )
        }
    }
}

@Composable
fun ActionsBotton(
    modifier: Modifier = Modifier.Companion,
    enabled: Boolean,
    title: String,
    icon: ImageVector,
    color: Color,
    actionIcon: ImageVector? = null,
    onClick: () -> Unit
) {
    ActionButton(
        modifier = modifier,
        enabled = enabled,
        icon = icon,
        color = color,
        onClick = onClick,
        trainlingIcon = {
            if (actionIcon != null)
                Icon(
                    imageVector = actionIcon,
                    contentDescription = null
                )
        }
    ) {
        AutoLabelLargeText(
            modifier = Modifier.Companion.weight(1f),
            text = title,
            enabled = enabled
        )
    }
}

fun Modifier.intrinsicIcon() = layout { measurable, constraints ->
    if (constraints.maxHeight == Constraints.Companion.Infinity) {
        layout(0, 0) {}
    } else {
        val placeable = measurable.measure(constraints)
        layout(placeable.width, placeable.height) {
            placeable.place(0, 0)
        }
    }
}

@Composable
private fun DownloadActionButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String?,
    tint: Color
) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint
        )
    }
}


@Composable
fun DownloadActions(
    status: DownloadStatus,
    onCommand: (DownloadPageCommands) -> Unit
) {
    Row {
        DownloadActionButton(
            onClick = { onCommand(DownloadPageCommands.GoTo) },
            icon = Icons.Default.ArrowOutward,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )

        when (status) {
            DownloadStatus.DOWNLOADING, DownloadStatus.PENDING, DownloadStatus.FAILED -> {
                DownloadActionButton(
                    onClick = { onCommand(DownloadPageCommands.Retry) },
                    icon = Icons.Default.Refresh,
                    contentDescription = "Retry",
                    tint = MaterialTheme.colorScheme.secondary
                )
                DownloadActionButton(
                    onClick = { onCommand(DownloadPageCommands.Cancel) },
                    icon = Icons.Default.Cancel,
                    contentDescription = "Cancel",
                    tint = MaterialTheme.colorScheme.error
                )
            }
            DownloadStatus.COMPLETED -> {
                DownloadActionButton(
                    onClick = { onCommand(DownloadPageCommands.Delete) },
                    icon = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}