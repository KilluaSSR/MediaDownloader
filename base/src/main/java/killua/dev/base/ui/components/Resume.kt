package killua.dev.base.ui.components

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

@androidx.compose.runtime.Composable
fun SetOnResume(onResume: () -> Unit) {
    val owner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    androidx.compose.runtime.DisposableEffect(owner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                onResume()
            }
        }
        owner.lifecycle.addObserver(observer)
        onDispose {
            owner.lifecycle.removeObserver(observer)
        }
    }
}