package killua.dev.twitterdownloader.utils

import api.Model.RootDto

fun RootDto.getAllHighestBitrateUrls(): List<String> {
    val tweetData = this.data?.threaded_conversation_with_injections_v2?.instructions?.firstOrNull()
        ?.entries?.firstOrNull()?.content?.itemContent?.tweet_results?.result?.let { result ->
            result.tweet ?: result
        }
    val mediaList = tweetData
        ?.legacy
        ?.entities
        ?.media
    return mediaList?.mapNotNull { media ->
        media.video_info?.variants
            ?.maxByOrNull { it.bitrate ?: 0L }
            ?.url
    } ?: emptyList()
}