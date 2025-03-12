package killua.dev.mediadownloader.ui.ViewModels

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import killua.dev.mediadownloader.Model.AvailablePlatforms
import killua.dev.mediadownloader.download.DownloadListManager
import killua.dev.mediadownloader.repository.TwitterSubscriptionRepository
import killua.dev.mediadownloader.ui.SnackbarUIEffect
import killua.dev.mediadownloader.ui.UIIntent
import killua.dev.mediadownloader.ui.UIState
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

sealed interface SubscribePageUIIntent : UIIntent {
    data class ToggleSubscription(val author: String, val isSubscribed: Boolean) : SubscribePageUIIntent
    data object LoadSubscriptions : SubscribePageUIIntent
    data class FilterAuthors(val query: String) : SubscribePageUIIntent
    data class BatchUpdateSubscriptions(val updates: Map<String, Boolean>) : SubscribePageUIIntent
}

data class SubscribePageUIState(
    val twitterAuthors: List<Pair<String, Boolean>> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val filterQuery: String = "",
    val filteredAuthors: List<Pair<String, Boolean>> = emptyList(),
) : UIState


@HiltViewModel
class SubscribePageViewModel @Inject constructor(
    private val downloadListManager: DownloadListManager,
    private val twitterSubscriptionRepository: TwitterSubscriptionRepository
) : BaseViewModel<SubscribePageUIIntent, SubscribePageUIState, SnackbarUIEffect>(SubscribePageUIState()) {
    private val mutex = Mutex()

    companion object {
        const val EVENT_SUBSCRIPTION_ADDED = "subscription_added"
        const val EVENT_SUBSCRIPTION_REMOVED = "subscription_removed"
        const val EVENT_BATCH_UPDATE_SUCCESS = "batch_update_success"
        const val EVENT_ERROR = "error"
    }

    init {
        viewModelScope.launch {
            loadData()
        }
    }

    private suspend fun loadData() {
        try {
            emitState(uiState.value.copy(isLoading = true))
            downloadListManager.observeAllDownloads()
                .map { downloads ->
                    downloads.filter { it.platform == AvailablePlatforms.Twitter }
                        .mapNotNull { it.name }
                        .toSet()
                }
                .combine(twitterSubscriptionRepository.getAllSubscriptions()) { twitterAuthors, subscriptions ->
                    Pair(twitterAuthors, subscriptions)
                }
                .catch { error ->
                    emitState(uiState.value.copy(
                        isLoading = false,
                        error = error.message
                    ))
                }
                .collect { (twitterAuthors, subscriptions) ->
                    mutex.withLock {
                        updateState(twitterAuthors, subscriptions)
                    }
                }
        } catch (e: Exception) {
            emitState(uiState.value.copy(
                isLoading = false,
                error = e.message
            ))
        }
    }

    private suspend fun updateState(
        twitterAuthors: Set<String>,
        subscriptions: Map<String, Boolean>
    ) {
        val authorPairs = twitterAuthors.map { author ->
            Pair(author, subscriptions[author] == true)
        }

        emitState(uiState.value.copy(
            twitterAuthors = authorPairs,
            filteredAuthors = filterAuthors(authorPairs, uiState.value.filterQuery),
            isLoading = false,
            error = null
        ))
    }

    private fun filterAuthors(authors: List<Pair<String, Boolean>>, query: String): List<Pair<String, Boolean>> {
        return if (query.isBlank()) {
            authors
        } else {
            authors.filter { (name, _) ->
                name.contains(query, ignoreCase = true)
            }
        }
    }

    override suspend fun onEvent(state: SubscribePageUIState, intent: SubscribePageUIIntent) {
        when (intent) {
            is SubscribePageUIIntent.ToggleSubscription -> toggleSubscription(intent.author, intent.isSubscribed)
            is SubscribePageUIIntent.LoadSubscriptions -> refreshData()
            is SubscribePageUIIntent.FilterAuthors -> applyFilter(intent.query)
            is SubscribePageUIIntent.BatchUpdateSubscriptions -> batchUpdateSubscriptions(intent.updates)
        }
    }

    private fun toggleSubscription(author: String, isSubscribed: Boolean) {
        viewModelScope.launch {
            try {
                twitterSubscriptionRepository.updateSubscription(author, isSubscribed)

                val updatedAuthors = uiState.value.twitterAuthors.map { (name, subscribed) ->
                    if (name == author) Pair(name, isSubscribed) else Pair(name, subscribed)
                }

                emitState(uiState.value.copy(
                    twitterAuthors = updatedAuthors,
                    filteredAuthors = filterAuthors(updatedAuthors, uiState.value.filterQuery)
                ))

                val eventType = if (isSubscribed) EVENT_SUBSCRIPTION_ADDED else EVENT_SUBSCRIPTION_REMOVED
                emitEffect(SnackbarUIEffect.ShowSnackbar(
                    message = "$eventType:$author"
                ))
            } catch (e: Exception) {
                emitEffect(SnackbarUIEffect.ShowSnackbar(
                    message = "$EVENT_ERROR:${e.javaClass.simpleName}"
                ))
            }
        }
    }

    private fun refreshData() {
        viewModelScope.launch {
            loadData()
        }
    }

    private fun applyFilter(query: String) {
        viewModelScope.launch {
            mutex.withLock {
                emitState(uiState.value.copy(
                    filterQuery = query,
                    filteredAuthors = filterAuthors(uiState.value.twitterAuthors, query)
                ))
            }
        }
    }

    private fun batchUpdateSubscriptions(updates: Map<String, Boolean>) {
        viewModelScope.launch {
            try {
                twitterSubscriptionRepository.updateBatchSubscriptions(updates)

                val updatedAuthors = uiState.value.twitterAuthors.map { (name, _) ->
                    Pair(name,
                        (updates[name]
                            ?: uiState.value.twitterAuthors.find { it.first == name }?.second) == true
                    )
                }

                emitState(uiState.value.copy(
                    twitterAuthors = updatedAuthors,
                    filteredAuthors = filterAuthors(updatedAuthors, uiState.value.filterQuery)
                ))

                emitEffect(SnackbarUIEffect.ShowSnackbar(
                    message = EVENT_BATCH_UPDATE_SUCCESS
                ))
            } catch (e: Exception) {
                emitEffect(SnackbarUIEffect.ShowSnackbar(
                    message = "$EVENT_ERROR:${e.javaClass.simpleName}"
                ))
            }
        }
    }
}