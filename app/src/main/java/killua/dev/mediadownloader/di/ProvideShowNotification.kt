package killua.dev.mediadownloader.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ProvideShowNotification {
    @Provides
    @Singleton
    fun provideShowNotification(
        @ApplicationContext context: Context
    ) = killua.dev.mediadownloader.utils.ShowNotification(context)
}