package killua.dev.twitterdownloader.di

import killua.dev.twitterdownloader.api.TwitterApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import killua.dev.twitterdownloader.repository.CredentialRepository
import killua.dev.twitterdownloader.repository.LoginCredentials
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ProvideTwitterApiService {
    @Provides
    @Singleton
    @ApplicationScope
    fun provideApplicationScope(): CoroutineScope {
        return CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }

    @Provides
    @Singleton
    suspend fun provideTwitterApiService(
        credentialRepository: CredentialRepository,
        @ApplicationScope appScope: CoroutineScope
    ): TwitterApiService {
        val credentials = credentialRepository.getCredentials() ?: LoginCredentials("", "")

        val client = ProvideTwitterApiClient.buildClient(credentials)
        return TwitterApiService(client, credentials)
    }
}
@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class ApplicationScope