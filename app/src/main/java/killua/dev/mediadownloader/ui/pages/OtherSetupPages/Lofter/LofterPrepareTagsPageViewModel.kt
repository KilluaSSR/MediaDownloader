package killua.dev.mediadownloader.ui.pages.OtherSetupPages.Lofter

import dagger.hilt.android.lifecycle.HiltViewModel
import killua.dev.base.ui.BaseViewModel
import killua.dev.base.ui.SnackbarUIEffect
import killua.dev.base.ui.UIIntent
import killua.dev.base.ui.UIState
import killua.dev.mediadownloader.db.LofterTagsRepository
import killua.dev.mediadownloader.db.TagEntry
import javax.inject.Inject

object TagUtils {
    fun splitTags(input: String): List<String> {
        return input.split(Regex("[,，\\s]"))
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
    }
}

sealed class LofterPrepareTagsPageUIIntents : UIIntent {
    object OnEntry : LofterPrepareTagsPageUIIntents()
    data class UpdateInput(val input: String) : LofterPrepareTagsPageUIIntents()
    data class AddTag(val tag: String) : LofterPrepareTagsPageUIIntents()
    data class RemoveTag(val tag: String) : LofterPrepareTagsPageUIIntents()
    data class AddBatchTags(val tags: List<String>) : LofterPrepareTagsPageUIIntents()
    object SaveTags: LofterPrepareTagsPageUIIntents()
    object ClearAll: LofterPrepareTagsPageUIIntents()
}
data class LofterPrepareTagsPageUIState(
    val tags: Set<String> = emptySet(),
    val currentInput: String = ""
): UIState

@HiltViewModel
class LofterPrepareTagsPageViewModel @Inject constructor(
    private val tagsRepository: LofterTagsRepository
) : BaseViewModel<LofterPrepareTagsPageUIIntents,LofterPrepareTagsPageUIState, SnackbarUIEffect>(LofterPrepareTagsPageUIState()) {
    fun observeAllTags(){
        launchOnIO {
            tagsRepository.observeAllDownloads().collect{
                val tags = it?.tags
                emitState(
                    uiState.value.copy(
                        tags = tags ?: emptySet()
                    )
                )
            }
        }
    }

    fun saveTags(){
        launchOnIO {
            val tagsToSave = TagEntry(tags = uiState.value.tags)
            tagsRepository.insert(tagsToSave)
        }
    }

    override suspend fun onEvent(state: LofterPrepareTagsPageUIState, intent: LofterPrepareTagsPageUIIntents) {
        when (intent) {
            is LofterPrepareTagsPageUIIntents.UpdateInput -> {
                updateState { it.copy(currentInput = intent.input) }
            }
            is LofterPrepareTagsPageUIIntents.AddTag -> {
                if (intent.tag.isNotEmpty()) {
                    updateState {
                        it.copy(
                            tags = it.tags + intent.tag,
                            currentInput = ""
                        )
                    }
                }
            }
            is LofterPrepareTagsPageUIIntents.RemoveTag -> {
                uiState.value.copy(

                )
                updateState { it.copy(tags = it.tags - intent.tag) }
            }
            is LofterPrepareTagsPageUIIntents.AddBatchTags -> {
                updateState {
                    it.copy(
                        tags = it.tags + intent.tags,
                        currentInput = ""
                    )
                }
            }
            is LofterPrepareTagsPageUIIntents.SaveTags -> {
                saveTags()
            }

            LofterPrepareTagsPageUIIntents.OnEntry -> {
                observeAllTags()
            }

            LofterPrepareTagsPageUIIntents.ClearAll -> {
                updateState {
                    it.copy(
                        tags = emptySet(),
                        currentInput = ""
                    )
                }
                tagsRepository.clearAllTags()
            }
        }
    }
}