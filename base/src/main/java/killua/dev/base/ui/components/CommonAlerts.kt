package killua.dev.base.ui.components

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