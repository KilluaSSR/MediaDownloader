package killua.dev.twitterdownloader.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import killua.dev.base.Model.LofterLoginKey
import killua.dev.base.datastore.ApplicationUserDataLofter
import killua.dev.base.datastore.ApplicationUserDataTwitter
import killua.dev.base.datastore.readApplicationUserAuth
import killua.dev.base.datastore.readApplicationUserCt0
import killua.dev.base.datastore.readLofterLoginAuth
import killua.dev.base.datastore.readLofterLoginKey
import killua.dev.base.di.ApplicationScope
import killua.dev.twitterdownloader.api.Lofter.LofterService
import killua.dev.twitterdownloader.api.Twitter.TwitterDownloadSingleMedia
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject
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
    private val _userData = MutableStateFlow(ApplicationUserDataTwitter("", ""))
    val userTwitterData: StateFlow<ApplicationUserDataTwitter> = _userData.asStateFlow()

    private val _userLofterData = MutableStateFlow(ApplicationUserDataLofter("", ""))
    val userLofterData: StateFlow<ApplicationUserDataLofter> = _userLofterData.asStateFlow()

    init {
        scope.launch {
            // Twitter 数据流
            combine(
                context.readApplicationUserCt0(),
                context.readApplicationUserAuth()
            ) { ct0, auth ->
                ApplicationUserDataTwitter(ct0, auth)
            }.collect {
                _userData.value = it
            }
        }

        scope.launch {
            // Lofter 数据流
            combine(
                context.readLofterLoginKey(),
                context.readLofterLoginAuth()
            ) { key, auth ->
                ApplicationUserDataLofter(
                    login_key = key,
                    login_auth = auth
                )
            }.collect {
                _userLofterData.value = it
            }
        }
    }
}


@Module
@InstallIn(SingletonComponent::class)
object ProvideAPI {
    @Provides
    @Singleton
    fun provideTwitterSingleMedia(
        userDataManager: UserDataManager
    ): TwitterDownloadSingleMedia {
        return TwitterDownloadSingleMedia(userDataManager)
    }

    @Provides
    @Singleton
    fun provideLofterService(
        userDataManager: UserDataManager,
        @ApplicationScope scope: CoroutineScope
    ): LofterService {
        return LofterService(userDataManager,scope)
    }
}

