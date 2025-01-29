package killua.dev.twitterdownloader.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import killua.dev.twitterdownloader.DownloadEventManager
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ProvideDownloadEventManager{
    @Provides
    @Singleton
    fun provideDownloadEventManager(@ApplicationContext context: Context): DownloadEventManager = DownloadEventManager(context)
}