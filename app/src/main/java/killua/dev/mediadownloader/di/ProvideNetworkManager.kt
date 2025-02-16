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
object ProvideNetworkManagerModule {
    @Provides
    @Singleton
    fun provideNetworkManager(@ApplicationContext context: Context): killua.dev.mediadownloader.utils.NetworkManager =
        killua.dev.mediadownloader.utils.NetworkManager(context)
}