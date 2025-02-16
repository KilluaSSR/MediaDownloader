package killua.dev.mediadownloader.Setup.ui

import android.content.Context
import killua.dev.mediadownloader.ui.UIIntent
import killua.dev.mediadownloader.ui.UIState

data class SetupUIState(
    val isLoggedIn: Boolean = false
) : UIState

sealed class SetupUIIntent : UIIntent {
    data class ValidateNotifications(val context: Context) : SetupUIIntent()
    data class OnResume(val context: Context) : SetupUIIntent()
    data class StartApplication(val context: Context) : SetupUIIntent()
}