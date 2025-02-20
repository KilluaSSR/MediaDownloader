package killua.dev.mediadownloader.ui.ViewModels

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import killua.dev.mediadownloader.Model.ChapterInfo
import killua.dev.mediadownloader.Model.NetworkResult
import killua.dev.mediadownloader.Model.toChapterInfo
import killua.dev.mediadownloader.api.Kuaikan.Chapter
import killua.dev.mediadownloader.api.Pixiv.Model.NovelInfo
import killua.dev.mediadownloader.api.Twitter.TwitterDownloadAPI
import killua.dev.mediadownloader.di.ApplicationScope
import killua.dev.mediadownloader.features.AdvancedFeaturesManager
import killua.dev.mediadownloader.states.CurrentState
import killua.dev.mediadownloader.ui.SnackbarUIEffect
import killua.dev.mediadownloader.ui.SnackbarUIEffect.ShowSnackbar
import killua.dev.mediadownloader.ui.UIIntent
import killua.dev.mediadownloader.ui.UIState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

enum class ChapterDownloadType {
    KUAIKAN_MANGA,
    PIXIV_NOVEL,
    NONE
}
data class AdvancedPageUIState(
    val currentDialogType: DialogType = DialogType.NONE,
    val isGettingMyTwitterBookmark: Boolean = false,
    val isFetching: Boolean = false,
    val showDialog: Boolean = false,
    val info: Triple<String, String, String> = Triple("","",""),
    val downloadList: Set<String> = emptySet(),
    val chapters: List<Pair<ChapterInfo, Boolean>> = emptyList(),
    val showChapterSelection: Boolean = false,
    val currentDownloadType: ChapterDownloadType = ChapterDownloadType.NONE
): UIState

sealed class AdvancedPageUIIntent : UIIntent {
    data object OnEntry: AdvancedPageUIIntent()
    data object GetMyTwitterBookmark: AdvancedPageUIIntent()
    data object GetMyTwitterLiked : AdvancedPageUIIntent()
    data class GetSomeonesTwitterAccountInfo(val screenName: String): AdvancedPageUIIntent()
    data class OnConfirmTwitterDownloadMedia(val screenName: String, val id: String): AdvancedPageUIIntent()
    data class GetKuaikanEntireManga(val url: String): AdvancedPageUIIntent()
    data class GetPixivEntireNovel(val url: String): AdvancedPageUIIntent()
    data class GetLofterPicsByTags(val url: String): AdvancedPageUIIntent()
    data class ToggleChapter(val index: Int) : AdvancedPageUIIntent()
    data object ConfirmChapterSelection : AdvancedPageUIIntent()
    data object DismissChapterSelection : AdvancedPageUIIntent()
    data object SelectAllChapters : AdvancedPageUIIntent()
    data object ClearAllChapters : AdvancedPageUIIntent()
}
enum class DialogType {
    TWITTER_USER_INFO_DOWNLOAD,
    LOFTER_AUTHOR_TAGS,
    KUAIKAN_ENTIRE,
    PIXIV_ENTIRE_NOVEL,
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
    private val _twitterUsableState = MutableStateFlow<CurrentState>(CurrentState.Idle)

    val lofterGetByTagsEligibility: StateFlow<Boolean> = _lofterUsableState
        .map { it == CurrentState.Success }
        .flowOnIO()
        .stateInScope(false)
    val twitterEligibility: StateFlow<Boolean> = _twitterUsableState
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
            AdvancedPageUIIntent.SelectAllChapters -> handleSelectAllChapters()
            AdvancedPageUIIntent.ClearAllChapters -> handleClearAllChapters()
            is AdvancedPageUIIntent.GetPixivEntireNovel -> handlePixivEntireNovel(intent.url)
        }
    }

    private fun handleEntry() {
        launchOnIO {
            val tags = advancedFeaturesManager.readLofterTags()
            val tagsState = !tags.isNullOrEmpty()
            mutex.withLock {
                val (startDate, endDate) = advancedFeaturesManager.readStartDateAndEndDate()
                val isTwitterLoggedIn = advancedFeaturesManager.isTwitterLoggedIn().isSuccess
                val isLofterLoggedIn = advancedFeaturesManager.isLofterLoggedIn().isSuccess
                val allLofterConditionsMet = isLofterLoggedIn &&
                        startDate != 0L &&
                        endDate != 0L &&
                        tagsState
                _lofterUsableState.value = if (allLofterConditionsMet) {
                    CurrentState.Success
                } else {
                    CurrentState.Idle
                }
                _twitterUsableState.value = if (isTwitterLoggedIn) {
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
            when(val mangaList = advancedFeaturesManager.getKuaikanEntireComic(url)){
                is NetworkResult.Error -> {
                    emitState(uiState.value.copy(isFetching = false))
                    showMessage("Error")
                }
                is NetworkResult.Success -> {
                    emitState(uiState.value.copy(
                        isFetching = false,
                        showDialog = false,
                        chapters = mangaList.data.map { it.toChapterInfo() }.map { it to true },
                        showChapterSelection = true,
                        currentDownloadType = ChapterDownloadType.KUAIKAN_MANGA  // 设置下载类型
                    ))
                }
            }
        }
    }

    private fun handlePixivEntireNovel(url: String) {
        applicationScope.launch {
            emitState(uiState.value.copy(isFetching = true))
            when(val novelList = advancedFeaturesManager.getPixivEntireNovel(url)){
                is NetworkResult.Error -> {
                    emitState(uiState.value.copy(isFetching = false))
                    showMessage("Error")
                }
                is NetworkResult.Success -> {
                    emitState(uiState.value.copy(
                        isFetching = false,
                        showDialog = false,
                        chapters = novelList.data.map { it.toChapterInfo() }.map { it to true },
                        showChapterSelection = true,
                        currentDownloadType = ChapterDownloadType.PIXIV_NOVEL  // 设置下载类型
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
    private fun handleSelectAllChapters() {
        val updatedChapters = uiState.value.chapters.map { it.copy(second = true) }
        viewModelScope.launch{
            emitState(uiState.value.copy(chapters = updatedChapters))
        }
    }

    private fun handleClearAllChapters() {
        val updatedChapters = uiState.value.chapters.map { it.copy(second = false) }
        viewModelScope.launch{
            emitState(uiState.value.copy(chapters = updatedChapters))
        }
    }
    private fun handleConfirmChapterSelection() {
        applicationScope.launch {
            val selectedChapters = uiState.value.chapters
                .filter { it.second }
                .map { it.first }

            when (uiState.value.currentDownloadType) {
                ChapterDownloadType.KUAIKAN_MANGA -> {
                    // 将 ChapterInfo 转换回 Kuaikan.Chapter
                    val kuaikanChapters = selectedChapters.mapNotNull { chapterInfo ->
                        when (chapterInfo) {
                            is ChapterInfo.DownloadableChapter -> Chapter(
                                id = chapterInfo.id,
                                name = chapterInfo.title
                            )
                            else -> null
                        }
                    }
                    advancedFeaturesManager.downloadEntireKuaikanComic(kuaikanChapters)
                    advancedFeaturesManager.cancelKuaikanProgressNotification()
                }
                ChapterDownloadType.PIXIV_NOVEL -> {
                    val pixivChapters = selectedChapters.mapNotNull { chapterInfo ->
                        when (chapterInfo) {
                            is ChapterInfo.DownloadableChapter -> NovelInfo(
                                seriesTitle = chapterInfo.seriesName!!,
                                id = chapterInfo.id,
                                title = chapterInfo.title
                            )
                            else -> null
                        }
                    }
                    advancedFeaturesManager.downloadEntirePixivNovel(pixivChapters)
                    advancedFeaturesManager.cancelPixivProgressNotification()
                }
                ChapterDownloadType.NONE -> {
                    showMessage("Invalid download type")
                    return@launch
                }
            }

            emitState(uiState.value.copy(
                showChapterSelection = false,
                chapters = emptyList(),
                currentDownloadType = ChapterDownloadType.NONE
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