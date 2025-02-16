package killua.dev.mediadownloader.ui.components.common

import androidx.compose.runtime.Composable

@Composable
fun DevelopingAlert(
    onDismiss: () -> Unit
) {
    OKAlert("Sorry","This feature is under development, it's unavailable, currently.",onDismiss)
}

@Composable
fun FileNotFountAlert(
    onDismiss: () -> Unit
) {
    OKAlert("File not found","The file may have been moved or deleted.",onDismiss)
}