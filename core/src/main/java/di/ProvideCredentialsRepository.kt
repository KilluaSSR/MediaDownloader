package di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import db.UserinfoDAO
import repository.CredentialRepository
import repository.LoginCredentials
import javax.inject.Inject
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindCredentialRepository(
        impl: CredentialRepositoryImpl
    ): CredentialRepository
}
@Singleton
class CredentialRepositoryImpl @Inject constructor(
    private val userInfoDao: UserinfoDAO
) : CredentialRepository {
    override suspend fun getCredentials(): LoginCredentials? {
        return userInfoDao.getUser().let { user ->
            if (! && !user.userauth.isNullOrEmpty()) {
                LoginCredentials(
                    ct0 = user.userct0,
                    authToken = user.userauth
                )
            } else null
        }
    }
}