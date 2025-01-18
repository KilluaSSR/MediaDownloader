package killua.dev.setup

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.Pending
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

sealed class CurrentState{
    data object Idle: CurrentState()
    data object Processing: CurrentState()
    data object Success: CurrentState()
    data object Error: CurrentState()

    val leadingIcon: ImageVector
        @Composable
        get() = when(this){
            Idle -> Icons.Rounded.Pending
            Processing -> Icons.Rounded.AccessTime
            Success -> Icons.Rounded.Done
            Error -> Icons.Rounded.Error
        }
}