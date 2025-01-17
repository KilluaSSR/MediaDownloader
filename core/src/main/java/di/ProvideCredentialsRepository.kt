package di

import android.content.Context
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.core.DataStore
import androidx.datastore.core.MultiProcessDataStoreFactory
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferencesSerializer
import androidx.datastore.preferences.preferencesDataStoreFile
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import db.UserPreuserferences
import kotlinx.coroutines.flow.first
import repository.CredentialRepository
import repository.LoginCredentials
import javax.inject.Inject
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ProvideUserDataStoreModule {
    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(
            produceFile = { context.preferencesDataStoreFile("user_data") }
        )
}

@Module
@InstallIn(SingletonComponent::class)
abstract class ProvideCredentialRepositoryModule {
    @Binds
    @Singleton
    abstract fun bindCredentialRepository(
        impl: CredentialRepositoryImpl
    ): CredentialRepository
}
@Singleton
class CredentialRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : CredentialRepository {
    override suspend fun getCredentials(): LoginCredentials? {
        val userdata = UserPreuserferences.getUserdata(context).first()
        return userdata?.let {
            LoginCredentials(it.userct0,it.userauth)
        }
    }
}
