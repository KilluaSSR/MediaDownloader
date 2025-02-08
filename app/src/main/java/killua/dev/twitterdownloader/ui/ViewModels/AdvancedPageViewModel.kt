package killua.dev.twitterdownloader.ui.ViewModels

import android.content.Context
import dagger.hilt.android.lifecycle.HiltViewModel
import killua.dev.base.datastore.readLofterLoginAuth
import killua.dev.base.datastore.readLofterLoginKey
import killua.dev.base.states.CurrentState
import killua.dev.base.ui.BaseViewModel
import killua.dev.base.ui.SnackbarUIEffect
import killua.dev.base.ui.UIIntent
import killua.dev.base.ui.UIState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

data class AdvancedPageUIState(
    val isLofterLoggedIn: Boolean = false,
    val isEligibleToUseLofterGetByTags: Boolean = false
): UIState

sealed class AdvancedPageUIIntent : UIIntent {
    data class OnEntry(val context: Context): AdvancedPageUIIntent()
}
@HiltViewModel
class AdvancedPageViewModel @Inject constructor(): BaseViewModel<AdvancedPageUIIntent, AdvancedPageUIState, SnackbarUIEffect>(AdvancedPageUIState()) {
    private val mutex = Mutex()
    private val _lofterLoginState: MutableStateFlow<CurrentState> =
        MutableStateFlow(CurrentState.Idle)
    val lofterLoginState: StateFlow<CurrentState> =
        _lofterLoginState.stateInScope(CurrentState.Idle)
    val lofterGetByTagsEligibility: StateFlow<Boolean> = _lofterLoginState.map { login ->
        login == CurrentState.Success
    }.flowOnIO().stateInScope(false)

    override suspend fun onEvent(state: AdvancedPageUIState, intent: AdvancedPageUIIntent) {
        when(intent){
            is AdvancedPageUIIntent.OnEntry -> {
                mutex.withLock {
                    val loginKey = intent.context.readLofterLoginKey().first()
                    val loginAuth = intent.context.readLofterLoginAuth().first()
                    if(loginKey.isNotBlank() && loginAuth.isNotBlank()){
                        _lofterLoginState.value = CurrentState.Success
                    }
                }
            }
        }
    }
}