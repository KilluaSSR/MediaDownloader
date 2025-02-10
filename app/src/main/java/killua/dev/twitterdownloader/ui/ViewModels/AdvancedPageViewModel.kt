package killua.dev.twitterdownloader.ui.ViewModels

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import db.Download
import db.DownloadStatus
import killua.dev.base.Model.AvailablePlatforms
import killua.dev.base.Model.DownloadTask
import killua.dev.base.Model.MediaType
import killua.dev.base.datastore.readLofterLoginAuth
import killua.dev.base.datastore.readLofterLoginKey
import killua.dev.base.states.CurrentState
import killua.dev.base.ui.BaseViewModel
import killua.dev.base.ui.SnackbarUIEffect
import killua.dev.base.ui.SnackbarUIEffect.ShowSnackbar
import killua.dev.base.ui.UIIntent
import killua.dev.base.ui.UIState
import killua.dev.base.utils.MediaFileNameStrategy
import killua.dev.twitterdownloader.Model.NetworkResult
import killua.dev.twitterdownloader.api.Twitter.Model.TwitterUser
import killua.dev.twitterdownloader.api.Twitter.TwitterDownloadAPI
import killua.dev.twitterdownloader.download.DownloadQueueManager
import killua.dev.twitterdownloader.repository.DownloadRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID
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
    private val downloadQueueManager: DownloadQueueManager,
    private val downloadRepository: DownloadRepository,
): BaseViewModel<AdvancedPageUIIntent, AdvancedPageUIState, SnackbarUIEffect>(AdvancedPageUIState()) {
    private val mutex = Mutex()
    private val _lofterLoginState: MutableStateFlow<CurrentState> =
        MutableStateFlow(CurrentState.Idle)
    val lofterLoginState: StateFlow<CurrentState> =
        _lofterLoginState.stateInScope(CurrentState.Idle)
    val lofterGetByTagsEligibility: StateFlow<Boolean> = _lofterLoginState.map { login ->
        login == CurrentState.Success
    }.flowOnIO().stateInScope(false)

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override suspend fun onEvent(state: AdvancedPageUIState, intent: AdvancedPageUIIntent) {
        when(intent){
            is AdvancedPageUIIntent.OnEntry -> {
                mutex.withLock {
                    val loginKey = intent.context.readLofterLoginKey().first()
                    val loginAuth = intent.context.readLofterLoginAuth().first()
                    if(loginKey.isNotBlank() && loginAuth.isNotBlank()){
                        _lofterLoginState.value = CurrentState.Success
                    }
                }
            }

            AdvancedPageUIIntent.GetMyTwitterBookmark -> {
                viewModelScope.launch {
                    twitterDownloadAPI.getBookmarksAllTweets(
                        onNewItems = { bookmarks ->
                            bookmarks.forEach { bookmark ->
                                bookmark.videoUrls.forEach { url ->
                                    createAndStartDownloadTwitterSingleMedia(
                                        url = url,
                                        user = bookmark.user,
                                        tweetID = bookmark.tweetId,
                                        mediaType = MediaType.VIDEO
                                    )
                                }
                                bookmark.photoUrls.forEach { url ->
                                    createAndStartDownloadTwitterSingleMedia(
                                        url = url,
                                        user = bookmark.user,
                                        tweetID = bookmark.tweetId,
                                        mediaType = MediaType.PHOTO
                                    )
                                }
                            }
                        },
                        onError = { errorMessage ->
                            handleError(errorMessage)
                        }
                    )
                }
            }
            AdvancedPageUIIntent.GetMyTwitterLiked -> {
                viewModelScope.launch {
                    twitterDownloadAPI.getLikesAllTweets(
                        onNewItems = { tweets ->
                            tweets.forEach { tweet ->
                                tweet.videoUrls.forEach { url ->
                                    createAndStartDownloadTwitterSingleMedia(
                                        url = url,
                                        user = tweet.user,
                                        tweetID = tweet.tweetId,
                                        mediaType = MediaType.VIDEO
                                    )
                                }
                                tweet.photoUrls.forEach { url ->
                                    createAndStartDownloadTwitterSingleMedia(
                                        url = url,
                                        user = tweet.user,
                                        tweetID = tweet.tweetId,
                                        mediaType = MediaType.PHOTO
                                    )
                                }
                            }
                        },
                        onError = { errorMessage ->
                            handleError(errorMessage)
                        }
                    )
                }
            }

            is AdvancedPageUIIntent.GetSomeonesTwitterAccountInfo -> {
                viewModelScope.launch {
                    emitState(uiState.value.copy(isFetchingTwitterUserInfo = true))
                    when (val result = twitterDownloadAPI.getUserBasicInfo(intent.screenName)) {
                        is NetworkResult.Success -> {
                            result.data.let { userInfo ->
                                emitState(uiState.value.copy(
                                    isFetchingTwitterUserInfo = false,
                                    TwitterUserAccountInfo = Triple(
                                        userInfo.id ?: "",
                                        userInfo.name ?: "",
                                        userInfo.screenName ?: ""
                                    )
                                ))
                            }
                        }
                        is NetworkResult.Error -> {
                            emitState(uiState.value.copy(isFetchingTwitterUserInfo = false))
                            handleError(result.message)
                        }
                    }
                }
            }

            is AdvancedPageUIIntent.OnConfirmTwitterDownloadMedia -> {
                viewModelScope.launch {
                    val userId = intent.id
                    val userScreenName = intent.screenName
                    if (userId.isEmpty()) {
                        return@launch
                    }

                    twitterDownloadAPI.getUserMediaByUserId(
                        userId = userId,
                        screenName = userScreenName,
                        onNewItems = { tweets ->
                            tweets.forEach { tweet ->
                                tweet.videoUrls.forEach { url ->
                                    createAndStartDownloadTwitterSingleMedia(
                                        url = url,
                                        user = tweet.user,
                                        tweetID = tweet.tweetId,
                                        mediaType = MediaType.VIDEO
                                    )
                                }
                                tweet.photoUrls.forEach { url ->
                                    createAndStartDownloadTwitterSingleMedia(
                                        url = url,
                                        user = tweet.user,
                                        tweetID = tweet.tweetId,
                                        mediaType = MediaType.PHOTO
                                    )
                                }
                            }
                        },
                        onError = { errorMessage ->
                            handleError(errorMessage)
                        }
                    )
                }
            }
        }
    }

    private fun createAndStartDownloadTwitterSingleMedia(
        url: String,
        user: TwitterUser?,
        tweetID: String,
        mediaType: MediaType
    ) {
        val uuid = UUID.randomUUID().toString()
        val fileNameStrategy = MediaFileNameStrategy(mediaType)
        val fileName = fileNameStrategy.generate(user?.screenName)

        try {
            val download = Download(
                uuid = uuid,
                userId = user?.id,
                screenName = user?.screenName,
                type = AvailablePlatforms.Twitter,
                name = user?.name,
                tweetID = tweetID,
                fileUri = null,
                link = url,
                fileName = fileName,
                fileType = mediaType.name.lowercase(),
                fileSize = 0L,
                status = DownloadStatus.PENDING,
                mimeType = mediaType.mimeType
            )

            viewModelScope.launch {
                downloadRepository.insert(download)
                downloadQueueManager.enqueue(
                    DownloadTask(
                        id = uuid,
                        url = url,
                        fileName = fileName,
                        screenName = user?.screenName ?: "",
                        type = mediaType
                    )
                )
            }
        } catch (e: Exception) {
            handleError(e.message.toString())
        }
    }

    private fun handleError(error: String) {
        viewModelScope.launch{
            emitEffect(ShowSnackbar(error, actionLabel =  "OKAY" , withDismissAction = true))
        }
    }
}