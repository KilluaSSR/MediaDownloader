package killua.dev.setup.ui

import android.content.Context
import dagger.hilt.android.lifecycle.HiltViewModel
import killua.dev.core.utils.NotificationUtils
import killua.dev.core.utils.StorageUtils
import Model.EnvState
import killua.dev.setup.ui.SetupUIIntent.ValidateNotifications
import killua.dev.setup.ui.SetupUIIntent.ValidateStoragePermission
import killua.dev.setup.ui.SetupUIIntent.ValidatedRoot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ui.BaseViewModel
import ui.SetupPageUIEffect
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
    data class onResume(val context: Context): SetupUIIntent()
}

@HiltViewModel
class SetupPageViewModel @Inject constructor() : BaseViewModel<SetupUIIntent, SetupUIState, SetupPageUIEffect>(SetupUIState("")) {
    private val mutex = Mutex()
    private val _rootState: MutableStateFlow<EnvState> = MutableStateFlow(EnvState.Idle)
    private val _notificationState: MutableStateFlow<EnvState> = MutableStateFlow(EnvState.Idle)
    private val _storagePermissionState: MutableStateFlow<EnvState> = MutableStateFlow(EnvState.Idle)
    val rootState: StateFlow<EnvState> = _rootState.stateInScope(EnvState.Idle)
    val notificationState: StateFlow<EnvState> = _notificationState.stateInScope(EnvState.Idle)
    val storagePermissionState: StateFlow<EnvState> = _storagePermissionState.stateInScope(EnvState.Idle)

    val allOptionsValidated: StateFlow<Boolean> = combine(_rootState,_notificationState,_storagePermissionState){ root,notification,storage ->
        root == EnvState.Success && notification == EnvState.Success && storage == EnvState.Success
    }.flowOnIO().stateInScope(false)

    override suspend fun onEvent(state: SetupUIState, intent: SetupUIIntent) {
        when(intent){
            is ValidateNotifications -> {
                mutex.withLock{
                    if(notificationState.value != EnvState.Success){
                        NotificationUtils.checkPermission(intent.context)
                    }
                }
            }
            is ValidatedRoot -> TODO()
            is ValidateStoragePermission -> {
                mutex.withLock{
                    if(storagePermissionState.value != EnvState.Success){
                        // Request storage permission
                    }
                }
            }

            is SetupUIIntent.onResume -> {
                mutex.withLock{
                    if(NotificationUtils.checkPermission(intent.context) != 0){
                        _notificationState.value = EnvState.Success
                    }
                    if (StorageUtils.checkPermission(intent.context) != 0){
                        _storagePermissionState.value = EnvState.Success
                    }
                }
            }
        }
    }
}