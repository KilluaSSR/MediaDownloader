package killua.dev.base.Model

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import killua.dev.base.R
import killua.dev.base.ui.tokens.SizeTokens

enum class AvailablePlatforms {
    Twitter,
    Lofter,
    Pixiv,
}

val patterns: Map<String, AvailablePlatforms> = mapOf(
    "x.com" to AvailablePlatforms.Twitter,
    "twitter.com" to AvailablePlatforms.Twitter,
    ".lofter.com/post/" to AvailablePlatforms.Lofter,
    "pixiv.net/artworks/" to AvailablePlatforms.Pixiv,
)

val platformsDrawable: Map<AvailablePlatforms,Int> = mapOf(
    AvailablePlatforms.Twitter to R.drawable.logo_of_twitter,
    AvailablePlatforms.Lofter to R.drawable.lofter_logo,
    AvailablePlatforms.Pixiv to R.drawable.pixiv_logo__2025_
)

enum class SupportedUrlType(val pattern: Regex) {
    TWITTER("""(?:twitter\.com|x\.com)/.*""".toRegex()),
    LOFTER("""lofter\.com/post/.*""".toRegex()),
    PIXIV("""pixiv\.net/artworks/.*""".toRegex()),
    UNKNOWN("".toRegex());

    companion object {
        fun fromUrl(url: String): SupportedUrlType {
            return entries.find { it.pattern.containsMatchIn(url) } ?: UNKNOWN
        }
    }
}

@Composable
fun AppIcon(platforms: AvailablePlatforms, modifier: Modifier = Modifier) {
    val icon = when(platforms){
        AvailablePlatforms.Twitter -> R.drawable.logo_of_twitter
        AvailablePlatforms.Lofter -> R.drawable.lofter_logo
        AvailablePlatforms.Pixiv -> R.drawable.pixiv_logo__2025_
    }
    val sizeTokens = when(platforms){
        AvailablePlatforms.Twitter -> SizeTokens.Level100
        AvailablePlatforms.Lofter -> SizeTokens.Level152
        AvailablePlatforms.Pixiv -> SizeTokens.Level152
    }
    Box(
        modifier = modifier
            .size(SizeTokens.Level152),
        contentAlignment = Alignment.Center
    ) {
        Image(
            modifier = Modifier.size(sizeTokens),
            imageVector = ImageVector.vectorResource(id = icon),
            contentDescription = null
        )
    }
}
