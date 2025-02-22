package killua.dev.mediadownloader.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import killua.dev.mediadownloader.api.Kuaikan.KuaikanService
import killua.dev.mediadownloader.api.Lofter.LofterService
import killua.dev.mediadownloader.api.MissEvan.MissEvanService
import killua.dev.mediadownloader.api.Pixiv.PixivService
import killua.dev.mediadownloader.api.Twitter.TwitterDownloadAPI
import killua.dev.mediadownloader.datastore.ApplicationUserDataLofter
import killua.dev.mediadownloader.datastore.ApplicationUserDataTwitter
import killua.dev.mediadownloader.datastore.readApplicationUserAuth
import killua.dev.mediadownloader.datastore.readApplicationUserCt0
import killua.dev.mediadownloader.datastore.readApplicationUserID
import killua.dev.mediadownloader.datastore.readDelay
import killua.dev.mediadownloader.datastore.readKuaikanPassToken
import killua.dev.mediadownloader.datastore.readLofterEndTime
import killua.dev.mediadownloader.datastore.readLofterLoginAuth
import killua.dev.mediadownloader.datastore.readLofterLoginKey
import killua.dev.mediadownloader.datastore.readLofterStartTime
import killua.dev.mediadownloader.datastore.readMissEvanToken
import killua.dev.mediadownloader.datastore.readPixivPHPSSID
import killua.dev.mediadownloader.download.DownloadQueueManager
import killua.dev.mediadownloader.download.DownloadbyLink
import killua.dev.mediadownloader.repository.DownloadRepository
import killua.dev.mediadownloader.repository.DownloadServicesRepository
import killua.dev.mediadownloader.utils.DownloadEventManager
import killua.dev.mediadownloader.utils.DownloadPreChecks
import killua.dev.mediadownloader.utils.FileUtils
import killua.dev.mediadownloader.utils.ShowNotification
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
    scope: CoroutineScope
) {
    private val _userData = MutableStateFlow(ApplicationUserDataTwitter("", "", ""))
    val userTwitterData: StateFlow<ApplicationUserDataTwitter> = _userData.asStateFlow()

    private val _userLofterData = MutableStateFlow(ApplicationUserDataLofter("", "", 0, 0))
    val userLofterData: StateFlow<ApplicationUserDataLofter> = _userLofterData.asStateFlow()


    private val _userPixivPHPSSID = MutableStateFlow("")
    val userPixivPHPSSID: StateFlow<String> = _userPixivPHPSSID.asStateFlow()

    private val _userKuaikanData = MutableStateFlow("")
    val userKuaikanData: StateFlow<String> = _userKuaikanData.asStateFlow()

    private val _userMissEvanData = MutableStateFlow("")
    val userMissEvanData: StateFlow<String> = _userMissEvanData.asStateFlow()

    private val _delay = MutableStateFlow(2)
    val delay : StateFlow<Int> = _delay.asStateFlow()

    init {
        scope.launch {
            // Twitter 数据流
            combine(
                context.readApplicationUserCt0(),
                context.readApplicationUserAuth(),
                context.readApplicationUserID()
            ) { ct0, auth, twid ->
                ApplicationUserDataTwitter(ct0, auth, twid)
            }.collect {
                _userData.value = it
            }
        }

        scope.launch {
            // Lofter 数据流
            combine(
                context.readLofterLoginKey(),
                context.readLofterLoginAuth(),
                context.readLofterStartTime(),
                context.readLofterEndTime()
            ) { key, auth, startTime, endTime ->
                ApplicationUserDataLofter(
                    login_key = key,
                    login_auth = auth,
                    start_time = startTime,
                    end_time = endTime
                )
            }.collect {
                _userLofterData.value = it
            }
        }

        scope.launch{
            context.readKuaikanPassToken().collect{
                _userKuaikanData.value = it
            }
        }

        scope.launch{
            context.readDelay().collect{
                _delay.value = it
            }
        }

        scope.launch{
            context.readPixivPHPSSID().collect{
                _userPixivPHPSSID.value = it
            }
        }

        scope.launch{
            context.readMissEvanToken().collect{
                _userMissEvanData.value = it
            }
        }
    }
}


@Module
@InstallIn(SingletonComponent::class)
object ProvideAPI {
    @Provides
    @Singleton
    fun provideDownloadByLink(
        downloadRepository: DownloadServicesRepository,
        downloadQueueManager: DownloadQueueManager,
        downloadEventManager: DownloadEventManager,
        downloadPreChecks: DownloadPreChecks,
        fileUtils: FileUtils
    ): DownloadbyLink {
        return DownloadbyLink(downloadRepository, downloadQueueManager, downloadEventManager, downloadPreChecks, fileUtils)
    }

    @Provides
    @Singleton
    fun provideDownloadServicesRepository(
        twitterDownloadAPI: TwitterDownloadAPI,
        lofterService: LofterService,
        pixivService: PixivService,
        missEvanService: MissEvanService,
        kuaikanService: KuaikanService,
        downloadRepository: DownloadRepository
    ): DownloadServicesRepository {
        return DownloadServicesRepository(twitterDownloadAPI, lofterService, pixivService,missEvanService, kuaikanService, downloadRepository)
    }

    @Provides
    @Singleton
    fun provideTwitterSingleMedia(
        userDataManager: UserDataManager,
        notification: ShowNotification
    ): TwitterDownloadAPI {
        return TwitterDownloadAPI(userDataManager, notification)
    }

    @Provides
    @Singleton
    fun provideLofterService(
        userDataManager: UserDataManager,
        @ApplicationScope scope: CoroutineScope
    ): LofterService {
        return LofterService(userDataManager,scope)
    }

    @Provides
    @Singleton
    fun providePixivService(
        userDataManager: UserDataManager,
        @ApplicationScope scope: CoroutineScope
    ): PixivService {
        return PixivService(userDataManager,scope)
    }

    @Provides
    @Singleton
    fun provideMissEvanService(
        userDataManager: UserDataManager,
        @ApplicationScope scope: CoroutineScope
    ): MissEvanService {
        return MissEvanService(userDataManager,scope)
    }

    @Provides
    @Singleton
    fun provideKuaikanService(
        userDataManager: UserDataManager,
        @ApplicationScope scope: CoroutineScope
    ): KuaikanService {
        return KuaikanService(userDataManager,scope)
    }

    @Provides
    @Singleton
    fun provideFileUtils(
        @ApplicationContext context: Context
    ) = FileUtils(context)
}

