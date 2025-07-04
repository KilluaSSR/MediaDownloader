package killua.dev.mediadownloader.ui.ViewModels

import androidx.compose.material3.SnackbarDuration
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import killua.dev.mediadownloader.Model.ChapterInfo
import killua.dev.mediadownloader.Model.NetworkResult
import killua.dev.mediadownloader.Model.toChapterInfo
import killua.dev.mediadownloader.api.KuaikanChapter
import killua.dev.mediadownloader.api.MissEvan.Model.MissEvanDownloadDrama
import killua.dev.mediadownloader.api.Pixiv.Model.NovelInfo
import killua.dev.mediadownloader.api.PlatformService
import killua.dev.mediadownloader.di.ApplicationScope
import killua.dev.mediadownloader.features.AdvancedFeaturesManager
import killua.dev.mediadownloader.states.CurrentState
import killua.dev.mediadownloader.ui.SnackbarUIEffect
import killua.dev.mediadownloader.ui.SnackbarUIEffect.ShowSnackbar
import killua.dev.mediadownloader.ui.UIIntent
import killua.dev.mediadownloader.ui.UIState
import killua.dev.mediadownloader.utils.DownloadPreChecks
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
    MISSEVAN_DRAMA,
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
    data class GetMissEvanEntireDrama(val url: String): AdvancedPageUIIntent()
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
    MissEvan,
    NONE
}

@HiltViewModel
class AdvancedPageViewModel @Inject constructor(
    private val platformService: PlatformService,
    private val advancedFeaturesManager: AdvancedFeaturesManager,
    private val downloadPreChecks: DownloadPreChecks,
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
            is AdvancedPageUIIntent.GetMissEvanEntireDrama -> handleMissEvanEntireDrama(intent.url)
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

    private suspend fun handleTwitterBookmarks() {
        downloadPreChecks.canStartDownload().onSuccess {
            applicationScope.launch {
                advancedFeaturesManager.handleTwitterBookmarks()
                    .onFailure { emitEffect(ShowSnackbar(it.message ?: "Error", "OK", true, SnackbarDuration.Short)) }
            }
        }.onFailure {
            emitEffect(ShowSnackbar(it.message ?: "Error", "OK", true, SnackbarDuration.Short))
        }

    }

    private suspend fun handleLofterPicsByAuthorTags(url: String) {
        downloadPreChecks.canStartDownload().onSuccess {
            applicationScope.launch {
                advancedFeaturesManager.getLofterPicsByAuthorTags(url = url)
            }
        }.onFailure {
            emitEffect(ShowSnackbar(it.message ?: "Error", "OK", true, SnackbarDuration.Short))
        }
    }

    private suspend fun handleTwitterLikes() {
        downloadPreChecks.canStartDownload().onSuccess {
            applicationScope.launch {
                advancedFeaturesManager.handleTwitterLikes()
                    .onFailure { emitEffect(ShowSnackbar(it.message ?: "Error", "OK", true, SnackbarDuration.Short)) }
            }
        }.onFailure {
            emitEffect(ShowSnackbar(it.message ?: "Error", "OK", true, SnackbarDuration.Short))
        }
    }

    private suspend fun handleTwitterUserInfo(screenName: String) {
        downloadPreChecks.canStartDownload().onSuccess {
            applicationScope.launch {
                emitState(uiState.value.copy(isFetching = true))
                when (val result = platformService.getTwitterUserBasicInfo(screenName)) {
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
                        emitEffect(ShowSnackbar(result.message, "OK", true, SnackbarDuration.Short))
                    }
                }
            }
        }.onFailure {
            emitEffect(ShowSnackbar(it.message ?: "Error", "OK", true, SnackbarDuration.Short))
        }
    }


    private suspend fun handleKuaikanEntireManga(url: String) {
        downloadPreChecks.canStartDownload().onSuccess {
            applicationScope.launch {
                emitState(uiState.value.copy(isFetching = true))
                when(val mangaList = advancedFeaturesManager.getKuaikanEntireComic(url)){
                    is NetworkResult.Error -> {
                        emitState(uiState.value.copy(isFetching = false))
                        emitEffect(ShowSnackbar(mangaList.message, "OK", true, SnackbarDuration.Short))
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
        }.onFailure {
            emitEffect(ShowSnackbar(it.message ?: "Error", "OK", true, SnackbarDuration.Short))
        }
    }

    private suspend fun handlePixivEntireNovel(url: String) {
        downloadPreChecks.canStartDownload().onSuccess {
            applicationScope.launch {
                emitState(uiState.value.copy(isFetching = true))
                when(val novelList = advancedFeaturesManager.getPixivEntireNovel(url)){
                    is NetworkResult.Error -> {
                        emitState(uiState.value.copy(isFetching = false))
                        emitEffect(ShowSnackbar(novelList.message, "OK", true, SnackbarDuration.Short))
                    }
                    is NetworkResult.Success -> {
                        emitState(uiState.value.copy(
                            isFetching = false,
                            showDialog = false,
                            chapters = novelList.data.map { it.toChapterInfo() }.map { it to true },
                            showChapterSelection = true,
                            currentDownloadType = ChapterDownloadType.PIXIV_NOVEL
                        ))
                    }
                }
            }
        }.onFailure {
            emitEffect(ShowSnackbar(it.message ?: "Error", "OK", true, SnackbarDuration.Short))
        }
    }

    private suspend fun handleMissEvanEntireDrama(url: String){
        downloadPreChecks.canStartDownload().onSuccess {
            applicationScope.launch {
                emitState(uiState.value.copy(isFetching = true))
                when(val dramaList = advancedFeaturesManager.getMissEvanEntireDrama(url)){
                    is NetworkResult.Error -> {
                        emitState(uiState.value.copy(isFetching = false))

                    }
                    is NetworkResult.Success -> {
                        emitState(uiState.value.copy(
                            isFetching = false,
                            showDialog = false,
                            chapters = dramaList.data.dramaList.map { it.toChapterInfo(dramaList.data.title) }.map { it to true },
                            showChapterSelection = true,
                            currentDownloadType = ChapterDownloadType.MISSEVAN_DRAMA
                        ))
                    }
                }
            }
        }.onFailure {
            emitEffect(ShowSnackbar(it.message ?: "Error", "OK", true, SnackbarDuration.Short))
        }
    }

    private fun handleUserMediaDownload(screenName: String, id: String) {
        if (id.isEmpty()) return
        applicationScope.launch {
            advancedFeaturesManager.getUserMediaByUserId(id, screenName)
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
                    val kuaikanKuaikanChapters = selectedChapters.mapNotNull { chapterInfo ->
                        when (chapterInfo) {
                            is ChapterInfo.DownloadableChapter -> KuaikanChapter(
                                id = chapterInfo.id,
                                name = chapterInfo.title
                            )
                            else -> null
                        }
                    }
                    advancedFeaturesManager.downloadEntireKuaikanComic(kuaikanKuaikanChapters)
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
                    return@launch
                }

                ChapterDownloadType.MISSEVAN_DRAMA -> {
                    val missEvanChapters = selectedChapters.mapNotNull { chapterInfo ->
                        when (chapterInfo) {
                            is ChapterInfo.DownloadableChapter -> MissEvanDownloadDrama(
                                id = chapterInfo.id,
                                title = chapterInfo.title,
                                mainTitle = chapterInfo.seriesName!!
                            )
                            else -> null
                        }
                    }
                    advancedFeaturesManager.downloadEntireMissEvanDrama(missEvanChapters)
                    advancedFeaturesManager.cancelMissEvanProgressNotification()
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
}