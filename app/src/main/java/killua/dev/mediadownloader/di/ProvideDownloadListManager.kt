package killua.dev.mediadownloader.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import killua.dev.mediadownloader.download.DownloadListManager
import killua.dev.mediadownloader.download.DownloadManager
import killua.dev.mediadownloader.download.DownloadQueueManager
import killua.dev.mediadownloader.repository.DownloadRepository
import killua.dev.mediadownloader.repository.ThumbnailRepository
import killua.dev.mediadownloader.utils.FileDelete
import killua.dev.mediadownloader.utils.VideoDurationRepository

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
        fileDelete: FileDelete,
        @ApplicationContext context: Context
    ): DownloadListManager = DownloadListManager(
        downloadRepository,
        downloadManager,
        thumbnailRepository,
        videoDurationRepository,
        downloadQueueManager,
        fileDelete,
        context
    )
}