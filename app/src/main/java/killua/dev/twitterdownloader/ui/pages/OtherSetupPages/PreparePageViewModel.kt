package killua.dev.twitterdownloader.ui.pages.OtherSetupPages

import android.content.Context
import dagger.hilt.android.lifecycle.HiltViewModel
import killua.dev.base.datastore.readKuaikanPassToken
import killua.dev.base.datastore.readLofterEndTime
import killua.dev.base.datastore.readLofterLoginAuth
import killua.dev.base.datastore.readLofterLoginKey
import killua.dev.base.datastore.readLofterStartTime
import killua.dev.base.datastore.readPixivPHPSSID
import killua.dev.base.states.CurrentState
import killua.dev.base.ui.BaseViewModel
import killua.dev.base.ui.SnackbarUIEffect
import killua.dev.base.ui.UIIntent
import killua.dev.base.ui.UIState
import killua.dev.twitterdownloader.db.LofterTagsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

data class PreparePageUIState(
    val isLoggedIn: Boolean = false,
    val isTagsAdded: Boolean = false,
): UIState

sealed class PreparePageUIIntent : UIIntent {
    data object OnDateChanged : PreparePageUIIntent()
    data class OnEntryLofter(val context: Context) : PreparePageUIIntent()
    data class OnEntryPixiv(val context: Context) : PreparePageUIIntent()
    data class OnEntryKuaikan(val context: Context) : PreparePageUIIntent()
    data class OnTagsChanged(val context: Context) : PreparePageUIIntent()
    data object OnLofterLoggedOut: PreparePageUIIntent()
    data object OnPixivLoggedOut: PreparePageUIIntent()
    data object OnKuaikanLoggedOut: PreparePageUIIntent()
}

@HiltViewModel
class PreparePageViewModel @Inject constructor(
    private val tagsRepository: LofterTagsRepository
): BaseViewModel<PreparePageUIIntent, PreparePageUIState,SnackbarUIEffect>(
    PreparePageUIState()
){
    private val mutex = Mutex()
    private val _lofterLoginState: MutableStateFlow<CurrentState> =
        MutableStateFlow(CurrentState.Idle)
    val lofterLoginState: StateFlow<CurrentState> =
        _lofterLoginState.stateInScope(CurrentState.Idle)

    private val _pixivLoginState: MutableStateFlow<CurrentState> =
        MutableStateFlow(CurrentState.Idle)
    val pixivLoginState: StateFlow<CurrentState> =
        _pixivLoginState.stateInScope(CurrentState.Idle)

    private val _kuaikanLoginState: MutableStateFlow<CurrentState> =
        MutableStateFlow(CurrentState.Idle)
    val kuaikanLoginState: StateFlow<CurrentState> =
        _kuaikanLoginState.stateInScope(CurrentState.Idle)

    private val _dateSelectedState: MutableStateFlow<CurrentState> =
        MutableStateFlow(CurrentState.Idle)
    val dateSelectedState: StateFlow<CurrentState>  = _dateSelectedState.stateInScope(CurrentState.Idle)

    private val _tagsAddedState: MutableStateFlow<CurrentState> =
        MutableStateFlow(CurrentState.Idle)
    val tagsAddedState: StateFlow<CurrentState>  = _tagsAddedState.stateInScope(CurrentState.Idle)

    val lofterEligibility: StateFlow<Boolean> = _lofterLoginState.map { login ->
        login == CurrentState.Success}.flowOnIO().stateInScope(false)
    val pixivEligibility: StateFlow<Boolean> = _pixivLoginState.map { login ->
        login == CurrentState.Success}.flowOnIO().stateInScope(false)
    val kuaikanEligibility: StateFlow<Boolean> = _kuaikanLoginState.map { login ->
        login == CurrentState.Success}.flowOnIO().stateInScope(false)

    override suspend fun onEvent(
        state: PreparePageUIState,
        intent: PreparePageUIIntent
    ) {
        when(intent){
            is PreparePageUIIntent.OnDateChanged -> {
                _dateSelectedState.value = CurrentState.Success
            }

            is PreparePageUIIntent.OnEntryLofter -> {
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
                        _lofterLoginState.value = CurrentState.Success
                    }
                    if(startDate != 0L && endDate != 0L){
                        _dateSelectedState.value = CurrentState.Success
                    }
                }
            }

            is PreparePageUIIntent.OnEntryPixiv -> {
                mutex.withLock{
                    val PHPSSID = intent.context.readPixivPHPSSID().first()
                    if (PHPSSID.isNotBlank()){
                        _pixivLoginState.value = CurrentState.Success
                    }
                }
            }

            is PreparePageUIIntent.OnTagsChanged -> {}
            PreparePageUIIntent.OnLofterLoggedOut -> {
                _lofterLoginState.value = CurrentState.Idle
            }


            PreparePageUIIntent.OnPixivLoggedOut, -> {
                _pixivLoginState.value = CurrentState.Idle
            }

            is PreparePageUIIntent.OnEntryKuaikan -> {
                mutex.withLock{
                    val PassToken = intent.context.readKuaikanPassToken().first()
                    if (PassToken.isNotBlank()){
                        _kuaikanLoginState.value = CurrentState.Success
                    }
                }
            }
            PreparePageUIIntent.OnKuaikanLoggedOut -> {
                _kuaikanLoginState.value = CurrentState.Idle
            }
        }
    }
}