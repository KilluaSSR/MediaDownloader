package killua.dev.mediadownloader.Model

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import killua.dev.mediadownloader.R
import killua.dev.mediadownloader.ui.tokens.SizeTokens

enum class AvailablePlatforms {
    Twitter,
    Lofter,
    Pixiv,
    Kuaikan,
    MissEvan
}

val platformsDrawable: Map<AvailablePlatforms,Int> = mapOf(
    AvailablePlatforms.Twitter to R.drawable.logo_of_twitter,
    AvailablePlatforms.Lofter to R.drawable.lofter_logo,
    AvailablePlatforms.Pixiv to R.drawable.pixiv_logo__2025_,
    AvailablePlatforms.Kuaikan to R.drawable.kuaikanmanhua,
    AvailablePlatforms.MissEvan to R.drawable.missevan_logo
)

val platformName: Map<AvailablePlatforms, Int> = mapOf(
    AvailablePlatforms.Twitter to R.string.twitter,
    AvailablePlatforms.Lofter to R.string.lofter,
    AvailablePlatforms.Pixiv to R.string.pixiv,
    AvailablePlatforms.Kuaikan to R.string.kuaikan,
    AvailablePlatforms.MissEvan to R.string.missevan
)

enum class SupportedUrlType(val pattern: Regex) {
    TWITTER("""(?:twitter\.com|x\.com)/.*""".toRegex()),
    LOFTER("""lofter\.com/post/.*""".toRegex()),
    PIXIV_IMG("""pixiv\.net/artworks/.*""".toRegex()),
    PIXIV_NOVEL("""pixiv\.net/novel/show\.php\?id=\d+""".toRegex()),
    KUAIKAN("""kuaikanmanhua\.com/.*""".toRegex()),
    MISSEVAN("""missevan\.com/sound/player\?id=(\d+)""".toRegex()),
    UNKNOWN("".toRegex());

    companion object {
        fun fromUrl(url: String): SupportedUrlType {
            return entries.find { it.pattern.containsMatchIn(url) } ?: UNKNOWN
        }

        fun toPlatform(supportedUrlType: SupportedUrlType) = when(supportedUrlType){
            TWITTER -> AvailablePlatforms.Twitter
            LOFTER -> AvailablePlatforms.Lofter
            PIXIV_IMG -> AvailablePlatforms.Pixiv
            PIXIV_NOVEL -> AvailablePlatforms.Pixiv
            KUAIKAN -> AvailablePlatforms.Kuaikan
            UNKNOWN -> null
            MISSEVAN -> AvailablePlatforms.MissEvan
        }
    }
}

@Composable
fun AppIcon(platforms: AvailablePlatforms, modifier: Modifier = Modifier) {
    val icon = platformsDrawable[platforms]
    val sizeTokens = when(platforms){
        AvailablePlatforms.Twitter -> SizeTokens.Level64
        AvailablePlatforms.Lofter -> SizeTokens.Level152
        AvailablePlatforms.Pixiv -> SizeTokens.Level152
        AvailablePlatforms.Kuaikan -> SizeTokens.Level152
        else -> SizeTokens.Level152
    }
    Box(
        modifier = modifier
            .size(SizeTokens.Level152),
        contentAlignment = Alignment.Center
    ) {
        Image(
            modifier = Modifier.size(sizeTokens),
            imageVector = ImageVector.vectorResource(id = icon!!),
            contentDescription = null,
        )
    }
}
