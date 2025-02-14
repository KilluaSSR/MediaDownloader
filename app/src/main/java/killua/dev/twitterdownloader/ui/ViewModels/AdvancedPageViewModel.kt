package killua.dev.twitterdownloader.ui.ViewModels

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import killua.dev.base.di.ApplicationScope
import killua.dev.base.states.CurrentState
import killua.dev.base.ui.BaseViewModel
import killua.dev.base.ui.SnackbarUIEffect
import killua.dev.base.ui.SnackbarUIEffect.ShowSnackbar
import killua.dev.base.ui.UIIntent
import killua.dev.base.ui.UIState
import killua.dev.twitterdownloader.Model.NetworkResult
import killua.dev.twitterdownloader.api.Kuaikan.Chapter
import killua.dev.twitterdownloader.api.Twitter.TwitterDownloadAPI
import killua.dev.twitterdownloader.features.AdvancedFeaturesManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

data class AdvancedPageUIState(
    val currentDialogType: DialogType = DialogType.NONE,
    val isEligibleToUseLofterGetByTags: Boolean = false,
    val isGettingMyTwitterBookmark: Boolean = false,
    val isFetching: Boolean = false,
    val info: Triple<String, String, String> = Triple("","",""),
    val downloadList: Set<String> = emptySet(),
    val chapters: List<Pair<Chapter, Boolean>> = emptyList(),
    val showChapterSelection: Boolean = false
): UIState

sealed class AdvancedPageUIIntent : UIIntent {
    data object OnEntry: AdvancedPageUIIntent()
    data object GetMyTwitterBookmark: AdvancedPageUIIntent()
    data object GetMyTwitterLiked : AdvancedPageUIIntent()
    data class GetSomeonesTwitterAccountInfo(val screenName: String): AdvancedPageUIIntent()
    data class OnConfirmTwitterDownloadMedia(val screenName: String, val id: String): AdvancedPageUIIntent()
    data class GetKuaikanEntireManga(val url: String): AdvancedPageUIIntent()
    data class GetLofterPicsByTags(val url: String): AdvancedPageUIIntent()
    data class ToggleChapter(val index: Int) : AdvancedPageUIIntent()
    data object ConfirmChapterSelection : AdvancedPageUIIntent()
    data object DismissChapterSelection : AdvancedPageUIIntent()
}
enum class DialogType {
    TWITTER_USER_INFO_DOWNLOAD,
    LOFTER_AUTHOR_TAGS,
    KUAIKAN_ENTIRE,
    NONE
}

@HiltViewModel
class AdvancedPageViewModel @Inject constructor(
    private val twitterDownloadAPI: TwitterDownloadAPI,
    private val advancedFeaturesManager: AdvancedFeaturesManager,
    @ApplicationScope private val applicationScope: CoroutineScope
): BaseViewModel<AdvancedPageUIIntent, AdvancedPageUIState, SnackbarUIEffect>(AdvancedPageUIState()) {
    private val mutex = Mutex()
    private val _lofterUsableState = MutableStateFlow<CurrentState>(CurrentState.Idle)

    val lofterGetByTagsEligibility: StateFlow<Boolean> = _lofterUsableState
        .map { it == CurrentState.Success }
        .flowOnIO()
        .stateInScope(false)
    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override suspend fun onEvent(state: AdvancedPageUIState, intent: AdvancedPageUIIntent) {
        when(intent) {
            is AdvancedPageUIIntent.OnEntry -> handleEntry()
            AdvancedPageUIIntent.GetMyTwitterBookmark -> handleTwitterBookmarks()
            AdvancedPageUIIntent.GetMyTwitterLiked -> handleTwitterLikes()
            is AdvancedPageUIIntent.GetSomeonesTwitterAccountInfo -> handleTwitterUserInfo(intent.screenName)
            is AdvancedPageUIIntent.OnConfirmTwitterDownloadMedia -> handleUserMediaDownload(intent.screenName, intent.id)
            is AdvancedPageUIIntent.GetKuaikanEntireManga -> handleKuaikanEntireManga(intent.url)
            is AdvancedPageUIIntent.GetLofterPicsByTags -> handleLofterPicsByAuthorTags(intent.url)
            is AdvancedPageUIIntent.ToggleChapter -> handleToggleChapter(intent.index)
            AdvancedPageUIIntent.ConfirmChapterSelection -> handleConfirmChapterSelection()
            AdvancedPageUIIntent.DismissChapterSelection -> handleDismissChapterSelection()
        }
    }
    
    private fun handleEntry() {
        launchOnIO {
            val tags = advancedFeaturesManager.readLofterTags()
            val tagsState = !tags.isNullOrEmpty()
            println("tags $tagsState")
            mutex.withLock {
                val (startDate, endDate) = advancedFeaturesManager.readStartDateAndEndDate()
                val (loginKey, loginAuth) = advancedFeaturesManager.readLofterCredits()
                println("startDate: $startDate, endDate: $endDate")
                println("loginKey: $loginKey, loginAuth: $loginAuth")

                val allConditionsMet = loginKey.isNotBlank() &&
                        loginAuth.isNotBlank() &&
                        startDate != 0L &&
                        endDate != 0L &&
                        tagsState
                _lofterUsableState.value = if (allConditionsMet) {
                    CurrentState.Success
                } else {
                    CurrentState.Idle
                }
            }
        }
    }

    private fun handleTwitterBookmarks() {
        applicationScope.launch {
            advancedFeaturesManager.handleTwitterBookmarks()
                .onFailure { showMessage(it.message ?: "Download failed") }
        }
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private fun handleLofterPicsByAuthorTags(url: String) {
        applicationScope.launch {
            advancedFeaturesManager.getLofterPicsByAuthorTags(url = url)
        }
    }

    private fun handleTwitterLikes() {
        applicationScope.launch {
            advancedFeaturesManager.handleTwitterLikes()
                .onFailure { showMessage(it.message ?: "Download failed") }
        }
    }


    private fun handleTwitterUserInfo(screenName: String) {
        applicationScope.launch {
            emitState(uiState.value.copy(isFetching = true))
            when (val result = twitterDownloadAPI.getUserBasicInfo(screenName)) {
                is NetworkResult.Success -> {
                    emitState(uiState.value.copy(
                        isFetching = false,
                        info = Triple(
                            result.data.id ?: "",
                            result.data.name ?: "",
                            result.data.screenName ?: ""
                        )
                    ))
                }
                is NetworkResult.Error -> {
                    emitState(uiState.value.copy(isFetching = false))
                    showMessage(result.message)
                }
            }
        }
    }


    private fun handleKuaikanEntireManga(url: String) {
        applicationScope.launch {
            emitState(uiState.value.copy(isFetching = true))
            when(val mangaList = advancedFeaturesManager.getWholeManga(url)){
                is NetworkResult.Error -> {
                    emitState(uiState.value.copy(isFetching = false))
                    showMessage("Error")
                }
                is NetworkResult.Success -> {
                    emitState(uiState.value.copy(
                        isFetching = false,
                        chapters = mangaList.data.map { it to true },
                        showChapterSelection = true
                    ))
                }
            }
        }
    }

    private fun handleUserMediaDownload(screenName: String, id: String) {
        if (id.isEmpty()) return
        applicationScope.launch {
            advancedFeaturesManager.getUserMediaByUserId(id, screenName)
                .onFailure { showMessage(it.message ?: "Download failed") }
        }
    }

    private fun handleToggleChapter(index: Int) {
        val updatedChapters = uiState.value.chapters.mapIndexed { i, pair ->
            if (i == index) pair.copy(second = !pair.second)
            else pair
        }
        viewModelScope.launch{
            emitState(uiState.value.copy(chapters = updatedChapters))
        }
    }

    private fun handleConfirmChapterSelection() {
        applicationScope.launch {
            val selectedChapters = uiState.value.chapters
                .filter { it.second }
                .map { it.first }
            advancedFeaturesManager.downloadEntireManga(selectedChapters)
            emitState(uiState.value.copy(
                showChapterSelection = false,
                chapters = emptyList()
            ))
        }
    }

    private fun handleDismissChapterSelection() {
        viewModelScope.launch{
            emitState(uiState.value.copy(
                showChapterSelection = false,
                chapters = emptyList()
            ))
        }
    }

    private fun showMessage(message: String) {
        viewModelScope.launch {
            emitEffect(ShowSnackbar(message))
        }
    }
}