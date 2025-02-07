package killua.dev.twitterdownloader.ui.ViewModels

import android.content.Context
import dagger.hilt.android.lifecycle.HiltViewModel
import killua.dev.base.datastore.readApplicationUserAuth
import killua.dev.base.datastore.readApplicationUserCt0
import killua.dev.base.datastore.readLofterLoginAuth
import killua.dev.base.datastore.readLofterLoginKey
import killua.dev.base.states.CurrentState
import killua.dev.base.ui.BaseViewModel
import killua.dev.base.ui.SnackbarUIEffect
import killua.dev.base.ui.UIIntent
import killua.dev.base.ui.UIState
import killua.dev.base.utils.NotificationUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

data class AuthorPageUIState(
    val isLofterLoggedIn: Boolean = false,
    val isEligibleToUseLofterGetByTags: Boolean = false
): UIState

sealed class AuthorPageUIIntent : UIIntent {
    data class OnEntry(val context: Context): AuthorPageUIIntent()
}
@HiltViewModel
class AuthorPageViewModel @Inject constructor(): BaseViewModel<AuthorPageUIIntent, AuthorPageUIState, SnackbarUIEffect>(AuthorPageUIState()) {
    private val mutex = Mutex()
    private val _lofterLoginState: MutableStateFlow<CurrentState> =
        MutableStateFlow(CurrentState.Idle)
    val lofterLoginState: StateFlow<CurrentState> =
        _lofterLoginState.stateInScope(CurrentState.Idle)
    val lofterGetByTagsEligibility: StateFlow<Boolean> = _lofterLoginState.map { login ->
        login == CurrentState.Success
    }.flowOnIO().stateInScope(false)

    override suspend fun onEvent(state: AuthorPageUIState, intent: AuthorPageUIIntent) {
        when(intent){
            is AuthorPageUIIntent.OnEntry -> {
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