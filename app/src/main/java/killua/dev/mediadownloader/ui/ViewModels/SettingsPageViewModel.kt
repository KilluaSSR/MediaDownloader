package killua.dev.mediadownloader.ui.ViewModels

import dagger.hilt.android.lifecycle.HiltViewModel
import killua.dev.mediadownloader.ui.SnackbarUIEffect
import killua.dev.mediadownloader.ui.UIIntent
import killua.dev.mediadownloader.ui.UIState
import javax.inject.Inject
data class a(
    val a0: String = ""
): UIState
@HiltViewModel
class SettingsPageViewModel @Inject constructor(): BaseViewModel<UIIntent, a, SnackbarUIEffect>(a())