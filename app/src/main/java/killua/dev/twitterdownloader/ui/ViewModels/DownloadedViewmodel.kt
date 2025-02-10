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
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

@OptIn(FlowPreview::class)
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
        optionsType = DownloadPageDestinations.All,
        thumbnailCache = emptyMap()
    )
) {
    private val _thumbnailCache = MutableStateFlow<Map<Uri, Bitmap?>>(emptyMap())
    private val _authors = MutableStateFlow<Set<String>>(emptySet())
    private val filteredResultsCache = mutableMapOf<DownloadPageDestinations, List<DownloadedItem>>()
    init {
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

        viewModelScope.launch {
            downloadRepository.observeAllDownloads().collect { downloads ->
                // 1. 处理作者列表
                val authors = downloads
                    .mapNotNull { it.name }
                    .filter { it.isNotBlank() }
                    .sortedBy { it.length }
                    .toSet()

                // 2. 处理基础数据
                val unfilteredItems = downloads.map { DownloadedItem.fromDownload(it) }
                    .sortedByDescending { it.createdAt }

                // 3. 更新当前页面数据
                val currentDestination = uiState.value.optionsType
                val currentFiltered = applyFilters(
                    unfilteredItems,
                    currentDestination,
                    uiState.value.filterOptions
                )

                // 4. 更新状态，包括作者列表
                emitState(
                    uiState.value.copy(
                        unfilteredDownloads = unfilteredItems,
                        downloads = currentFiltered,
                        availableAuthors = authors,  // 确保更新作者列表
                        isLoading = false
                    )
                )

                // 5. 后台预加载其他标签页数据
                launchOnIO {
                    DownloadPageDestinations.values()
                        .filter { it != currentDestination }
                        .forEach { destination ->
                            filteredResultsCache[destination] = applyFilters(
                                unfilteredItems,
                                destination,
                                uiState.value.filterOptions
                            )
                        }
                }
            }
        }
    }

    private fun observeDownloadsFromDB() {
        launchOnIO {
            downloadRepository.observeAllDownloads().collect { downloads ->
                val authors = downloads.mapNotNull { it.name }
                    .sortedBy { it.length }
                    .toSet()

                val unfilteredItems = downloads.map { DownloadedItem.fromDownload(it) }
                    .sortedByDescending { it.createdAt }

                val filteredItems = applyFilters(
                    unfilteredItems,
                    uiState.value.optionsType,
                    uiState.value.filterOptions
                )

                emitState(
                    uiState.value.copy(
                        unfilteredDownloads = unfilteredItems,
                        downloads = filteredItems,
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
    ): List<DownloadedItem> = withContext(Dispatchers.Default) {
        // 1. 首先按状态过滤，这是最快的操作
        val statusFiltered = when (destination) {
            DownloadPageDestinations.All -> unfiltered
            DownloadPageDestinations.Downloading -> unfiltered.filter {
                it.downloadState.toDownloadStatus() == DownloadStatus.DOWNLOADING || it.downloadState.toDownloadStatus() == DownloadStatus.PENDING
            }
            DownloadPageDestinations.Completed -> unfiltered.filter {
                it.downloadState.toDownloadStatus() == DownloadStatus.COMPLETED
            }
            DownloadPageDestinations.Failed -> unfiltered.filter {
                it.downloadState.toDownloadStatus() == DownloadStatus.FAILED
            }
        }

        // 2. 如果过滤后列表为空，直接返回
        if (statusFiltered.isEmpty()) {
            return@withContext emptyList()
        }

        // 3. 预加载视频时长（如果需要）
        val durationFilterSelected = filterOptions.durationFilter != DurationFilter.All
        val durationMap = if (durationFilterSelected) {
            statusFiltered.asSequence()
                .filter { it.fileType == MediaType.VIDEO }
                .mapNotNull { it.fileUri }
                .distinct()
                .toList()
                .map { uri ->
                    async {
                        uri to videoDurationRepository.getVideoDuration(uri)
                    }
                }
                .awaitAll()
                .toMap()
        } else {
            emptyMap()
        }

        // 4. 加载缩略图
        val newThumbnails = statusFiltered
            .mapNotNull { it.fileUri }
            .filter { uri -> !uiState.value.thumbnailCache.containsKey(uri) }
            .map { uri ->
                async {
                    uri to thumbnailRepository.getThumbnail(uri)
                }
            }
            .awaitAll()
            .toMap()

        // 5. 更新缩略图缓存
        if (newThumbnails.isNotEmpty()) {
            emitState(
                uiState.value.copy(
                    thumbnailCache = uiState.value.thumbnailCache + newThumbnails
                )
            )
        }

        // 6. 应用剩余过滤条件
        statusFiltered.filter { item ->
            // 作者过滤
            val authorMatch = filterOptions.selectedAuthors.isEmpty() ||
                    (item.name.isNotBlank() && item.name in filterOptions.selectedAuthors)

            // 判断是否选择了具体时长（非 All）
            val durationFilterSelected = filterOptions.durationFilter != DurationFilter.All

            // 如果选择了具体时长，就只显示视频
            if (durationFilterSelected && item.fileType != MediaType.VIDEO) {
                return@filter false
            }

            // 对视频进行实际时长判断
            val duration = if (durationFilterSelected) {
                item.fileUri?.let { durationMap[it] } ?: 0L
            } else {
                0L
            }

            val durationMatch = when (filterOptions.durationFilter) {
                DurationFilter.All -> true
                DurationFilter.UnderOneMinute -> duration <= 60
                DurationFilter.OneToThreeMinutes -> duration in 61..180
                DurationFilter.ThreeToTenMinutes -> duration in 181..600
                DurationFilter.MoreThanTemMinutes -> duration > 600
            }

            // 类型过滤
            val typeMatch = if (durationFilterSelected) {
                true
            } else {
                when (filterOptions.typeFilter) {
                    TypeFilter.All -> true
                    TypeFilter.Videos -> item.fileType == MediaType.VIDEO
                    TypeFilter.Images -> item.fileType == MediaType.PHOTO
                }
            }

            // 平台过滤
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
            is DownloadPageUIIntent.LoadDownloads -> observeDownloadsFromDB()

            is DownloadPageUIIntent.FilterDownloads -> handleFilterDownloads(state, intent)

            is DownloadPageUIIntent.UpdateFilterOptions -> handleUpdateFilterOptions(state, intent)

            is DownloadPageUIIntent.ResumeDownload ->
                updateDownloadStatus(intent.downloadId, DownloadStatus.DOWNLOADING)

            is DownloadPageUIIntent.PauseDownload ->
                updateDownloadStatus(intent.downloadId, DownloadStatus.PENDING)

            is DownloadPageUIIntent.CancelDownload ->
                launchOnIO { cancelDownload(intent.downloadId) }

            DownloadPageUIIntent.CancelAll -> handleBulkOperation(
                operation = { cancelDownload(it.uuid) },
                emptyMessage = "No active downloads"
            )

            DownloadPageUIIntent.RetryAll -> handleBulkOperation(
                operation = { retryDownload(it.uuid) },
                emptyMessage = "No failed downloads"
            )

            is DownloadPageUIIntent.RetryDownload ->
                launchOnIO { retryDownload(intent.downloadId) }

            is DownloadPageUIIntent.GoTo -> handleNavigation(intent)
        }
    }

    private suspend fun cancelDownload(downloadId: String) {
        val download = downloadRepository.getById(downloadId) ?: return
        download.fileUri?.let { uri ->
            fileDelete.deleteFile(uri)
        }
        downloadRepository.deleteById(downloadId)
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
                screenName = old.screenName ?: "",
                type = mediaType
            )
        )
    }

    private suspend fun handleBulkOperation(
        operation: suspend (Download) -> Unit,
        emptyMessage: String
    ) {
        val downloads = downloadRepository.getActiveDownloads()
        if (downloads.isEmpty()) {
            showMessage(emptyMessage)
            return
        }
        downloads.forEach { operation(it) }
    }

    private fun handleNavigation(intent: DownloadPageUIIntent.GoTo) {
        launchOnIO {
            val download = downloadRepository.getById(intent.downloadId) ?: run {
                showMessage("Not Found")
                return@launchOnIO
            }

            withMainContext {
                when(download.type) {
                    AvailablePlatforms.Twitter -> {
                        intent.context.NavigateTwitterTweet(
                            download.screenName,
                            download.tweetID,
                            download.link
                        )
                    }
                    AvailablePlatforms.Lofter -> {
                        intent.context.NavigateToLofter(download.tweetID!!)
                    }
                }
            }
        }
    }
    private suspend fun handleFilterDownloads(
        state: DownloadPageUIState,
        intent: DownloadPageUIIntent.FilterDownloads
    ) {
        filteredResultsCache[intent.filter]?.let { cached ->
            emitState(state.copy(
                optionsType = intent.filter,
                downloads = cached
            ))
            return
        }

        launchOnIO {
            val filtered = applyFilters(
                state.unfilteredDownloads,
                intent.filter,
                state.filterOptions
            )
            filteredResultsCache[intent.filter] = filtered
            if (state.optionsType == intent.filter) {
                emitState(state.copy(downloads = filtered))
            }
        }
    }
    private suspend fun handleUpdateFilterOptions(
        state: DownloadPageUIState,
        intent: DownloadPageUIIntent.UpdateFilterOptions
    ) {
        val filtered = applyFilters(
            state.unfilteredDownloads,
            state.optionsType,
            intent.filterOptions
        )
        emitState(state.copy(
            filterOptions = intent.filterOptions,
            downloads = filtered
        ))
    }

    private fun Download.toDownloadTask(): DownloadTask {
        val mediaType = when(fileType) {
            "video" -> MediaType.VIDEO
            "photo" -> MediaType.PHOTO
            else -> MediaType.VIDEO
        }
        return DownloadTask(
            id = uuid,
            url = link!!,
            fileName = fileName,
            screenName = screenName ?: "",
            type = mediaType
        )
    }

    private fun showMessage(message: String) {
        viewModelScope.launch {
            emitEffect(SnackbarUIEffect.ShowSnackbar(message))
        }
    }

    private suspend fun updateDownloadStatus(downloadId: String, status: DownloadStatus) {
        launchOnIO {
            downloadRepository.updateStatus(downloadId, status)
        }
    }
}