package killua.dev.mediadownloader.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import killua.dev.mediadownloader.utils.MeidaDurationRepository
import killua.dev.mediadownloader.utils.MediaDurationRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    @Singleton
    fun provideVideoDurationRepository(
        @ApplicationContext context: Context
    ): MeidaDurationRepository = MediaDurationRepositoryImpl(context)
}