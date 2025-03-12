package killua.dev.mediadownloader.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import killua.dev.mediadownloader.repository.TwitterSubscriptionRepository
import killua.dev.mediadownloader.repository.TwitterSubscriptionRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ProvideTwitterSubscriptionRepository {
    @Binds
    @Singleton
    abstract fun bindTwitterSubscriptionRepository(
        impl: TwitterSubscriptionRepositoryImpl
    ): TwitterSubscriptionRepository
}