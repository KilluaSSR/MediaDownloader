package killua.dev.mediadownloader.ui.pages.OtherSetupPages

import dagger.hilt.android.lifecycle.HiltViewModel
import killua.dev.mediadownloader.db.LofterTagsRepository
import killua.dev.mediadownloader.di.UserDataManager
import killua.dev.mediadownloader.states.CurrentState
import killua.dev.mediadownloader.ui.SnackbarUIEffect
import killua.dev.mediadownloader.ui.UIIntent
import killua.dev.mediadownloader.ui.UIState
import killua.dev.mediadownloader.ui.ViewModels.BaseViewModel
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
    data object OnEntryLofter : PreparePageUIIntent()
    data object OnEntryPixiv : PreparePageUIIntent()
    data object OnEntryKuaikan : PreparePageUIIntent()
    data object OnEntryTwitter : PreparePageUIIntent()
    data object OnTagsChanged : PreparePageUIIntent()
    data object OnLofterLoggedOut: PreparePageUIIntent()
    data object OnPixivLoggedOut: PreparePageUIIntent()
    data object OnKuaikanLoggedOut: PreparePageUIIntent()
    data object OnTwitterLoggedOut: PreparePageUIIntent()
}

@HiltViewModel
class PreparePageViewModel @Inject constructor(
    private val tagsRepository: LofterTagsRepository,
    private val userDataManager: UserDataManager
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

    private val _twitterLoginState: MutableStateFlow<CurrentState> =
        MutableStateFlow(CurrentState.Idle)
    val twitterLoginState: StateFlow<CurrentState> =
        _twitterLoginState.stateInScope(CurrentState.Idle)

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
    val twitterEligibility: StateFlow<Boolean> = _twitterLoginState.map { login ->
        login == CurrentState.Success}.flowOnIO().stateInScope(false)
    override suspend fun onEvent(
        state: PreparePageUIState,
        intent: PreparePageUIIntent
    ) {
        when(intent) {
            is PreparePageUIIntent.OnDateChanged -> {
                _dateSelectedState.value = CurrentState.Success
            }

            is PreparePageUIIntent.OnEntryLofter -> {
                launchOnIO {
                    val tags = tagsRepository.observeAllDownloads().first()?.tags
                    if (!tags.isNullOrEmpty()) {
                        _tagsAddedState.value = CurrentState.Success
                    }
                }
                mutex.withLock {
                    val (loginKey, loginAuth, startDate, endDate) = userDataManager.userLofterData.value
                    if (loginKey.isNotBlank() && loginAuth.isNotBlank()) {
                        _lofterLoginState.value = CurrentState.Success
                    }
                    if (startDate != 0L && endDate != 0L) {
                        _dateSelectedState.value = CurrentState.Success
                    }
                }
            }

            is PreparePageUIIntent.OnEntryPixiv -> {
                mutex.withLock {
                    val PHPSSID = userDataManager.userPixivPHPSSID.value
                    if (PHPSSID.isNotBlank()) {
                        _pixivLoginState.value = CurrentState.Success
                    }
                }
            }

            is PreparePageUIIntent.OnTagsChanged -> {}
            PreparePageUIIntent.OnLofterLoggedOut -> {
                _lofterLoginState.value = CurrentState.Idle
            }


            PreparePageUIIntent.OnPixivLoggedOut -> {
                _pixivLoginState.value = CurrentState.Idle
            }

            is PreparePageUIIntent.OnEntryKuaikan -> {
                mutex.withLock{
                    val PassToken = userDataManager.userKuaikanData.value
                    if (PassToken.isNotBlank()){
                        _kuaikanLoginState.value = CurrentState.Success
                    }
                }
            }
            PreparePageUIIntent.OnKuaikanLoggedOut -> {
                _kuaikanLoginState.value = CurrentState.Idle
            }

            is PreparePageUIIntent.OnEntryTwitter -> {
                mutex.withLock{
                    val (ct0,auth) = userDataManager.userTwitterData.value
                    if (ct0.isNotBlank() && auth.isNotBlank()){
                        _twitterLoginState.value = CurrentState.Success
                    }
                }
            }
            PreparePageUIIntent.OnTwitterLoggedOut -> {
                _twitterLoginState.value = CurrentState.Idle
            }
        }
    }
}