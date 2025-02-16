package killua.dev.mediadownloader.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import killua.dev.mediadownloader.utils.VideoDurationRepository
import killua.dev.mediadownloader.utils.VideoDurationRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    @Singleton
    fun provideVideoDurationRepository(
        @ApplicationContext context: Context
    ): VideoDurationRepository = VideoDurationRepositoryImpl(context)
}