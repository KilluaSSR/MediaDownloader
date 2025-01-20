package killua.dev.base.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height

@androidx.compose.runtime.Composable
fun InnerTopPadding(innerPadding: androidx.compose.foundation.layout.PaddingValues) {
    androidx.compose.foundation.layout.Spacer(
        modifier = androidx.compose.ui.Modifier.Companion
            .fillMaxWidth()
            .height(innerPadding.calculateTopPadding())
    )
}

@androidx.compose.runtime.Composable
fun InnerBottomPadding(innerPadding: androidx.compose.foundation.layout.PaddingValues) {
    androidx.compose.foundation.layout.Spacer(
        modifier = androidx.compose.ui.Modifier.Companion
            .fillMaxWidth()
            .height(innerPadding.calculateBottomPadding())
    )
}