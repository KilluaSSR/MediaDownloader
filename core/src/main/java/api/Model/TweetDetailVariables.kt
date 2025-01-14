package api.Model

import kotlinx.serialization.Serializable

@Serializable
data class TweetDetailVariables(
    val focalTweetId: String,
    val with_rux_injections: Boolean = false,
    val rankingMode: String = "Relevance",
    val includePromotedContent: Boolean = true,
    val withCommunity: Boolean = true,
    val withQuickPromoteEligibilityTweetFields: Boolean = true,
    val withBirdwatchNotes: Boolean = true,
    val withVoice: Boolean = true
)