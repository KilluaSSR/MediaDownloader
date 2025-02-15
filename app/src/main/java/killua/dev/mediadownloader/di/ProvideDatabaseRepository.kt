package killua.dev.mediadownloader.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import db.DownloadDao
import killua.dev.mediadownloader.db.LofterTagsRepository
import killua.dev.mediadownloader.db.TagDao
import killua.dev.mediadownloader.repository.DownloadRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ProvideDatabaseRepositoryModule {
    @Provides
    @Singleton
    fun provideDownloadRepository(
        downloadDao: DownloadDao
    ): DownloadRepository = DownloadRepository(downloadDao)

    @Provides
    @Singleton
    fun provideTagsRepository(
        tagDao: TagDao
    ): LofterTagsRepository = LofterTagsRepository(tagDao)
}

