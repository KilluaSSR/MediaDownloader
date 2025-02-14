package killua.dev.base.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Modifier

@androidx.compose.runtime.Composable
fun InnerTopPadding(innerPadding: PaddingValues) {
    Spacer(
        modifier = Modifier.Companion
            .fillMaxWidth()
            .height(innerPadding.calculateTopPadding())
    )
}

@androidx.compose.runtime.Composable
fun InnerBottomPadding(innerPadding: PaddingValues) {
    Spacer(
        modifier = Modifier.Companion
            .fillMaxWidth()
            .height(innerPadding.calculateBottomPadding())
    )
}