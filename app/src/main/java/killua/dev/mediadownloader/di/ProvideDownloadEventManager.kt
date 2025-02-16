package killua.dev.mediadownloader.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ProvideDownloadEventManager{
    @Provides
    @Singleton
    fun provideDownloadEventManager(): killua.dev.mediadownloader.utils.DownloadEventManager =
        killua.dev.mediadownloader.utils.DownloadEventManager()
}