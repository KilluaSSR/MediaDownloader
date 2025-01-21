package killua.dev.twitterdownloader.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import db.DownloadDao
import killua.dev.twitterdownloader.repository.DownloadRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ProvideDownloadRepositoryModule {
    @Provides
    @Singleton
    fun provideDownloadRepository(
        downloadDao: DownloadDao
    ): DownloadRepository = DownloadRepository(downloadDao)
}