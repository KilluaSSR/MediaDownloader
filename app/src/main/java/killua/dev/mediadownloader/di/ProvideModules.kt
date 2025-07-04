package killua.dev.mediadownloader.di

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
import killua.dev.mediadownloader.api.Kuaikan.KuaikanService
import killua.dev.mediadownloader.api.Lofter.LofterService
import killua.dev.mediadownloader.api.MissEvan.MissEvanService
import killua.dev.mediadownloader.api.Pixiv.PixivService
import killua.dev.mediadownloader.api.Twitter.TwitterDownloadAPI
import killua.dev.mediadownloader.db.DownloadDao
import killua.dev.mediadownloader.db.LofterTagsRepository
import killua.dev.mediadownloader.db.MediaDownloadDatabase
import killua.dev.mediadownloader.db.TagDao
import killua.dev.mediadownloader.download.DownloadListManager
import killua.dev.mediadownloader.download.DownloadManager
import killua.dev.mediadownloader.download.DownloadQueueManager
import killua.dev.mediadownloader.download.DownloadbyLink
import killua.dev.mediadownloader.features.AdvancedFeaturesManager
import killua.dev.mediadownloader.repository.DownloadRepository
import killua.dev.mediadownloader.repository.DownloadServicesRepository
import killua.dev.mediadownloader.repository.ThumbnailRepository
import killua.dev.mediadownloader.utils.DownloadEventManager
import killua.dev.mediadownloader.utils.DownloadPreChecks
import killua.dev.mediadownloader.utils.FileDelete
import killua.dev.mediadownloader.utils.FileUtils
import killua.dev.mediadownloader.utils.MediaDurationRepositoryImpl
import killua.dev.mediadownloader.utils.MeidaDurationRepository
import killua.dev.mediadownloader.utils.ShowNotification
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ProvideModules {

    @Provides
    @Singleton
    fun provideContent(@ApplicationContext contexts: Context) = contexts

    @Provides
    @Singleton
    fun provideDelete(@ApplicationContext context: Context) =
        killua.dev.mediadownloader.utils.FileDelete(context)

    @Provides
    @Singleton
    fun provideDownloadEventManager(): killua.dev.mediadownloader.utils.DownloadEventManager =
        killua.dev.mediadownloader.utils.DownloadEventManager()

    @Provides
    @Singleton
    fun provideDownloadRepository(
        downloadDao: DownloadDao
    ): DownloadRepository = DownloadRepository(downloadDao)

    @Provides
    @Singleton
    fun provideTagsRepository(
        tagDao: TagDao
    ): LofterTagsRepository = LofterTagsRepository(tagDao)

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
    fun provideDownloadDatabase(@ApplicationContext context: Context): MediaDownloadDatabase {
        return MediaDownloadDatabase.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideDownloadDao(database: MediaDownloadDatabase): DownloadDao {
        return database.downloadDao()
    }

    @Provides
    @Singleton
    fun provideTagsDao(database: MediaDownloadDatabase): TagDao {
        return database.tagDao()
    }

    @Provides
    @Singleton
    fun provideNetworkManager(@ApplicationContext context: Context): killua.dev.mediadownloader.utils.NetworkManager =
        killua.dev.mediadownloader.utils.NetworkManager(context)

    @Provides
    @Singleton
    fun provideShowNotification(
        @ApplicationContext context: Context
    ) = killua.dev.mediadownloader.utils.ShowNotification(context)

    @Provides
    @Singleton
    fun provideVideoDurationRepository(
        @ApplicationContext context: Context
    ): MeidaDurationRepository = MediaDurationRepositoryImpl(context)

    @Provides
    @Singleton
    fun provideUserDataManager(
        @ApplicationContext context: Context,
        @ApplicationScope scope: CoroutineScope
    ): UserDataManager {
        return UserDataManager(context, scope)
    }

    @Provides
    @Singleton
    fun ProvideAdvancedFeaturesManager(
        twitterDownloadAPI: TwitterDownloadAPI,
        kuaikanService: KuaikanService,
        lofterService: LofterService,
        notification: ShowNotification,
        pixelService: PixivService,
        missEvanService: MissEvanService,
        downloadQueueManager: DownloadQueueManager,
        downloadRepository: DownloadRepository,
        tagsRepository: LofterTagsRepository,
        preChecks: DownloadPreChecks,
        fileUtils: FileUtils,
        @ApplicationContext context: Context
    ): AdvancedFeaturesManager = AdvancedFeaturesManager(
        twitterDownloadAPI,
        kuaikanService,
        lofterService,
        pixelService,
        missEvanService,
        notification,
        downloadQueueManager,
        downloadRepository,
        tagsRepository,
        preChecks,
        fileUtils,
        context
    )

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

    @Provides
    fun provideDownloadListManager(
        downloadRepository: DownloadRepository,
        downloadManager: DownloadManager,
        thumbnailRepository: ThumbnailRepository,
        videoDurationRepository: MeidaDurationRepository,
        downloadQueueManager: DownloadQueueManager,
        fileDelete: FileDelete,
        @ApplicationContext context: Context
    ): DownloadListManager = DownloadListManager(
        downloadRepository,
        downloadManager,
        thumbnailRepository,
        videoDurationRepository,
        downloadQueueManager,
        fileDelete,
        context
    )
}

