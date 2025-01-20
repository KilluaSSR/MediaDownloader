package killua.dev.twitterdownloader.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import db.DownloadDao
import db.TwitterDownloadDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ProvideDownloadDatabaseModule {
    @Provides
    @Singleton
    fun provideDownloadDatabase(@ApplicationContext context: Context): TwitterDownloadDatabase {
        return TwitterDownloadDatabase.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideDownloadDao(database: TwitterDownloadDatabase): DownloadDao {
        return database.downloadDao()
    }
}