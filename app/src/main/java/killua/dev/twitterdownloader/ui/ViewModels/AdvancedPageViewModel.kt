package killua.dev.twitterdownloader.ui.ViewModels

import android.content.Context
import dagger.hilt.android.lifecycle.HiltViewModel
import killua.dev.base.datastore.readLofterLoginAuth
import killua.dev.base.datastore.readLofterLoginKey
import killua.dev.base.di.ApplicationScope
import killua.dev.base.states.CurrentState
import killua.dev.base.ui.BaseViewModel
import killua.dev.base.ui.SnackbarUIEffect
import killua.dev.base.ui.SnackbarUIEffect.ShowSnackbar
import killua.dev.base.ui.UIIntent
import killua.dev.base.ui.UIState
import killua.dev.twitterdownloader.Model.NetworkResult
import killua.dev.twitterdownloader.api.Twitter.TwitterDownloadAPI
import killua.dev.twitterdownloader.features.AdvancedFeaturesManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

data class AdvancedPageUIState(
    val isLofterLoggedIn: Boolean = false,
    val isEligibleToUseLofterGetByTags: Boolean = false,
    val isGettingMyTwitterBookmark: Boolean = false,
    val isFetchingTwitterUserInfo: Boolean = false,
    val TwitterUserAccountInfo: Triple<String, String, String> = Triple("","","")
): UIState

sealed class AdvancedPageUIIntent : UIIntent {
    data class OnEntry(val context: Context): AdvancedPageUIIntent()
    data object GetMyTwitterBookmark: AdvancedPageUIIntent()
    data object GetMyTwitterLiked : AdvancedPageUIIntent()
    data class GetSomeonesTwitterAccountInfo(val screenName: String): AdvancedPageUIIntent()
    data class OnConfirmTwitterDownloadMedia(val screenName: String, val id: String): AdvancedPageUIIntent()
}
@HiltViewModel
class AdvancedPageViewModel @Inject constructor(
    private val twitterDownloadAPI: TwitterDownloadAPI,
    private val advancedFeaturesManager: AdvancedFeaturesManager,
    @ApplicationScope private val applicationScope: CoroutineScope
): BaseViewModel<AdvancedPageUIIntent, AdvancedPageUIState, SnackbarUIEffect>(AdvancedPageUIState()) {
    private val mutex = Mutex()
    private val _lofterLoginState = MutableStateFlow<CurrentState>(CurrentState.Idle)
    val lofterLoginState: StateFlow<CurrentState> = _lofterLoginState.asStateFlow()

    val lofterGetByTagsEligibility: StateFlow<Boolean> = _lofterLoginState
        .map { it == CurrentState.Success }
        .flowOnIO()
        .stateInScope(false)

    override suspend fun onEvent(state: AdvancedPageUIState, intent: AdvancedPageUIIntent) {
        when(intent) {
            is AdvancedPageUIIntent.OnEntry -> handleEntry(intent.context)
            AdvancedPageUIIntent.GetMyTwitterBookmark -> handleTwitterBookmarks()
            AdvancedPageUIIntent.GetMyTwitterLiked -> handleTwitterLikes()
            is AdvancedPageUIIntent.GetSomeonesTwitterAccountInfo ->
                handleTwitterUserInfo(intent.screenName)
            is AdvancedPageUIIntent.OnConfirmTwitterDownloadMedia ->
                handleUserMediaDownload(intent.screenName, intent.id)
        }
    }
    private suspend fun handleEntry(context: Context) {
        mutex.withLock {
            val loginKey = context.readLofterLoginKey().first()
            val loginAuth = context.readLofterLoginAuth().first()
            if(loginKey.isNotBlank() && loginAuth.isNotBlank()) {
                _lofterLoginState.value = CurrentState.Success
            }
        }
    }

    private fun handleTwitterBookmarks() {
        applicationScope.launch {
            advancedFeaturesManager.handleTwitterBookmarks()
                .onFailure { handleError(it.message ?: "Download failed") }
        }
    }

    private fun handleTwitterLikes() {
        applicationScope.launch {
            advancedFeaturesManager.handleTwitterLikes()
                .onFailure { handleError(it.message ?: "Download failed") }
        }
    }
    private fun handleTwitterUserInfo(screenName: String) {
        applicationScope.launch {
            emitState(uiState.value.copy(isFetchingTwitterUserInfo = true))
            when (val result = twitterDownloadAPI.getUserBasicInfo(screenName)) {
                is NetworkResult.Success -> {
                    emitState(uiState.value.copy(
                        isFetchingTwitterUserInfo = false,
                        TwitterUserAccountInfo = Triple(
                            result.data.id ?: "",
                            result.data.name ?: "",
                            result.data.screenName ?: ""
                        )
                    ))
                }
                is NetworkResult.Error -> {
                    emitState(uiState.value.copy(isFetchingTwitterUserInfo = false))
                    handleError(result.message)
                }
            }
        }
    }

    private fun handleUserMediaDownload(screenName: String, id: String) {
        if (id.isEmpty()) return
        applicationScope.launch {
            advancedFeaturesManager.getUserMediaByUserId(id, screenName)
                .onFailure { handleError(it.message ?: "Download failed") }
        }
    }

    private fun handleError(error: String) {
        applicationScope.launch {
            emitEffect(ShowSnackbar(error, "OKAY", true))
        }
    }
}