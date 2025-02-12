package killua.dev.twitterdownloader.ui.ViewModels

import android.content.Context
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import db.Download
import db.DownloadStatus
import killua.dev.base.Model.AvailablePlatforms
import killua.dev.base.Model.DownloadTask
import killua.dev.base.Model.MediaType
import killua.dev.base.Model.patterns
import killua.dev.base.ui.BaseViewModel
import killua.dev.base.ui.SnackbarUIEffect
import killua.dev.base.ui.SnackbarUIEffect.*
import killua.dev.base.ui.UIIntent
import killua.dev.base.ui.UIState
import killua.dev.base.utils.DownloadEventManager
import killua.dev.base.utils.DownloadPreChecks
import killua.dev.base.utils.MediaFileNameStrategy
import killua.dev.twitterdownloader.Model.NetworkResult
import killua.dev.twitterdownloader.api.Kuaikan.KuaikanService
import killua.dev.twitterdownloader.api.Lofter.LofterService
import killua.dev.twitterdownloader.api.Pixiv.PixivService
import killua.dev.twitterdownloader.api.Twitter.TwitterDownloadAPI
import killua.dev.twitterdownloader.download.DownloadQueueManager
import killua.dev.twitterdownloader.repository.DownloadRepository
import killua.dev.twitterdownloader.utils.navigateLofterProfile
import killua.dev.twitterdownloader.utils.navigatePixivProfile
import killua.dev.twitterdownloader.utils.navigateTwitterProfile
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID
import javax.inject.Inject

sealed class MainPageUIIntent : UIIntent {
    data class ExecuteDownload(val url: String) : MainPageUIIntent()
    data class NavigateToFavouriteUser(val context: Context, val userID: String,val platforms: AvailablePlatforms, val screenName: String) : MainPageUIIntent()
    object DismissLoginDialog : MainPageUIIntent()
}

data class MainPageUIState(
    val youHaveDownloadedSth: Boolean = false,
    val favouriteUserName: String = "",
    val favouriteUserScreenName: String = "",
    val favouriteUserID: String = "",
    val favouriteUserFromPlatform: AvailablePlatforms = AvailablePlatforms.Twitter,
    val downloadedTimes: Int = 0,
    val showNotLoggedInDialog: Boolean = false,
    val loginErrorPlatform: AvailablePlatforms = AvailablePlatforms.Twitter
) : UIState

@HiltViewModel
class MainPageViewmodel @Inject constructor(
    private val twitterDownloadAPI: TwitterDownloadAPI,
    private val lofterService: LofterService,
    private val pixivService: PixivService,
    private val kuaiaknService: KuaikanService,
    private val downloadRepository: DownloadRepository,
    private val downloadQueueManager: DownloadQueueManager,
    private val downloadEventManager: DownloadEventManager,
    private val downloadPreChecks: DownloadPreChecks,
) : BaseViewModel<MainPageUIIntent, MainPageUIState, SnackbarUIEffect>(
    MainPageUIState()
) {
    private val mutex = Mutex()
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
            downloadEventManager.downloadCompletedFlow.collect{
                presentFavouriteCardDetails()
            }
        }
    }

    suspend fun presentFavouriteCardDetails() {
        val mostDownloaded = downloadRepository.getMostDownloadedUser()
        if (mostDownloaded != null) {
            when(mostDownloaded.platforms){
                AvailablePlatforms.Twitter -> {
                    emitState(uiState.value.copy(
                        youHaveDownloadedSth = true,
                        favouriteUserID = mostDownloaded.userID!!,
                        favouriteUserName = mostDownloaded.name!!,
                        favouriteUserFromPlatform = mostDownloaded.platforms,
                        favouriteUserScreenName = mostDownloaded.screenName!!,
                        downloadedTimes = mostDownloaded.totalDownloads
                    ))
                }
                AvailablePlatforms.Lofter -> {
                    emitState(uiState.value.copy(
                        youHaveDownloadedSth = true,
                        favouriteUserID = mostDownloaded.userID!!,
                        favouriteUserName = mostDownloaded.name!!,
                        favouriteUserFromPlatform = mostDownloaded.platforms,
                        favouriteUserScreenName = mostDownloaded.screenName!!,
                        downloadedTimes = mostDownloaded.totalDownloads
                    ))
                }
                AvailablePlatforms.Pixiv -> {
                    emitState(uiState.value.copy(
                        youHaveDownloadedSth = true,
                        favouriteUserID = mostDownloaded.userID!!,
                        favouriteUserName = "",
                        favouriteUserFromPlatform = mostDownloaded.platforms,
                        favouriteUserScreenName = mostDownloaded.screenName!!,
                        downloadedTimes = mostDownloaded.totalDownloads
                    ))
                }

                AvailablePlatforms.Kuaikan -> TODO()
            }
        }
    }

    override suspend fun onEvent(state: MainPageUIState, intent: MainPageUIIntent) {
        when (intent) {
            is MainPageUIIntent.ExecuteDownload -> {
                mutex.withLock {
                    val platform = classifyLinks(intent.url)
                    when(platform){
                        AvailablePlatforms.Twitter -> {
                            downloadPreChecks.checkTwitterLoggedIn().onSuccess {
                                handleNewDownload(intent.url, AvailablePlatforms.Twitter)
                            }.onFailure { error ->
                                emitState(uiState.value.copy(
                                    showNotLoggedInDialog = true,
                                    loginErrorPlatform = AvailablePlatforms.Twitter
                                ))
                            }
                        }
                        AvailablePlatforms.Lofter -> {
                            downloadPreChecks.checkLofterLoggedIn().onSuccess {
                                handleNewDownload(intent.url, AvailablePlatforms.Lofter)
                            }.onFailure { error ->
                                emitState(uiState.value.copy(
                                    showNotLoggedInDialog = true,
                                    loginErrorPlatform = AvailablePlatforms.Lofter
                                ))
                            }
                        }

                        AvailablePlatforms.Pixiv -> {
                            downloadPreChecks.checkPixivLoggedIn().onSuccess {
                                handleNewDownload(intent.url, AvailablePlatforms.Pixiv)
                            }.onFailure { error ->
                                emitState(uiState.value.copy(
                                    showNotLoggedInDialog = true,
                                    loginErrorPlatform = AvailablePlatforms.Pixiv
                                ))
                            }
                        }

                        AvailablePlatforms.Kuaikan -> {
                            handleNewDownload(intent.url, AvailablePlatforms.Kuaikan)
                        }
                    }
                }
            }
            is MainPageUIIntent.NavigateToFavouriteUser -> {
                withMainContext {
                    when(intent.platforms){
                        AvailablePlatforms.Twitter -> intent.context.navigateTwitterProfile(intent.userID,intent.screenName)
                        AvailablePlatforms.Lofter -> intent.context.navigateLofterProfile(intent.screenName)
                        AvailablePlatforms.Pixiv -> intent.context.navigatePixivProfile(intent.userID)
                        AvailablePlatforms.Kuaikan -> {}
                    }
                }
            }
            MainPageUIIntent.DismissLoginDialog -> {
                emitState(uiState.value.copy(
                    showNotLoggedInDialog = false
                ))
            }
        }
    }

    private fun classifyLinks(urlLink: String): AvailablePlatforms{
        return patterns.entries.firstOrNull{ (pattern, _) ->
            urlLink.contains(pattern, ignoreCase = true)
        }?.value!!
    }

    private suspend fun handleNewDownload(url: String, platforms: AvailablePlatforms) {
        downloadPreChecks.canStartDownload().onSuccess {
            when(platforms) {
                AvailablePlatforms.Twitter -> handleTwitterDownload(url)
                AvailablePlatforms.Lofter -> handleLofterDownload(url)
                AvailablePlatforms.Pixiv -> handlePixivDownload(url)
                AvailablePlatforms.Kuaikan -> handleKuaikanDownload(url)
            }
        }.onFailure { error ->
            viewModelScope.launch {
                emitEffect(ShowSnackbar(error.message.toString(), withDismissAction = true, actionLabel = "OKAY"))
            }
        }
    }
    private suspend fun handleKuaikanDownload(url: String){
        try {
            when(val result = kuaiaknService.getSingleChapter(url)){
                is NetworkResult.Error -> {
                    viewModelScope.launch {
                        emitState(uiState.value.copy(
                            showNotLoggedInDialog = true,
                            loginErrorPlatform = AvailablePlatforms.Lofter
                        ))
                    }
                }
                is NetworkResult.Success -> {

                    viewModelScope.launch{
                        createAndStartDownload(
                            url = result.data.urlList.joinToString(separator = ","),
                            userId = result.data.title,
                            screenName = result.data.title,
                            platform = AvailablePlatforms.Kuaikan,
                            name = result.data.chapter,
                            tweetID = result.data.title,
                            mainLink = url,
                            mediaType = MediaType.PDF
                        )
                    }

                }
            }
        }catch (e: Exception) {
            viewModelScope.launch {
                emitEffect(ShowSnackbar(e.message ?: "Internal Error"))
            }
        }
    }

    private suspend fun handlePixivDownload(url: String){
        try {
            when(val result = pixivService.getSingleBlogImage(url)){
                is NetworkResult.Error -> {
                    viewModelScope.launch {
                        emitEffect(ShowSnackbar("Pixiv request error", withDismissAction = true, actionLabel = "OKAY"))
                    }
                }
                is NetworkResult.Success -> {
                    result.data.originalUrls.forEach { imageURL ->
                        createAndStartDownload(
                            url = imageURL,
                            userId = result.data.userId,
                            screenName = result.data.userName,
                            platform = AvailablePlatforms.Pixiv,
                            name = result.data.title,
                            tweetID = imageURL,
                            mainLink = url,
                            mediaType = MediaType.PHOTO
                        )
                    }
                }
            }
        }catch (e: Exception) {
            viewModelScope.launch {
                emitEffect(ShowSnackbar(e.message ?: "Internal Error"))
            }
        }
    }

    private suspend fun handleTwitterDownload(url: String) {
        val tweetId = url.split("?")[0].split("/").last()
        try {
            when (val result = twitterDownloadAPI.getTwitterSingleMediaDetailAsync(tweetId)) {
                is NetworkResult.Success -> {
                    val user = result.data.user
                    // 处理视频
                    result.data.videoUrls.forEach { videoUrl ->
                        createAndStartDownload(
                            url = videoUrl,
                            userId = user?.id,
                            screenName = user?.screenName ?: "",
                            platform = AvailablePlatforms.Twitter,
                            name = user?.name ?: "",
                            tweetID = tweetId,
                            mainLink = videoUrl,
                            mediaType = MediaType.VIDEO
                        )
                    }
                    // 处理图片
                    if(result.data.photoUrls.isNotEmpty()) {
                        downloadPreChecks.checkPhotosDownload().onSuccess {
                            result.data.photoUrls.forEach { photoUrl ->
                                createAndStartDownload(
                                    url = photoUrl,
                                    userId = user?.id,
                                    screenName = user?.screenName ?: "",
                                    platform = AvailablePlatforms.Twitter,
                                    name = user?.name ?: "",
                                    tweetID = tweetId,
                                    mainLink = photoUrl,
                                    mediaType = MediaType.PHOTO
                                )
                            }
                        }.onFailure { error ->
                            viewModelScope.launch {
                                emitEffect(ShowSnackbar(error.message.toString(), "OKAY", true))
                            }
                        }
                    }
                }
                is NetworkResult.Error -> {
                    viewModelScope.launch {
                        emitEffect(ShowSnackbar("Twitter request error", withDismissAction = true, actionLabel = "OKAY"))
                    }
                }
            }
        } catch (e: Exception) {
            viewModelScope.launch {
                emitEffect(ShowSnackbar(e.message ?: "Internal Error"))
            }
        }
    }

    private fun handleLofterDownload(url: String) {
        try {
            when (val result = lofterService.getBlogImages(url)) {
                is NetworkResult.Success -> {
                    val data = result.data
                    data.images.forEach { image ->
                        createAndStartDownload(
                            url = image.url,
                            userId = data.authorId,
                            screenName = data.authorDomain,
                            platform = AvailablePlatforms.Lofter,
                            name = data.authorName,
                            tweetID = image.url,
                            mainLink = url,
                            mediaType = MediaType.PHOTO
                        )
                    }
                }
                is NetworkResult.Error -> {
                    viewModelScope.launch {
                        emitEffect(ShowSnackbar(result.message.toString(), withDismissAction = true, actionLabel = "OKAY"))
                    }
                }
            }
        } catch (e: Exception) {
            viewModelScope.launch {
                emitEffect(ShowSnackbar(e.message ?: "Internal Error"))
            }
        }
    }

    private fun createAndStartDownload(
        url: String,
        uuid: String = UUID.randomUUID().toString(),
        userId: String?,
        screenName: String,
        platform: AvailablePlatforms,
        name: String,
        tweetID: String,
        mainLink: String,
        mediaType: MediaType
    ) {
        val fileNameStrategy = MediaFileNameStrategy(mediaType)
        val fileName = fileNameStrategy.generateMedia(screenName)

        try {
            val download = Download(
                uuid = uuid,
                userId = userId,
                screenName = screenName,
                type = platform,
                name = name,
                tweetID = tweetID,
                fileUri = null,
                link = mainLink,
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
                        from = download.type,
                        fileName = fileName,
                        screenName = screenName,
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