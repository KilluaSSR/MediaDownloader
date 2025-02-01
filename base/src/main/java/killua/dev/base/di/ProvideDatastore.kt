package killua.dev.base.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import killua.dev.base.datastore.PreferencesDataSource
import killua.dev.base.repository.SettingsRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {
    @Provides
    @Singleton
    internal fun providesPreferencesDataStore(
        @ApplicationContext context: Context,
        @Dispatcher(DbDispatchers.IO) ioDispatcher: CoroutineDispatcher,
        @ApplicationScope scope: CoroutineScope,
    ): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(scope = CoroutineScope(scope.coroutineContext + ioDispatcher)) {
            context.preferencesDataStoreFile("Datastore")
        }

    @Provides
    @Singleton
    fun providePreferencesDataSource(
        dataStore: DataStore<Preferences>
    ): PreferencesDataSource = PreferencesDataSource(dataStore)

    @Provides
    @Singleton
    fun provideSettingsRepository(
        preferencesDataSource: PreferencesDataSource,
        @ApplicationScope scope: CoroutineScope
    ): SettingsRepository = SettingsRepository(
        preferencesDataSource = preferencesDataSource, scope
    )
}