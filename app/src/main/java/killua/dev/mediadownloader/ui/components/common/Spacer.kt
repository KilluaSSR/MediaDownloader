package killua.dev.mediadownloader.ui.components.common

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun InnerTopPadding(innerPadding: PaddingValues) {
    Spacer(
        modifier = Modifier.Companion
            .fillMaxWidth()
            .height(innerPadding.calculateTopPadding())
    )
}

@Composable
fun InnerBottomPadding(innerPadding: PaddingValues) {
    Spacer(
        modifier = Modifier.Companion
            .fillMaxWidth()
            .height(innerPadding.calculateBottomPadding())
    )
}