package killua.dev.twitterdownloader.ui.ViewModels

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import db.Download
import db.DownloadStatus
import killua.dev.base.Model.AvailablePlatforms
import killua.dev.base.Model.DownloadPageDestinations
import killua.dev.base.Model.DownloadProgress
import killua.dev.base.Model.DownloadTask
import killua.dev.base.Model.MediaType
import killua.dev.base.repository.ThumbnailRepository
import killua.dev.base.ui.BaseViewModel
import killua.dev.base.ui.SnackbarUIEffect
import killua.dev.base.ui.UIIntent
import killua.dev.base.ui.UIState
import killua.dev.base.ui.filters.DurationFilter
import killua.dev.base.ui.filters.FilterOptions
import killua.dev.base.ui.filters.PlatformFilter
import killua.dev.base.ui.filters.TypeFilter
import killua.dev.base.utils.FileDelete
import killua.dev.base.utils.MediaFileNameStrategy
import killua.dev.base.utils.VideoDurationRepository
import killua.dev.twitterdownloader.Model.DownloadedItem
import killua.dev.twitterdownloader.download.DownloadManager
import killua.dev.twitterdownloader.download.DownloadQueueManager
import killua.dev.twitterdownloader.repository.DownloadRepository
import killua.dev.twitterdownloader.utils.NavigateToLofter
import killua.dev.twitterdownloader.utils.NavigateTwitterTweet
import kotlinx.coroutines.launch
import javax.inject.Inject


sealed interface DownloadPageUIIntent : UIIntent {
    object LoadDownloads : DownloadPageUIIntent
    data class FilterDownloads(val filter: DownloadPageDestinations) : DownloadPageUIIntent
    data class ResumeDownload(val downloadId: String) : DownloadPageUIIntent
    data class RetryDownload(val downloadId: String) : DownloadPageUIIntent
    data class PauseDownload(val downloadId: String) : DownloadPageUIIntent
    data class CancelDownload(val downloadId: String) : DownloadPageUIIntent
    data object CancelAll : DownloadPageUIIntent
    data object RetryAll : DownloadPageUIIntent
    data class UpdateFilterOptions(val filterOptions: FilterOptions) : DownloadPageUIIntent
    data class GoTo(val downloadId: String, val context: Context) : DownloadPageUIIntent
}

data class DownloadPageUIState(
    val optionIndex: Int,
    val optionsType: DownloadPageDestinations,
    val isLoading: Boolean,
    val unfilteredDownloads: List<DownloadedItem> = emptyList(),
    val downloads: List<DownloadedItem> = emptyList(),
    val thumbnailCache: Map<Uri, Bitmap?> = emptyMap(),
    val availableAuthors: Set<String> = emptySet(),
    val filterOptions: FilterOptions = FilterOptions(),
    val downloadProgress: Map<String, DownloadProgress> = emptyMap()
) : UIState

@HiltViewModel
class DownloadedViewModel @Inject constructor(
    private val downloadRepository: DownloadRepository,
    private val downloadManager: DownloadManager,
    private val thumbnailRepository: ThumbnailRepository,
    private val videoDurationRepository: VideoDurationRepository,
    private val downloadQueueManager: DownloadQueueManager,
    private val fileDelete: FileDelete
) : BaseViewModel<DownloadPageUIIntent, DownloadPageUIState, SnackbarUIEffect>(
    DownloadPageUIState(
        optionIndex = 0,
        isLoading = true,
        optionsType = DownloadPageDestinations.All
    )
) {
    init {
        observeDownloadsFromDB()
        viewModelScope.launch {
            downloadManager.downloadProgress.collect { progressList ->
                for ((downloadId, progress) in progressList) {
                    val currentProgress = uiState.value.downloadProgress[downloadId]
                    emitState(
                        uiState.value.copy(
                            downloadProgress = uiState.value.downloadProgress + (downloadId to
                                    DownloadProgress(
                                        progress = progress,
                                        isCompleted = currentProgress?.isCompleted == true,
                                        isFailed = currentProgress?.isFailed == true,
                                        errorMessage = currentProgress?.errorMessage
                                    )
                           )
                        )
                    )
                }
            }
        }
    }

    private fun observeDownloadsFromDB() {
        launchOnIO {
            downloadRepository.observeAllDownloads().collect { downloads ->
                val authors = downloads.mapNotNull { it.name }.sortedBy { it.length }.toSet()
                // 将数据库查询到的内容转为 DownloadItem 列表
                val unfilteredItems = downloads.map { DownloadedItem.fromDownload(it) }
                    .sortedByDescending { it.createdAt }

                // 然后根据当前 UIState 的 optionsType、filterOptions 做过滤
                val filteredItems = applyFilters(
                    unfilteredItems,
                    uiState.value.optionsType,
                    uiState.value.filterOptions
                )

                val uris = downloads.mapNotNull { it.fileUri }
                val thumbnails = uris.associateWith { uri -> thumbnailRepository.getThumbnail(uri) }

                emitState(
                    uiState.value.copy(
                        unfilteredDownloads = unfilteredItems,
                        downloads = filteredItems,
                        thumbnailCache = thumbnails,
                        availableAuthors = authors,
                        isLoading = false
                    )
                )
            }
        }
    }

    private suspend fun applyFilters(
        unfiltered: List<DownloadedItem>,
        destination: DownloadPageDestinations,
        filterOptions: FilterOptions
    ): List<DownloadedItem> {
        // 先根据 DownloadPageDestinations 做一次筛选
        val filteredByDestination = when (destination) {
            DownloadPageDestinations.All -> unfiltered
            DownloadPageDestinations.Downloading -> unfiltered.filter {
                it.downloadState.toDownloadStatus() == DownloadStatus.DOWNLOADING
            }
            DownloadPageDestinations.Completed -> unfiltered.filter {
                it.downloadState.toDownloadStatus() == DownloadStatus.COMPLETED
            }
            DownloadPageDestinations.Failed -> unfiltered.filter {
                it.downloadState.toDownloadStatus() == DownloadStatus.FAILED
            }
        }

        // 再根据作者和时长/类型过滤
        val selectedAuthors = filterOptions.selectedAuthors
        filterOptions.platformFilter
        return filteredByDestination.filter { item ->
            // 作者过滤
            val authorMatch = selectedAuthors.isEmpty() ||
                    (item.name.isNotBlank() && item.name in selectedAuthors)

            // 判断是否选择了具体时长（非 All）
            val durationFilterSelected = filterOptions.durationFilter != DurationFilter.All

            // 如果选择了具体时长，就只显示视频
            if (durationFilterSelected && item.fileType != MediaType.VIDEO) {
                return@filter false
            }

            // 对视频进行实际时长判断
            val duration = item.fileUri?.let {
                videoDurationRepository.getVideoDuration(it)
            } ?: 0L
            val durationMatch = when (filterOptions.durationFilter) {
                DurationFilter.All -> true
                DurationFilter.UnderOneMinute -> duration <= 60
                DurationFilter.OneToThreeMinutes -> duration in 61..180
                DurationFilter.ThreeToTenMinutes -> duration in 181..600
                DurationFilter.MoreThanTemMinutes -> duration > 600
            }

            // 如果时长过滤是 All，则按用户的类型过滤；否则已在上方统一卡掉非视频内容
            val typeMatch = if (durationFilterSelected) {
                true
            } else {
                when (filterOptions.typeFilter) {
                    TypeFilter.All -> true
                    TypeFilter.Videos -> item.fileType == MediaType.VIDEO
                    TypeFilter.Images -> item.fileType == MediaType.PHOTO
                }
            }

            val platformMatch = when (filterOptions.platformFilter) {
                PlatformFilter.All -> true
                PlatformFilter.Twitter -> item.type == AvailablePlatforms.Twitter
                PlatformFilter.Lofter -> item.type == AvailablePlatforms.Lofter
            }

            authorMatch && durationMatch && typeMatch && platformMatch
        }
    }

    override suspend fun onEvent(state: DownloadPageUIState, intent: DownloadPageUIIntent) {
        when (intent) {
            is DownloadPageUIIntent.LoadDownloads -> {
                observeDownloadsFromDB()
            }

            is DownloadPageUIIntent.FilterDownloads -> {
                launchOnIO {
                    val filtered = applyFilters(
                        state.unfilteredDownloads,
                        intent.filter,
                        state.filterOptions
                    )
                    emitState(
                        state.copy(
                            optionsType = intent.filter,
                            downloads = filtered
                        )
                    )
                }
            }

            is DownloadPageUIIntent.UpdateFilterOptions -> {
                val newFilterOptions = intent.filterOptions
                val filtered = applyFilters(
                    state.unfilteredDownloads,
                    state.optionsType,
                    newFilterOptions
                )
                emitState(
                    state.copy(
                        filterOptions = newFilterOptions,
                        downloads = filtered
                    )
                )
            }

            is DownloadPageUIIntent.ResumeDownload -> {
                launchOnIO {
                    downloadRepository.updateStatus(intent.downloadId, DownloadStatus.DOWNLOADING)
                }
            }

            is DownloadPageUIIntent.PauseDownload -> {
                launchOnIO {
                    downloadRepository.updateStatus(intent.downloadId, DownloadStatus.PENDING)
                }
            }

            is DownloadPageUIIntent.CancelDownload -> {
                launchOnIO {
                    cancelDownload(intent.downloadId)
                }
            }

            DownloadPageUIIntent.CancelAll -> handleCancelAll()
            DownloadPageUIIntent.RetryAll -> handleRetryAll()
            is DownloadPageUIIntent.RetryDownload -> {
                launchOnIO {
                    retryDownload(intent.downloadId)
                }
            }

            is DownloadPageUIIntent.GoTo -> {
                launchOnIO {
                    val download = downloadRepository.getById(intent.downloadId)
                    if (download == null) {
                        launchOnIO { emitEffect(SnackbarUIEffect.ShowSnackbar("Not Found")) }
                        return@launchOnIO
                    }
                    when(download.type){
                        AvailablePlatforms.Twitter -> {
                            val userScreenName = download.screenName
                            val tweetID = download.tweetID
                            withMainContext {
                                intent.context.NavigateTwitterTweet(userScreenName!!, tweetID)
                            }
                        }
                        AvailablePlatforms.Lofter -> {
                            val link = download.tweetID!!
                            withMainContext {
                                intent.context.NavigateToLofter(link)
                            }
                        }
                    }
                }
            }
        }
    }

    private suspend fun cancelDownload(downloadId: String) {
        val download = downloadRepository.getById(downloadId) ?: return
        println("DOWNLOAD GOT ${download.uuid}, ${downloadId}")
        download.fileUri?.let { uri ->
            fileDelete.deleteFile(uri)
        }
        downloadRepository.deleteById(downloadId)
    }

    private suspend fun handleCancelAll() {
        val allDownloads = downloadRepository.getActiveDownloads()
        if (allDownloads.isEmpty()) {
            viewModelScope.launch {
                emitEffect(SnackbarUIEffect.ShowSnackbar("No active downloads"))
            }
            return
        }
        allDownloads.forEach { cancelDownload(it.uuid) }
    }

    private suspend fun retryDownload(downloadId: String) {
        val old = downloadRepository.getById(downloadId) ?: return
        val mediaType = when(old.fileType) {
            "video" -> MediaType.VIDEO
            "photo" -> MediaType.PHOTO
            else -> MediaType.VIDEO
        }

        val fileNameStrategy = MediaFileNameStrategy(mediaType)
        val fileName = fileNameStrategy.generate(old.screenName)

        cancelDownload(downloadId)

        val newDownload = Download(
            uuid = old.uuid,
            userId  = old.userId,
            screenName = old.screenName,
            name = old.name,
            type = old.type,
            tweetID = old.tweetID,
            fileUri = null,
            link = old.link,
            fileName = fileName,
            fileType = mediaType.name.lowercase(),
            fileSize = 0L,
            status = DownloadStatus.PENDING,
            mimeType = mediaType.mimeType
        )

        downloadRepository.insert(newDownload)
        downloadQueueManager.enqueue(
            DownloadTask(
                id = old.uuid,
                url = old.link!!,
                fileName = fileName,
                screenName = old.screenName!!,
                type = mediaType
            )
        )
    }

    private suspend fun handleRetryAll() {
        val failedDownloads = downloadRepository.getByStatus(status = DownloadStatus.FAILED)
        if (failedDownloads.isEmpty()) {
            viewModelScope.launch {
                emitEffect(SnackbarUIEffect.ShowSnackbar("No failed downloads"))
            }
            return
        }
        failedDownloads.forEach { old ->
            retryDownload(old.uuid)
        }
    }
}