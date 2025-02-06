package killua.dev.base.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color

data class ContainerColorPair(
    val container: Color,
    val onContainer: Color
)
@Composable
fun getRandomColors(): List<ContainerColorPair> {
    val colorScheme = MaterialTheme.colorScheme

    val colorPairs = remember(colorScheme) {
        listOf(
            ContainerColorPair(
                colorScheme.primaryContainer,
                colorScheme.onPrimaryContainer
            ),
            ContainerColorPair(
                colorScheme.surfaceContainer,
                colorScheme.onSurface

            ),
            ContainerColorPair(
                colorScheme.tertiaryContainer,
                colorScheme.onTertiaryContainer
            ),
            ContainerColorPair(
                colorScheme.errorContainer,
                colorScheme.onErrorContainer
            ),
            ContainerColorPair(
                colorScheme.surfaceVariant,
                colorScheme.onSurfaceVariant
            )
        )
    }

    return remember(colorPairs) {
        derivedStateOf { colorPairs.shuffled() }
    }.value
}
@Composable
fun getColorRelationship(containerColor: Color): Color {
    return when (containerColor) {
        MaterialTheme.colorScheme.tertiaryContainer -> MaterialTheme.colorScheme.onTertiaryContainer
        MaterialTheme.colorScheme.primaryContainer -> MaterialTheme.colorScheme.onPrimaryContainer
        MaterialTheme.colorScheme.errorContainer -> MaterialTheme.colorScheme.onErrorContainer
        MaterialTheme.colorScheme.secondaryContainer -> MaterialTheme.colorScheme.onSecondaryContainer
        MaterialTheme.colorScheme.surface -> MaterialTheme.colorScheme.onSurface
        MaterialTheme.colorScheme.background -> MaterialTheme.colorScheme.onBackground
        MaterialTheme.colorScheme.primary -> MaterialTheme.colorScheme.onPrimary
        else -> Color.Unspecified
    }
}