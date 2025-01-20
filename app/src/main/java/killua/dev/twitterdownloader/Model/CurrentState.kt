package killua.dev.twitterdownloader.Model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.AccessTime
import androidx.compose.material.icons.twotone.Done
import androidx.compose.material.icons.twotone.Error
import androidx.compose.material.icons.twotone.Pending
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

sealed class CurrentState{
    data object Idle: CurrentState()
    data object Processing: CurrentState()
    data object Success: CurrentState()
    data object Error: CurrentState()

    val leadingIcon: ImageVector
        @Composable
        get() = when(this){
            Idle -> Icons.TwoTone.Pending
            Processing -> Icons.TwoTone.AccessTime
            Success -> Icons.TwoTone.Done
            Error -> Icons.TwoTone.Error
        }
    val backgroundColor: Color
    @Composable
    get() = when(this){
        Idle -> MaterialTheme.colorScheme.tertiaryContainer
        Processing -> MaterialTheme.colorScheme.tertiaryContainer
        Success -> MaterialTheme.colorScheme.tertiaryContainer
        Error -> MaterialTheme.colorScheme.errorContainer
    }
    val textColor: Color
    @Composable
    get() = when(this){
        Idle -> MaterialTheme.colorScheme.onTertiaryContainer
        Processing -> MaterialTheme.colorScheme.onTertiaryContainer
        Success -> MaterialTheme.colorScheme.onTertiaryContainer
        Error -> MaterialTheme.colorScheme.onErrorContainer
    }
}