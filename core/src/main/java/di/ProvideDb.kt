package di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import db.TwitterDownloadDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): TwitterDownloadDatabase {
        return TwitterDownloadDatabase.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideDownloadDao(database: TwitterDownloadDatabase): DownloadDao {
        return database.downloadDao()
    }
}