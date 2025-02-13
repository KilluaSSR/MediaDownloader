package killua.dev.twitterdownloader.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import killua.dev.base.repository.ThumbnailRepository
import killua.dev.base.utils.FileDelete
import killua.dev.base.utils.VideoDurationRepository
import killua.dev.twitterdownloader.download.DownloadListManager
import killua.dev.twitterdownloader.download.DownloadManager
import killua.dev.twitterdownloader.download.DownloadQueueManager
import killua.dev.twitterdownloader.repository.DownloadRepository

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