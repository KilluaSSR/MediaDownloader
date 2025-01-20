package killua.dev.twitterdownloader.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import killua.dev.twitterdownloader.core.utils.NetworkManager
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ProvideNetworkManagerModule {
    @Provides
    @Singleton
    fun provideNetworkManager(@ApplicationContext context: Context): NetworkManager =
        NetworkManager(context)
}