package killua.dev.twitterdownloader.ui.pages.OtherSetupPages.Lofter

import android.content.Context
import android.webkit.CookieManager
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import killua.dev.base.datastore.readApplicationUserAuth
import killua.dev.base.datastore.readApplicationUserCt0
import killua.dev.base.datastore.readLofterEndTime
import killua.dev.base.datastore.readLofterLoginAuth
import killua.dev.base.datastore.readLofterLoginKey
import killua.dev.base.datastore.readLofterStartTime
import killua.dev.base.datastore.writeApplicationUserAuth
import killua.dev.base.datastore.writeApplicationUserCt0
import killua.dev.base.states.CurrentState
import killua.dev.base.ui.BaseViewModel
import killua.dev.base.ui.SnackbarUIEffect
import killua.dev.base.ui.UIIntent
import killua.dev.base.ui.UIState
import killua.dev.base.utils.NotificationUtils
import killua.dev.twitterdownloader.db.LofterTagsRepository
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

data class LofterPreparePageUIState(
    val isLoggedIn: Boolean = false,
    val isTagsAdded: Boolean = false
): UIState

sealed class LofterPreparePageUIIntent : UIIntent {
    data object OnDateChanged : LofterPreparePageUIIntent()
    data class OnEntry(val context: Context) : LofterPreparePageUIIntent()
    data class OnTagsChanged(val context: Context) : LofterPreparePageUIIntent()
    data object OnLoggedOut: LofterPreparePageUIIntent()
}

@HiltViewModel
class LofterPreparePageViewModel @Inject constructor(
    private val tagsRepository: LofterTagsRepository
): BaseViewModel<LofterPreparePageUIIntent, LofterPreparePageUIState,SnackbarUIEffect>(
    LofterPreparePageUIState()
){
    private val mutex = Mutex()
    private val _loginState: MutableStateFlow<CurrentState> =
        MutableStateFlow(CurrentState.Idle)
    val loginState: StateFlow<CurrentState> =
        _loginState.stateInScope(CurrentState.Idle)

    private val _dateSelectedState: MutableStateFlow<CurrentState> =
        MutableStateFlow(CurrentState.Idle)
    val dateSelectedState: StateFlow<CurrentState>  = _dateSelectedState.stateInScope(CurrentState.Idle)

    private val _tagsAddedState: MutableStateFlow<CurrentState> =
        MutableStateFlow(CurrentState.Idle)
    val tagsAddedState: StateFlow<CurrentState>  = _tagsAddedState.stateInScope(CurrentState.Idle)

    val eligibility: StateFlow<Boolean> = _loginState.map { login ->
        login == CurrentState.Success}.flowOnIO().stateInScope(false)

    override suspend fun onEvent(
        state: LofterPreparePageUIState,
        intent: LofterPreparePageUIIntent
    ) {
        when(intent){
            is LofterPreparePageUIIntent.OnDateChanged -> {
                _dateSelectedState.value = CurrentState.Success
            }
            is LofterPreparePageUIIntent.OnEntry -> {
                launchOnIO {
                    val tags = tagsRepository.observeAllDownloads().first()?.tags
                    if(!tags.isNullOrEmpty()){
                        _tagsAddedState.value = CurrentState.Success
                    }
                }
                mutex.withLock {
                    val loginKey = intent.context.readLofterLoginKey().first()
                    val loginAuth = intent.context.readLofterLoginAuth().first()
                    val startDate = intent.context.readLofterStartTime().first()
                    val endDate = intent.context.readLofterEndTime().first()
                    if(loginKey.isNotBlank() && loginAuth.isNotBlank()){
                        _loginState.value = CurrentState.Success
                    }
                    if(startDate != 0L && endDate != 0L){
                        _dateSelectedState.value = CurrentState.Success
                    }
                }
            }
            is LofterPreparePageUIIntent.OnTagsChanged -> TODO()
            LofterPreparePageUIIntent.OnLoggedOut -> {
                _loginState.value = CurrentState.Idle
            }
        }
    }
}