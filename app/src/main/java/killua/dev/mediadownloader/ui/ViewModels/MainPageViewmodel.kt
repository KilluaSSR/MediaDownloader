package killua.dev.mediadownloader.ui.ViewModels

import android.content.Context
import androidx.compose.material3.SnackbarDuration
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import killua.dev.mediadownloader.Model.AvailablePlatforms
import killua.dev.mediadownloader.Model.NotLoggedInPlatform
import killua.dev.mediadownloader.download.DownloadbyLink
import killua.dev.mediadownloader.repository.DownloadRepository
import killua.dev.mediadownloader.ui.SnackbarUIEffect
import killua.dev.mediadownloader.ui.SnackbarUIEffect.*
import killua.dev.mediadownloader.ui.UIIntent
import killua.dev.mediadownloader.ui.UIState
import killua.dev.mediadownloader.utils.navigateLofterProfile
import killua.dev.mediadownloader.utils.navigatePixivProfile
import killua.dev.mediadownloader.utils.navigateTwitterProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.lang.IllegalArgumentException
import javax.inject.Inject

sealed class MainPageUIIntent : UIIntent {
    data class ExecuteDownload(val url: String, val platforms: AvailablePlatforms) : MainPageUIIntent()
    data class NavigateToFavouriteUser(val context: Context, val userID: String,val platforms: AvailablePlatforms, val screenName: String) : MainPageUIIntent()

}

data class MainPageUIState(
    val youHaveDownloadedSth: Boolean = false,
    val favouriteUserName: String = "",
    val favouriteUserScreenName: String = "",
    val favouriteUserID: String = "",
    val favouriteUserFromPlatform: AvailablePlatforms = AvailablePlatforms.Twitter,
    val downloadedTimes: Int = 0,
    val showNotLoggedIn: NotLoggedInPlatform = NotLoggedInPlatform()
) : UIState

@HiltViewModel
class MainPageViewmodel @Inject constructor(
    private val downloadRepository: DownloadRepository,
    private val downloadbyLink: DownloadbyLink,
) : BaseViewModel<MainPageUIIntent, MainPageUIState, SnackbarUIEffect>(
    MainPageUIState()
) {
    private val mutex = Mutex()

    private val _sharedDownloadResult = MutableStateFlow<Result<Unit>?>(null)
    val sharedDownloadResult = _sharedDownloadResult.asStateFlow()

    init {
        launchOnIO {
            observeDownloadCompleted()
            downloadRepository.observeAllDownloads().collect {
                presentFavouriteCardDetails()
            }
        }

    }
    private fun observeDownloadCompleted() {
        viewModelScope.launch {
            downloadbyLink.downloadCompletedFlow.collect {
                presentFavouriteCardDetails()
            }
        }
    }

    suspend fun presentFavouriteCardDetails() {
        val mostDownloaded = downloadRepository.getMostDownloadedUser()
        if (mostDownloaded != null) {
            val state = when(mostDownloaded.platforms) {
                AvailablePlatforms.Twitter, AvailablePlatforms.Lofter -> {
                    uiState.value.copy(
                        youHaveDownloadedSth = true,
                        favouriteUserID = mostDownloaded.userID!!,
                        favouriteUserName = mostDownloaded.name!!,
                        favouriteUserFromPlatform = mostDownloaded.platforms,
                        favouriteUserScreenName = mostDownloaded.screenName!!,
                        downloadedTimes = mostDownloaded.totalDownloads
                    )
                }
                AvailablePlatforms.Pixiv -> {
                    uiState.value.copy(
                        youHaveDownloadedSth = true,
                        favouriteUserID = mostDownloaded.userID!!,
                        favouriteUserName = "",
                        favouriteUserFromPlatform = mostDownloaded.platforms,
                        favouriteUserScreenName = mostDownloaded.screenName!!,
                        downloadedTimes = mostDownloaded.totalDownloads
                    )
                }
                AvailablePlatforms.Kuaikan -> uiState.value
            }
            emitState(state)
        }
    }

    fun handleSharedLink(platforms: AvailablePlatforms, url: String) {
        viewModelScope.launch {
            _sharedDownloadResult.value = try {
                downloadbyLink.checkPlatformLogin(platforms).getOrThrow()
                downloadbyLink.handlePlatformDownload(url, platforms).getOrThrow()
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun onEvent(state: MainPageUIState, intent: MainPageUIIntent) {
        when (intent) {
            is MainPageUIIntent.ExecuteDownload -> {
                mutex.withLock {
                    try {
                        val platform = intent.platforms
                        downloadbyLink.checkPlatformLogin(platform)
                            .onSuccess {
                                downloadbyLink.handlePlatformDownload(intent.url, platform).onFailure { error ->
                                    emitEffect(ShowSnackbar(error.message ?: "Error", "OK", true, SnackbarDuration.Short))
                                }
                            }
                            .onFailure { error ->
                                emitState(uiState.value.copy(showNotLoggedIn = NotLoggedInPlatform(true,platform)))
                            }
                    }catch (e: Exception){
                        emitEffect(ShowSnackbar(e.message.toString(), "OK", true, SnackbarDuration.Short))
                    }
                }
            }
            is MainPageUIIntent.NavigateToFavouriteUser -> {
                withMainContext {
                    when(intent.platforms) {
                        AvailablePlatforms.Twitter -> intent.context.navigateTwitterProfile(intent.userID, intent.screenName)
                        AvailablePlatforms.Lofter -> intent.context.navigateLofterProfile(intent.screenName)
                        AvailablePlatforms.Pixiv -> intent.context.navigatePixivProfile(intent.userID)
                        AvailablePlatforms.Kuaikan -> {}
                    }
                }
            }
        }
    }
}