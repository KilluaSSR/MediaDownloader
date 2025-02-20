package killua.dev.mediadownloader.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import killua.dev.mediadownloader.api.Kuaikan.KuaikanService
import killua.dev.mediadownloader.api.Lofter.LofterService
import killua.dev.mediadownloader.api.Pixiv.PixivService
import killua.dev.mediadownloader.api.Twitter.TwitterDownloadAPI
import killua.dev.mediadownloader.db.LofterTagsRepository
import killua.dev.mediadownloader.download.DownloadQueueManager
import killua.dev.mediadownloader.features.AdvancedFeaturesManager
import killua.dev.mediadownloader.repository.DownloadRepository
import killua.dev.mediadownloader.utils.DownloadPreChecks
import killua.dev.mediadownloader.utils.FileUtils
import killua.dev.mediadownloader.utils.ShowNotification
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ProvideAdvancedFeaturesManagerModule {
    @Provides
    @Singleton
    fun ProvideAdvancedFeaturesManager(
        twitterDownloadAPI: TwitterDownloadAPI,
        kuaikanService: KuaikanService,
        lofterService: LofterService,
        notification: ShowNotification,
        pixelService: PixivService,
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
        notification,
        downloadQueueManager,
        downloadRepository,
        tagsRepository,
        preChecks,
        fileUtils,
        context
    )

}