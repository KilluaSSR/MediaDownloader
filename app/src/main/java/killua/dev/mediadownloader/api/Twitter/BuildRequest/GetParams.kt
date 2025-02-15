package killua.dev.mediadownloader.api.Twitter.BuildRequest

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

fun GetTwitterBookmarkMediaParams(count: Int, cursor: String, userId: String, gson: Gson): Map<String, String>{
    val variables = mapOf(
        "userId" to userId,
        "count" to count,
        "includePromotedContent" to true,
        "withClientEventToken" to false,
        "withBirdwatchNotes" to false,
        "withVoice" to true,
        "withV2Timeline" to true,
        "cursor" to cursor
    )
    return mapOf(
        "variables" to gson.toJson(variables),
        "features" to GetBookmarkFeatures,
        "fieldToggles" to "{\"withArticlePlainText\":false}"
    )
}


fun GetLikeParams(count: Int, cursor: String, userId: String, gson: Gson): Map<String, String> {
    val variables = mapOf(
        "userId" to userId,
        "count" to count,
        "includePromotedContent" to false,
        "withClientEventToken" to false,
        "withBirdwatchNotes" to false,
        "withVoice" to true,
        "withV2Timeline" to true,
        "cursor" to cursor
    )

    return mapOf(
        "variables" to gson.toJson(variables),
        "features" to GetLikeFeatures,
        "fieldToggles" to "{\"withArticlePlainText\":false}"
    )
}

fun GetUserMediaParams(
    count: Int,
    cursor: String,
    userId: String,
    gson: Gson
) : Map<String, String> {
    val variables = mapOf(
        "userId" to userId,
        "count" to count,
        "includePromotedContent" to false,
        "withClientEventToken" to false,
        "withBirdwatchNotes" to false,
        "withVoice" to true,
        "withV2Timeline" to true,
        "cursor" to cursor
    )
    return mapOf(
        "variables" to gson.toJson(variables),
        "features" to GetUserMediaFeatures,
        "fieldToggles" to "{\"withArticlePlainText\":false}"
    )

}

fun GetUserProfileParams(
    screenName: String,
    gson: Gson
): Map<String, String> = mapOf(
    "variables" to gson.toJson(
        mapOf("screen_name" to screenName)
    )
)
