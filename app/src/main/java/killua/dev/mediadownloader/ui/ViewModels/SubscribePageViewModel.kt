package killua.dev.mediadownloader.ui.ViewModels

import dagger.hilt.android.lifecycle.HiltViewModel
import killua.dev.mediadownloader.ui.SnackbarUIEffect
import killua.dev.mediadownloader.ui.UIIntent
import killua.dev.mediadownloader.ui.UIState
import javax.inject.Inject
sealed interface SubscribePageUIIntent: UIIntent

data class SubscribePageUIState(
    val twitterAuthors: List<String> = emptyList(),
): UIState
@HiltViewModel
class SubscribePageViewModel @Inject constructor(

) : BaseViewModel<SubscribePageUIIntent, SubscribePageUIState,SnackbarUIEffect>(SubscribePageUIState())