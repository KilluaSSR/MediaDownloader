package killua.dev.mediadownloader.utils

import killua.dev.mediadownloader.Model.AvailablePlatforms
import killua.dev.mediadownloader.Model.patterns
import kotlin.collections.component1
import kotlin.collections.component2

fun classifyLinks(urlLink: String): AvailablePlatforms {
    return patterns.entries.firstOrNull { (pattern, _) ->
        urlLink.contains(pattern, ignoreCase = true)
    }?.value ?: throw IllegalArgumentException("Unsupported URL")
}