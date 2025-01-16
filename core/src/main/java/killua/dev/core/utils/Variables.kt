package killua.dev.core.utils


fun String.toTweetVariablesSingleMedia(): Map<String, Any> {
    return mapOf(
        "focalTweetId" to this,
        "with_rux_injections" to false,
        "rankingMode" to "Relevance",
        "includePromotedContent" to true,
        "withCommunity" to true,
        "withQuickPromoteEligibilityTweetFields" to true,
        "withBirdwatchNotes" to true,
        "withVoice" to true
    )
}