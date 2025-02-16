package killua.dev.mediadownloader.Setup.ui

import android.content.Intent
import dagger.hilt.android.lifecycle.HiltViewModel
import killua.dev.mediadownloader.datastore.readApplicationUserAuth
import killua.dev.mediadownloader.datastore.readApplicationUserCt0
import killua.dev.mediadownloader.states.CurrentState
import killua.dev.mediadownloader.ui.ViewModels.BaseViewModel
import killua.dev.mediadownloader.ui.SnackbarUIEffect
import killua.dev.mediadownloader.utils.ActivityUtil
import killua.dev.mediadownloader.utils.NotificationUtils
import killua.dev.mediadownloader.utils.getActivity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
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
            is SetupUIIntent.ValidateNotifications -> {
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