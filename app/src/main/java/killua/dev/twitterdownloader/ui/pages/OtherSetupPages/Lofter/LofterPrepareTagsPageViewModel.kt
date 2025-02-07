package killua.dev.twitterdownloader.ui.pages.OtherSetupPages.Lofter

import dagger.hilt.android.lifecycle.HiltViewModel
import killua.dev.base.ui.BaseViewModel
import killua.dev.base.ui.SnackbarUIEffect
import killua.dev.base.ui.UIIntent
import killua.dev.base.ui.UIState
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
    data class UpdateInput(val input: String) : LofterPrepareTagsPageUIIntents()
    data class AddTag(val tag: String) : LofterPrepareTagsPageUIIntents()
    data class RemoveTag(val tag: String) : LofterPrepareTagsPageUIIntents()
    data class AddBatchTags(val tags: List<String>) : LofterPrepareTagsPageUIIntents()
    object SaveTags: LofterPrepareTagsPageUIIntents()
}
data class LofterPrepareTagsPageUIState(
    val tags: Set<String> = emptySet(),
    val currentInput: String = ""
): UIState

@HiltViewModel
class LofterPrepareTagsPageViewModel @Inject constructor() : BaseViewModel<LofterPrepareTagsPageUIIntents,LofterPrepareTagsPageUIState, SnackbarUIEffect>(LofterPrepareTagsPageUIState()) {
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
                // TODO: 实现保存逻辑
            }
        }
    }
}