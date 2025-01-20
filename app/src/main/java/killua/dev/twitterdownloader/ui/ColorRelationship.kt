package killua.dev.twitterdownloader.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color

data class ContainerColorPair(
    val container: Color,
    val onContainer: Color
)

@Composable
fun getRandomColors(): List<ContainerColorPair> {
    val colorPairs = listOf(
        ContainerColorPair(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer
        ),
        ContainerColorPair(
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer
        ),
        ContainerColorPair(
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer
        ),
        ContainerColorPair(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer
        )
    )

    return remember {
        colorPairs.shuffled()
    }
}

@Composable fun getColorRelationship(containerColor: Color): Color {
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