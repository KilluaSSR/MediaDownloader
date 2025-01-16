package di

import TwitterApiService
import api.TwitterApiClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import repository.CredentialRepository
import repository.LoginCredentials

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    suspend fun provideTwitterApiService(
        credentialRepository: CredentialRepository
    ): TwitterApiService {
        val creds = credentialRepository.getCredentials()
            ?: return TwitterApiService(TwitterApiClient.buildClient(LoginCredentials("","")))
        return TwitterApiService(TwitterApiClient.buildClient(creds))
    }
}