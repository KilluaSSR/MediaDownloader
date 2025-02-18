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
    Kuaikan
}

val patterns: Map<String, AvailablePlatforms> = mapOf(
    "x.com" to AvailablePlatforms.Twitter,
    "twitter.com" to AvailablePlatforms.Twitter,
    ".lofter.com/post/" to AvailablePlatforms.Lofter,
    "pixiv.net/artworks/" to AvailablePlatforms.Pixiv,
    "pixiv.net/novel/show.php?" to AvailablePlatforms.Pixiv,
    "kuaikanmanhua.com" to AvailablePlatforms.Kuaikan
)

val platformsDrawable: Map<AvailablePlatforms,Int> = mapOf(
    AvailablePlatforms.Twitter to R.drawable.logo_of_twitter,
    AvailablePlatforms.Lofter to R.drawable.lofter_logo,
    AvailablePlatforms.Pixiv to R.drawable.pixiv_logo__2025_,
    AvailablePlatforms.Kuaikan to R.drawable.kuaikanmanhua
)

val platformName: Map<AvailablePlatforms, Int> = mapOf(
    AvailablePlatforms.Twitter to R.string.twitter,
    AvailablePlatforms.Lofter to R.string.lofter,
    AvailablePlatforms.Pixiv to R.string.pixiv,
    AvailablePlatforms.Kuaikan to R.string.kuaikan
)


enum class SupportedUrlType(val pattern: Regex) {
    TWITTER("""(?:twitter\.com|x\.com)/.*""".toRegex()),
    LOFTER("""lofter\.com/post/.*""".toRegex()),
    PIXIV_IMG("""pixiv\.net/artworks/.*""".toRegex()),
    PIXIV_NOVEL("""pixiv\.net/novel/show\.php\?id=\d+""".toRegex()),
    KUAIKAN("""kuaikanmanhua\.com/.*""".toRegex()),
    UNKNOWN("".toRegex());

    companion object {
        fun fromUrl(url: String): SupportedUrlType {
            return entries.find { it.pattern.containsMatchIn(url) } ?: UNKNOWN
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
