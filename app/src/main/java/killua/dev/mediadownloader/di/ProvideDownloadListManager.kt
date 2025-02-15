package killua.dev.mediadownloader.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import killua.dev.base.repository.ThumbnailRepository
import killua.dev.base.utils.FileDelete
import killua.dev.base.utils.VideoDurationRepository
import killua.dev.mediadownloader.download.DownloadListManager
import killua.dev.mediadownloader.download.DownloadManager
import killua.dev.mediadownloader.download.DownloadQueueManager
import killua.dev.mediadownloader.repository.DownloadRepository

@Module
@InstallIn(ViewModelComponent::class)
object ManagerModule {
    @Provides
    fun provideDownloadListManager(
        downloadRepository: DownloadRepository,
        downloadManager: DownloadManager,
        thumbnailRepository: ThumbnailRepository,
        videoDurationRepository: VideoDurationRepository,
        downloadQueueManager: DownloadQueueManager,
        fileDelete: FileDelete
    ): DownloadListManager = DownloadListManager(
        downloadRepository,
        downloadManager,
        thumbnailRepository,
        videoDurationRepository,
        downloadQueueManager,
        fileDelete
    )
}