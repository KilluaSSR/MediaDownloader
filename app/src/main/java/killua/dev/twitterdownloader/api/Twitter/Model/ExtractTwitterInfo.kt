package killua.dev.twitterdownloader.api.Twitter.Model

import android.util.Log


fun RootDto.extractTwitterUser(): TwitterUser {
    val tweetData = this.data?.threaded_conversation_with_injections_v2?.instructions?.firstOrNull()
        ?.entries?.firstOrNull()?.content?.itemContent?.tweet_results?.result?.let { result ->
            result.tweet ?: result
        }
    val user = tweetData
        ?.core
        ?.user_results
        ?.result

    return TwitterUser(
        id = user?.rest_id,
        screenName = user?.legacy?.screen_name,
        name = user?.legacy?.name,
        createdTime = 0L
    )
}
fun RootDto.extractUserMediaPageData(cursor: String): MediaPageData {
    val instructions = data?.user?.result?.timeline_v2?.timeline?.instructions
        ?: return MediaPageData(emptyList(), cursor)

    val tweets = mutableListOf<Bookmark>()
    var nextCursor = cursor

    instructions.forEach { instruction ->
        when (instruction.type) {
            "TimelineAddEntries" -> {
                instruction.entries?.forEach { entry ->
                    when {
                        entry.entryId?.startsWith("profile-grid-") == true -> {
                            // 处理 TimelineTimelineModule 类型的内容
                            entry.content?.items?.forEach { item ->
                                val tweet = processUserMediaTweet(item?.item?.itemContent?.tweet_results?.result)
                                if (tweet != null) {
                                    tweets.add(tweet)
                                }
                            }
                        }
                        entry.entryId?.startsWith("cursor-bottom-") == true -> {
                            nextCursor = entry.content?.value ?: cursor
                        }
                    }
                }
            }
            "TimelineAddToModule" -> {
                instruction.moduleItems?.forEach { item ->
                    if (item.entryId?.startsWith("profile-grid-") == true) {
                        val tweet = processUserMediaTweet(item.item?.itemContent?.tweet_results?.result)
                        if (tweet != null) {
                            tweets.add(tweet)
                        }
                    }
                }
            }
        }
    }

    return MediaPageData(
        items = tweets,
        nextPage = nextCursor
    )
}

private fun processUserMediaTweet(tweetResult: TweetResult?): Bookmark? {
    if (tweetResult == null) return null

    try {
        val userResult = tweetResult.core?.user_results?.result
        val userId = userResult?.rest_id ?: return null

        // 创建用户信息
        val user = TwitterUser(
            id = userId,
            screenName = userResult.legacy?.screen_name,
            name = userResult.legacy?.name
        )

        // 获取媒体信息
        val mediaList = tweetResult.legacy?.extended_entities?.media.orEmpty()

        // 处理图片和视频
        val photoUrls = mediaList
            .filter { it.type == "photo" }
            .mapNotNull { it.media_url_https }

        val videoUrls = mediaList
            .filter { it.type == "video" || it.type == "animated_gif" }
            .mapNotNull { media ->
                media.video_info?.variants
                    ?.filter { it.content_type == "video/mp4" && it.bitrate != null }
                    ?.maxByOrNull { it.bitrate ?: 0 }
                    ?.url
            }

        // 只有包含媒体的推文才返回
        return if (photoUrls.isNotEmpty() || videoUrls.isNotEmpty()) {
            Bookmark(
                tweetId = tweetResult.rest_id ?: return null,
                user = user,
                photoUrls = photoUrls,
                videoUrls = videoUrls
            )
        } else null
    } catch (e: Exception) {
        Log.e("ProcessTweet", "Error processing tweet: ${e.message}")
        return null
    }
}

fun RootDto.extractMediaPageData(cursor: String, isBookmark: Boolean): MediaPageData {
    val instructions = if (isBookmark) {
        data?.bookmark_timeline_v2?.timeline?.instructions
    } else {
        data?.user?.result?.timeline_v2?.timeline?.instructions
    }?.firstOrNull { it.type == "TimelineAddEntries" }
        ?.entries ?: emptyList()

    Log.d("TwitterAPI", "Found ${instructions.size} entries")

    val nextPage = instructions
        .lastOrNull { it.entryId?.startsWith("cursor-bottom-") == true }
        ?.content?.value ?: cursor

    val items = instructions
        .filter { it.entryId?.startsWith("tweet-") == true }
        .mapNotNull { entry ->
            entry.content?.itemContent?.tweet_results?.result?.let { result ->
                val user = TwitterUser(
                    id = result.core?.user_results?.result?.rest_id,
                    screenName = result.core?.user_results?.result?.legacy?.screen_name,
                    name = result.core?.user_results?.result?.legacy?.name
                )

                val mediaList = result.legacy?.extended_entities?.media ?: emptyList()

                val photoUrls = mediaList
                    .filter { it.type == "photo" }
                    .mapNotNull { it.media_url_https }

                val videoUrls = mediaList
                    .filter { it.type == "video" }
                    .mapNotNull { media ->
                        media.video_info?.variants
                            ?.filter { it.bitrate != null }
                            ?.maxByOrNull { it.bitrate!! }
                            ?.url
                    }

                if (photoUrls.isNotEmpty() || videoUrls.isNotEmpty()) {
                    Bookmark(
                        tweetId = result.rest_id ?: "",
                        user = user,
                        photoUrls = photoUrls,
                        videoUrls = videoUrls
                    )
                } else null
            }
        }

    return MediaPageData(
        items = items,
        nextPage = nextPage
    )
}

