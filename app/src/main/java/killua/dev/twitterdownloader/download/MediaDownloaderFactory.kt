package killua.dev.twitterdownloader.download

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import killua.dev.base.Model.AvailablePlatforms
import killua.dev.base.utils.MediaStoreHelper
import killua.dev.twitterdownloader.download.Platforms.CommonMangaDownloader
import killua.dev.twitterdownloader.download.Platforms.LofterDownloader
import killua.dev.twitterdownloader.download.Platforms.PixivDownloader
import killua.dev.twitterdownloader.download.Platforms.TwitterMediaDownloader
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaDownloaderFactory @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val mediaHelper = MediaStoreHelper(context)

    fun create(platform: AvailablePlatforms): MediaDownloader = when (platform) {
        AvailablePlatforms.Twitter -> TwitterMediaDownloader(context, mediaHelper)
        AvailablePlatforms.Lofter -> LofterDownloader(context, mediaHelper)
        AvailablePlatforms.Pixiv -> PixivDownloader(context,mediaHelper)
        AvailablePlatforms.Kuaikan -> CommonMangaDownloader(context,mediaHelper)
    }
}