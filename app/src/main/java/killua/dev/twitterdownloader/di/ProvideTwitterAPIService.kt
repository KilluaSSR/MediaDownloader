package killua.dev.twitterdownloader.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import killua.dev.base.datastore.ApplicationUserData
import killua.dev.twitterdownloader.api.TwitterApiService
import killua.dev.base.datastore.readApplicationUserAuth
import killua.dev.base.datastore.readApplicationUserCt0
import killua.dev.base.datastore.readApplicationUserName
import killua.dev.base.datastore.readApplicationUserScreenName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Qualifier
import javax.inject.Singleton
@Module
@InstallIn(SingletonComponent::class)
object UserDataModule {
    @Provides
    @Singleton
    fun provideUserDataManager(
        @ApplicationContext context: Context,
        @ApplicationScope scope: CoroutineScope
    ): UserDataManager {
        return UserDataManager(context, scope)
    }
}
@Singleton
class UserDataManager @Inject constructor(
    private val context: Context,
    private val scope: CoroutineScope
) {
    private val _userData = MutableStateFlow(ApplicationUserData("", "", "", ""))
    val userData: StateFlow<ApplicationUserData> = _userData.asStateFlow()

    init {
        scope.launch {
            combine(
                context.readApplicationUserScreenName(),
                context.readApplicationUserName(),
                context.readApplicationUserCt0(),
                context.readApplicationUserAuth()
            ) { screenName, userName, ct0, auth ->
                ApplicationUserData(screenName, userName, ct0, auth)
            }.collect {
                _userData.value = it
            }
        }
    }
}


@Module
@InstallIn(SingletonComponent::class)
object ProvideTwitterApiService {
    @Provides
    @Singleton
    fun provideTwitterApiService(
        userDataManager: UserDataManager
    ): TwitterApiService {
        val client = ProvideTwitterApiClient.buildClient(userDataManager.userData.value)
        return TwitterApiService(client, userDataManager)
    }
}

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class ApplicationScope