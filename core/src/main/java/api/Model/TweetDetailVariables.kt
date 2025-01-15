package api.Model

import kotlinx.serialization.Serializable

@Serializable
data class TweetDetailVariables(
    val focalTweetId: String,
    val with_rux_injections: String,
    val rankingMode: String,
    val includePromotedContent: String,
    val withCommunity: String,
    val withQuickPromoteEligibilityTweetFields: String,
    val withBirdwatchNotes: String,
    val withVoice: String,
)