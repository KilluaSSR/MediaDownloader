package killua.dev.twitterdownloader.api.Twitter.BuildRequest

import com.google.gson.Gson

fun GetTwitterDownloadSpecificMediaParams(tweetId: String, gson: Gson): Map<String, String>{
    val variables = mapOf(
        "focalTweetId" to tweetId,
        "with_rux_injections" to false,
        "rankingMode" to "Relevance",
        "includePromotedContent" to true,
        "withCommunity" to true,
        "withQuickPromoteEligibilityTweetFields" to true,
        "withBirdwatchNotes" to true,
        "withVoice" to true
    )
    return mapOf(
        "variables" to gson.toJson(variables),
        "features" to GetTweetDetailFeatures,
        "fieldToggles" to "{\"withArticleRichContentState\":true,\"withArticlePlainText\":false,\"withGrokAnalyze\":false,\"withDisallowedReplyControls\":false}"
    )
}