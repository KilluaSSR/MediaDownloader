package killua.dev.twitterdownloader.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import killua.dev.base.utils.ShowNotification
import killua.dev.twitterdownloader.api.Kuaikan.KuaikanService
import killua.dev.twitterdownloader.api.Lofter.LofterService
import killua.dev.twitterdownloader.api.Twitter.TwitterDownloadAPI
import killua.dev.twitterdownloader.db.LofterTagsRepository
import killua.dev.twitterdownloader.download.DownloadQueueManager
import killua.dev.twitterdownloader.features.AdvancedFeaturesManager
import killua.dev.twitterdownloader.repository.DownloadRepository
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
        downloadQueueManager: DownloadQueueManager,
        downloadRepository: DownloadRepository,
        tagsRepository: LofterTagsRepository,
        @ApplicationContext context: Context
    ): AdvancedFeaturesManager = AdvancedFeaturesManager(
        twitterDownloadAPI,
        kuaikanService,
        lofterService,
        notification,
        downloadQueueManager,
        downloadRepository,
        tagsRepository,
        context
    )

}

