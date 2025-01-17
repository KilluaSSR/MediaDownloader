package di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import db.Download
import db.DownloadDao
import repository.DownloadRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideDownloadRepository(
        downloadDao: DownloadDao
    ): DownloadRepository = DownloadRepository(downloadDao)
}