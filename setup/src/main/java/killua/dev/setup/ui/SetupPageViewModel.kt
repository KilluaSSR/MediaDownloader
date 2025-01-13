package killua.dev.setup.ui

import android.content.Context
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import killua.dev.core.utils.NotificationUtils
import killua.dev.core.utils.PermissionUtils
import killua.dev.core.utils.StorageUtils
import killua.dev.setup.EnvState
import killua.dev.setup.ui.SetupUIIntent.ValidateNotifications
import killua.dev.setup.ui.SetupUIIntent.ValidatedRoot
import killua.dev.setup.ui.SetupUIIntent.ValidateStoragePermission
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ui.BaseViewModel
import ui.UIEvent
import ui.UIIntent
import ui.UIState
import javax.inject.Inject


sealed class SetupUIIntent : UIIntent{
    data object ValidatedRoot: SetupUIIntent()
    data class ValidateNotifications(val context: Context): SetupUIIntent()
    data class ValidateStoragePermission(val context: Context): SetupUIIntent()
    data object onResume: SetupUIIntent()
}
@HiltViewModel
class SetupPageViewModel @Inject constructor(
    @ApplicationContext private val context: Context
): BaseViewModel<SetupUIIntent, UIState, UIEvent>() {
    private val mutex = Mutex()
    private val _rootState: MutableStateFlow<EnvState> = MutableStateFlow(EnvState.Idle)
    private val _notificationState: MutableStateFlow<EnvState> = MutableStateFlow(EnvState.Idle)
    private val _storagePermissionState: MutableStateFlow<EnvState> = MutableStateFlow(EnvState.Idle)
    val rootState: StateFlow<EnvState> = _rootState.asStateFlow()
    val notificationState: StateFlow<EnvState> = _notificationState.asStateFlow()
    val storagePermissionState: StateFlow<EnvState> = _storagePermissionState.asStateFlow()

    val allOptionsValidated: StateFlow<Boolean> = combine(_rootState,_notificationState,_storagePermissionState){ root,notification,storage ->
        root == EnvState.Success && notification == EnvState.Success && storage == EnvState.Success
    }.flowOnIO().stateInScope(false)

    override suspend fun onEvent(state: UIState, intent: SetupUIIntent) {
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

            SetupUIIntent.onResume -> {
                mutex.withLock{
                    if(NotificationUtils.checkPermission(context) != 0){
                        _notificationState.value = EnvState.Success
                    }
                    if (StorageUtils.checkPermission(context) != 0){
                        _storagePermissionState.value = EnvState.Success
                    }
                }
            }
        }
    }
}