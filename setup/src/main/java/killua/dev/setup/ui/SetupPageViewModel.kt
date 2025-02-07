package killua.dev.setup.ui

import android.content.Context
import android.content.Intent
import android.webkit.CookieManager
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import killua.dev.base.states.CurrentState
import killua.dev.base.datastore.readApplicationUserAuth
import killua.dev.base.datastore.readApplicationUserCt0
import killua.dev.base.datastore.writeApplicationUserAuth
import killua.dev.base.datastore.writeApplicationUserCt0
import killua.dev.base.ui.BaseViewModel
import killua.dev.base.ui.SnackbarUIEffect
import killua.dev.base.utils.ActivityUtil
import killua.dev.base.utils.NotificationUtils
import killua.dev.base.utils.getActivity
import killua.dev.setup.ui.SetupUIIntent.ValidateNotifications
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

@HiltViewModel
class SetupPageViewModel @Inject constructor() :
    BaseViewModel<SetupUIIntent, SetupUIState, SnackbarUIEffect>(SetupUIState(false)) {
    private val mutex = Mutex()
    private val _notificationState: MutableStateFlow<CurrentState> =
        MutableStateFlow(CurrentState.Idle)
    val notificationState: StateFlow<CurrentState> =
        _notificationState.stateInScope(CurrentState.Idle)
    private val _loginState: MutableStateFlow<CurrentState> =
        MutableStateFlow(CurrentState.Idle)
    val loginState: StateFlow<CurrentState> =
        _loginState.stateInScope(CurrentState.Idle)
    val eligibility: StateFlow<Boolean> = _loginState.map { login ->
        login == CurrentState.Success
    }.flowOnIO().stateInScope(false)
    override suspend fun onEvent(state: SetupUIState, intent: SetupUIIntent) {
        when (intent) {
            is ValidateNotifications -> {
                mutex.withLock {
                    if (notificationState.value != CurrentState.Success) {
                        NotificationUtils.requestPermission(intent.context)
                    }
                }
            }
            is SetupUIIntent.OnResume -> {
                mutex.withLock {
                    if (NotificationUtils.checkPermission(intent.context)) {
                        _notificationState.value = CurrentState.Success
                    }
                    val ct0 = intent.context.readApplicationUserCt0().first()
                    val auth = intent.context.readApplicationUserAuth().first()
                    if(ct0.isNotBlank() && auth.isNotBlank()){
                        _loginState.value = CurrentState.Success
                    }
                }
            }
            is SetupUIIntent.StartApplication -> {
                val context = intent.context
                context.startActivity(Intent(context, ActivityUtil.classMainActivity))
                context.getActivity().finish()
            }

        }
    }
}