package killua.dev.twitterdownloader.ui.pages.OtherSetupPages.Lofter

import android.content.Context
import android.webkit.CookieManager
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import killua.dev.base.datastore.readApplicationUserAuth
import killua.dev.base.datastore.readApplicationUserCt0
import killua.dev.base.datastore.readLofterLoginAuth
import killua.dev.base.datastore.readLofterLoginKey
import killua.dev.base.datastore.writeApplicationUserAuth
import killua.dev.base.datastore.writeApplicationUserCt0
import killua.dev.base.states.CurrentState
import killua.dev.base.ui.BaseViewModel
import killua.dev.base.ui.SnackbarUIEffect
import killua.dev.base.utils.NotificationUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import kotlin.text.split


@HiltViewModel
class LofterPreparePageViewModel @Inject constructor(

): BaseViewModel<LofterPreparePageUIIntent, LofterPreparePageUIState,SnackbarUIEffect>(
    LofterPreparePageUIState()
){
    private val mutex = Mutex()
    private val _loginState: MutableStateFlow<CurrentState> =
        MutableStateFlow(CurrentState.Idle)
    val loginState: StateFlow<CurrentState> =
        _loginState.stateInScope(CurrentState.Idle)
    val eligibility: StateFlow<Boolean> = _loginState.map { login ->
        login == CurrentState.Success}.flowOnIO().stateInScope(false)
    val _dateSelectedState: MutableStateFlow<CurrentState> =
        MutableStateFlow(CurrentState.Idle)
    val dateSelectedState = _dateSelectedState.stateInScope(CurrentState.Idle)


    override suspend fun onEvent(
        state: LofterPreparePageUIState,
        intent: LofterPreparePageUIIntent
    ) {
        when(intent){
            is LofterPreparePageUIIntent.OnDateChanged -> TODO()
            is LofterPreparePageUIIntent.OnResume -> {
                mutex.withLock {
                    val loginKey = intent.context.readLofterLoginKey().first()
                    val loginAuth = intent.context.readLofterLoginAuth().first()
                    if(loginKey.isNotBlank() && loginAuth.isNotBlank()){
                        _loginState.value = CurrentState.Success
                    }
                }
            }
            is LofterPreparePageUIIntent.OnTagsChanged -> TODO()
        }
    }
}