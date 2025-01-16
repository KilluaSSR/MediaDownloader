package killua.dev.core.utils

import api.Model.GraphQLResponse
import api.Model.LegacyInfo


fun LegacyInfo.getHighestBitrateUrls(): List<String> {
    val medias = extendedEntities?.media ?: entities.media.orEmpty()
    return medias
        .filter { it.type == "video" && it.videoInfo != null }
        .mapNotNull { mediaEntity ->
            // 对每个视频分别获取最高码率的URL
            mediaEntity.videoInfo?.variants
                ?.filter { it.url.isNotBlank() }
                ?.maxByOrNull { it.bitrate ?: -1L }
                ?.url
        }
}

fun GraphQLResponse.getAllHighestBitrateUrls(): List<String> {
    val results = mutableListOf<String>()
    val instructions = data.threadedConversationV2?.instructions ?: return results
    for (instruction in instructions) {
        val entries = instruction.entries ?: continue
        for (entry in entries) {
            val itemContent = entry.content.itemContent ?: continue
            val tweetResult = itemContent.tweetResults?.result ?: continue
            val actualTweet = when(tweetResult.typeName) {
                "TweetWithVisibilityResults" -> tweetResult.tweet
                else -> tweetResult
            }
            actualTweet?.legacy?.getHighestBitrateUrls()?.let { urls ->
                results.addAll(urls)
            }
        }
    }
    return results
}