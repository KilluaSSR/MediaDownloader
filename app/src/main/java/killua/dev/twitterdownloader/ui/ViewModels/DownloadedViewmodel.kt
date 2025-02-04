package killua.dev.twitterdownloader.ui.ViewModels

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import db.Download
import db.DownloadStatus
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
import killua.dev.base.ui.filters.TypeFilter
import killua.dev.base.utils.TwitterMediaFileNameStrategy
import killua.dev.base.utils.VideoDurationRepository
import killua.dev.twitterdownloader.Model.TwitterDownloadItem
import killua.dev.twitterdownloader.download.DownloadManager
import killua.dev.twitterdownloader.download.DownloadQueueManager
import killua.dev.twitterdownloader.repository.DownloadRepository
import killua.dev.twitterdownloader.utils.NavigateTwitterTweet
import kotlinx.coroutines.launch
import java.io.File
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
    data class GoToTwitter(val downloadId: String, val context: Context) : DownloadPageUIIntent
}

data class DownloadPageUIState(
    val optionIndex: Int,
    val optionsType: DownloadPageDestinations,
    val isLoading: Boolean,
    val unfilteredDownloads: List<TwitterDownloadItem> = emptyList(),
    val downloads: List<TwitterDownloadItem> = emptyList(),
    val thumbnailCache: Map<Uri, Bitmap?> = emptyMap(),
    val availableAuthors: List<String> = emptyList(),
    val filterOptions: FilterOptions = FilterOptions(),
    val downloadProgress: Map<String, DownloadProgress> = emptyMap()
) : UIState

@HiltViewModel
class DownloadedViewModel @Inject constructor(
    private val downloadRepository: DownloadRepository,
    private val downloadManager: DownloadManager,
    private val thumbnailRepository: ThumbnailRepository,
    private val videoDurationRepository: VideoDurationRepository,
    private val downloadQueueManager: DownloadQueueManager
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
                val authors = downloads.mapNotNull { it.twitterName }.sortedBy { it.length }
                // 将数据库查询到的内容转为 DownloadItem 列表
                val unfilteredItems = downloads.map { TwitterDownloadItem.fromDownload(it) }
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
        unfiltered: List<TwitterDownloadItem>,
        destination: DownloadPageDestinations,
        filterOptions: FilterOptions
    ): List<TwitterDownloadItem> {
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

        // 再根据作者和时长过滤
        val selectedAuthors = filterOptions.selectedAuthors
        return filteredByDestination.filter { item ->
            val authorMatch = selectedAuthors.isEmpty() ||
                    (item.twitterName.isNotBlank() && item.twitterName in selectedAuthors)

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
            val typeMatch = when (filterOptions.typeFilter) {
                TypeFilter.All -> true
                TypeFilter.Videos -> item.fileType == MediaType.VIDEO
                TypeFilter.Images -> item.fileType == MediaType.PHOTO
            }
            authorMatch && durationMatch && typeMatch
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
                    downloadRepository.delete(downloadRepository.getById(intent.downloadId)!!)
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

            is DownloadPageUIIntent.GoToTwitter -> {
                launchOnIO {
                    val download = downloadRepository.getById(intent.downloadId)
                    if (download == null) {
                        launchOnIO { emitEffect(SnackbarUIEffect.ShowSnackbar("Not Found")) }
                        return@launchOnIO
                    }
                    val userScreenName = download.twitterScreenName
                    val tweetID = download.tweetID
                    withMainContext {
                        intent.context.NavigateTwitterTweet(userScreenName!!, tweetID)
                    }
                }
            }
        }
    }

    private suspend fun cancelDownload(downloadId: String) {
        val download = downloadRepository.getById(downloadId) ?: return
        download.fileUri?.path?.let { File(it).delete() }
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

        val fileNameStrategy = TwitterMediaFileNameStrategy(mediaType)
        val fileName = fileNameStrategy.generate(old.twitterScreenName)

        cancelDownload(downloadId)

        val newdownload = Download(
            uuid = old.uuid,
            twitterUserId = old.twitterUserId,
            twitterScreenName = old.twitterScreenName,
            twitterName = old.twitterName,
            tweetID = old.tweetID,
            fileUri = null,
            link = old.link,
            fileName = fileName,
            fileType = mediaType.name.lowercase(),
            fileSize = 0L,
            status = DownloadStatus.PENDING,
            mimeType = mediaType.mimeType
        )

        downloadRepository.insert(newdownload)
        downloadQueueManager.enqueue(
            DownloadTask(
                id = old.uuid,
                url = old.link!!,
                fileName = fileName,
                screenName = old.twitterScreenName!!,
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