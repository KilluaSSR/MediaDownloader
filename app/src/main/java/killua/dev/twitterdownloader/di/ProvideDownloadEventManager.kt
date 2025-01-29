package killua.dev.twitterdownloader.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import killua.dev.twitterdownloader.utils.DownloadEventManager
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ProvideDownloadEventManager{
    @Provides
    @Singleton
    fun provideDownloadEventManager(): DownloadEventManager = DownloadEventManager()
}