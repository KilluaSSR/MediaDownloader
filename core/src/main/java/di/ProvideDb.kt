package di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import db.DownloadDao
import db.TwitterDownloadDatabase
import db.UserinfoDAO
import db.UserinfoDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
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

    @Provides
    @Singleton
    fun provideUserinfoDatabase(@ApplicationContext context: Context): UserinfoDatabase {
        return UserinfoDatabase.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideUserinfoDao(database: UserinfoDatabase): UserinfoDAO {
        return database.userinfoDao()
    }
}