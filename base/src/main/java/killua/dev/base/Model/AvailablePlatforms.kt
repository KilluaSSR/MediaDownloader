package killua.dev.base.Model

import androidx.annotation.DrawableRes

enum class AvailablePlatforms {
    Twitter,
    Lofter,
}

val patterns: Map<String, AvailablePlatforms> = mapOf(
    "x.com" to AvailablePlatforms.Twitter,
    "twitter.com" to AvailablePlatforms.Twitter,
    ".lofter.com/post/" to AvailablePlatforms.Lofter,
)

val platformsDrawable: Map<AvailablePlatforms,Int> = mapOf(
    AvailablePlatforms.Twitter to killua.dev.base.R.drawable.logo_of_twitter,
    AvailablePlatforms.Lofter to killua.dev.base.R.drawable.lofter_logo
)