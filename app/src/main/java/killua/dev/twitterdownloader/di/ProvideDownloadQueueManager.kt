package killua.dev.twitterdownloader.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import killua.dev.twitterdownloader.download.DownloadQueueManager
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ProvideDownloadQueueManager {
    @Provides
    @Singleton
    fun provideDownloadQueueManager() = DownloadQueueManager()
}