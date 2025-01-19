package killua.dev.setup.ui

import android.content.Context
import dagger.hilt.android.lifecycle.HiltViewModel
import killua.dev.core.utils.NotificationUtils
import Model.CurrentState
import Model.SnackbarUIEffect
import killua.dev.setup.ui.SetupUIIntent.ValidateNotifications
import killua.dev.setup.ui.SetupUIIntent.ValidateStoragePermission
import killua.dev.setup.ui.SetupUIIntent.ValidatedRoot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ui.BaseViewModel
import ui.UIIntent
import ui.UIState
import javax.inject.Inject

data class SetupUIState(
    val rootError: String,
): UIState

sealed class SetupUIIntent : UIIntent{
    data object ValidatedRoot: SetupUIIntent()
    data class ValidateNotifications(val context: Context): SetupUIIntent()
    data class ValidateStoragePermission(val context: Context): SetupUIIntent()
    data class OnResume(val context: Context): SetupUIIntent()
}

@HiltViewModel
class SetupPageViewModel @Inject constructor() : BaseViewModel<SetupUIIntent, SetupUIState, SnackbarUIEffect>(SetupUIState("")) {
    private val mutex = Mutex()
    private val _rootState: MutableStateFlow<CurrentState> = MutableStateFlow(CurrentState.Idle)
    private val _notificationState: MutableStateFlow<CurrentState> = MutableStateFlow(CurrentState.Idle)
    private val _storagePermissionState: MutableStateFlow<CurrentState> = MutableStateFlow(CurrentState.Idle)
    //val rootState: StateFlow<EnvState> = _rootState.stateInScope(EnvState.Idle)
    val notificationState: StateFlow<CurrentState> = _notificationState.stateInScope(CurrentState.Idle)
    //val storagePermissionState: StateFlow<EnvState> = _storagePermissionState.stateInScope(EnvState.Idle)
//    val allOptionsValidated: StateFlow<Boolean> = combine(_notificationState,_storagePermissionState){ notification,storage ->
//         notification == EnvState.Success && storage == EnvState.Success
//    }.flowOnIO().stateInScope(false)
    override suspend fun onEvent(state: SetupUIState, intent: SetupUIIntent) {
        when(intent){
            is ValidateNotifications -> {
                mutex.withLock{
                    if(notificationState.value != CurrentState.Success){
                        NotificationUtils.requestPermission(intent.context)
                    }
                }
            }
            is ValidatedRoot -> {}
            is ValidateStoragePermission -> {
//                mutex.withLock{
//                    if(storagePermissionState.value != EnvState.Success){
//                        StorageUtils.requestPermission(context = intent.context)
//                    }
//                }
            }
            is SetupUIIntent.OnResume -> {
                mutex.withLock{
                    if(NotificationUtils.checkPermission(intent.context)){
                        _notificationState.value = CurrentState.Success
                    }
//                    if (StorageUtils.checkPermission(intent.context)){
//                        _storagePermissionState.value = EnvState.Success
//                    }
                }
            }
        }
    }
}