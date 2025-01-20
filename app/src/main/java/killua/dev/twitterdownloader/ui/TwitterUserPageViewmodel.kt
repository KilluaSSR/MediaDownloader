package killua.dev.twitterdownloader.ui

import killua.dev.twitterdownloader.Model.SnackbarUIEffect
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import db.DownloadDao
import db.DownloadStatus
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.plus
import javax.inject.Inject

sealed class TwitterUserPageIntent : UIIntent {
    data class Search(val query: String) : TwitterUserPageIntent()
    data class Sort(val sortType: SortType) : TwitterUserPageIntent()
    object Refresh : TwitterUserPageIntent()
    data class UserSelected(val userId: String) : TwitterUserPageIntent()
}
data class TwitterUserPageState(
    val isSearching: Boolean = false,
    val searchText: String = "",
    val sortType: SortType = SortType.NAME_ASC,
    val originalUsers: Map<String, QueryResult> = emptyMap(),
    val filteredUsers: Map<String, QueryResult> = emptyMap(),
    val selectedUserId: String? = null
) : UIState
data class QueryResult(
    val twitterScreenName: String,
    val twitterUsersName: String,
    val time: Long,
    val count: Int
)
enum class SortType {
    NAME_ASC, NAME_DESC,
    DATE_ASC, DATE_DESC,
    COUNT_ASC, COUNT_DESC
}
@HiltViewModel
class TwitterUserPageViewmodel @Inject constructor(
    private val dao: DownloadDao
): BaseViewModel<TwitterUserPageIntent, TwitterUserPageState, SnackbarUIEffect>(
    TwitterUserPageState(isSearching = false)
){
    private val searchJob = Job()
    private val searchQuery = MutableStateFlow("")
    private val sortType = MutableStateFlow(SortType.NAME_ASC)
    init {
        loadUsers()
        initializeSearchFlow()
    }

    @OptIn(FlowPreview::class)
    private fun initializeSearchFlow() {
        combine(
            searchQuery
                .debounce(300L)
                .distinctUntilChanged(),
            sortType
        ) { query, sort ->
            try {
                emitState(uiState.value.copy(isSearching = true))
                val filtered = performSearch(query)
                val sorted = performSort(filtered, sort)
                emitState(uiState.value.copy(
                    isSearching = false,
                    searchText = query,
                    sortType = sort,
                    filteredUsers = sorted
                ))
            } catch (e: Exception) {
                handleSearchError(e)
            }
        }.flowOnIO().launchIn(viewModelScope.plus(searchJob))
    }
    private fun loadUsers() = launchOnIO {
        try {
            emitState(uiState.value.copy(isSearching = true))
            val users = getAllDownloadList()
            emitState(uiState.value.copy(
                isSearching = false,
                originalUsers = users,
                filteredUsers = users
            ))
        }catch (e: Exception){
            emitState(uiState.value.copy(isSearching = false))
            emitEffect(SnackbarUIEffect.ShowSnackbar(
                message = e.message.toString()
            ))
        }
    }

    override suspend fun onEvent(state: TwitterUserPageState, intent: TwitterUserPageIntent) {
            when(intent){
                is TwitterUserPageIntent.Search -> {
                    searchQuery.value = intent.query
                }
                is TwitterUserPageIntent.Sort -> {
                    sortType.value = intent.sortType
                }
                is TwitterUserPageIntent.Refresh -> loadUsers()


                is TwitterUserPageIntent.UserSelected -> emitState(state.copy(selectedUserId = intent.userId))
            }
    }

    private fun performSearch(query: String): Map<String, QueryResult> {
        return if (query.isEmpty()) {
            uiState.value.originalUsers
        } else {
            uiState.value.originalUsers.filter { (_, user) ->
                user.twitterScreenName.contains(query, ignoreCase = true) ||
                        user.twitterUsersName.contains(query, ignoreCase = true)
            }
        }
    }
    private suspend fun handleSearchError(e: Exception) {
        emitState(uiState.value.copy(isSearching = false))
        emitEffect(SnackbarUIEffect.ShowSnackbar(
            message = "搜索失败：${e.message ?: "Internal Error"}"
        ))
    }

    private fun performSort(users: Map<String, QueryResult>, sortType: SortType): Map<String, QueryResult> {
        return users.toList().let { list ->
            when (sortType) {
                SortType.NAME_ASC -> list.sortedBy { it.second.twitterUsersName }
                SortType.NAME_DESC -> list.sortedByDescending { it.second.twitterUsersName}
                SortType.DATE_ASC -> list.sortedBy { it.second.time }
                SortType.DATE_DESC -> list.sortedByDescending { it.second.time }
                SortType.COUNT_ASC -> list.sortedBy { it.second.count }
                SortType.COUNT_DESC -> list.sortedByDescending { it.second.count }
            }.toMap()
        }
    }

    private suspend fun getAllDownloadList(): Map<String, QueryResult> {
        return dao.getAll()
            .filter { download ->
                download.twitterUserId != null &&
                        download.twitterScreenName != null &&
                        download.fileUri != null &&
                        download.status == DownloadStatus.COMPLETED
            }
            .groupBy { it.twitterUserId!! }
            .mapValues { (_, downloads) ->
                val latestDownload = downloads.maxByOrNull { it.completedAt ?: 0L }!!
                QueryResult(
                    twitterScreenName = latestDownload.twitterScreenName!!,
                    twitterUsersName = latestDownload.twitterName!!,
                    time = latestDownload.completedAt ?: 0L,
                    count = downloads.size
                )
            }
    }
    override fun onCleared() {
        super.onCleared()
        searchJob.cancel()
    }
}