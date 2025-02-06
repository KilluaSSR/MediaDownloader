package killua.dev.twitterdownloader.utils

import killua.dev.twitterdownloader.api.Twitter.Model.RootDto

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

fun RootDto.getAllImageUrls(): List<String>{
    val tweetData = this.data?.threaded_conversation_with_injections_v2?.instructions?.firstOrNull()
        ?.entries?.firstOrNull()?.content?.itemContent?.tweet_results?.result?.let { result ->
            result.tweet ?: result
        }
    val mediaList = tweetData
        ?.legacy
        ?.entities
        ?.media
    return mediaList
        ?.filter { it.type == "photo" }
        ?.mapNotNull { it.media_url_https }
        ?: emptyList()
}