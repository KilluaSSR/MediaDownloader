package di

import api.TwitterApiService
import api.TwitterApiClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.runBlocking
import repository.CredentialRepository
import repository.LoginCredentials
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
    fun provideTwitterApiService(
        credentialRepository: CredentialRepository,
        @ApplicationScope appScope: CoroutineScope
    ): TwitterApiService {
        val creds = runBlocking {
            withContext(appScope.coroutineContext) {
                credentialRepository.getCredentials() ?: LoginCredentials("", "")
            }
        }

        val client = TwitterApiClient.buildClient(creds)
        return TwitterApiService(client, creds)
    }
}
@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class ApplicationScope