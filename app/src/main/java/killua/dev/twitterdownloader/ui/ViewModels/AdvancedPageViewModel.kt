package killua.dev.twitterdownloader.ui.ViewModels

import android.content.Context
import android.os.Build
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
import killua.dev.twitterdownloader.api.Twitter.Model.TwitterUser
import killua.dev.twitterdownloader.api.Twitter.TwitterDownloadAPI
import killua.dev.twitterdownloader.download.DownloadQueueManager
import killua.dev.twitterdownloader.repository.DownloadRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID
import javax.inject.Inject

data class AdvancedPageUIState(
    val isLofterLoggedIn: Boolean = false,
    val isEligibleToUseLofterGetByTags: Boolean = false,
    val isGettingMyTwitterBookmark: Boolean = false
): UIState

sealed class AdvancedPageUIIntent : UIIntent {
    data class OnEntry(val context: Context): AdvancedPageUIIntent()
    data object GetMyTwitterBookmark: AdvancedPageUIIntent()
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
                viewModelScope.launch{
                    twitterDownloadAPI.getBookmarksAllTweets { user, newPhotoUrls, newVideoUrls ->
                        // 处理视频下载
                        newVideoUrls.forEach { url ->
                            createAndStartDownloadTwitterSingleMedia(url, user, MediaType.VIDEO)
                        }
                        newPhotoUrls.forEach { url ->
                            createAndStartDownloadTwitterSingleMedia(url, user, MediaType.PHOTO)
                        }
                    }
                }
            }
        }
    }

    private fun createAndStartDownloadTwitterSingleMedia(
        url: String,
        user: TwitterUser?,
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
                tweetID = null,
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
            handleError("Failed", e)
        }
    }

    private fun handleError(message: String, error: Exception) {
        viewModelScope.launch{
            emitEffect(ShowSnackbar("$message: ${error.message ?: "Internal Error"}", actionLabel =  "OKAY" , withDismissAction = true))
        }
    }
}