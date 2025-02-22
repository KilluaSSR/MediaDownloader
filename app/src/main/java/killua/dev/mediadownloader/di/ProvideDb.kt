package killua.dev.mediadownloader.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import killua.dev.mediadownloader.db.DownloadDao
import killua.dev.mediadownloader.db.MediaDownloadDatabase
import killua.dev.mediadownloader.db.TagDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ProvideDownloadDatabaseModule {
    @Provides
    @Singleton
    fun provideDownloadDatabase(@ApplicationContext context: Context): MediaDownloadDatabase {
        return MediaDownloadDatabase.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideDownloadDao(database: MediaDownloadDatabase): DownloadDao {
        return database.downloadDao()
    }

    @Provides
    @Singleton
    fun provideTagsDao(database: MediaDownloadDatabase): TagDao {
        return database.tagDao()
    }
}