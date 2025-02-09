package killua.dev.twitterdownloader.utils

import android.util.Log
import killua.dev.twitterdownloader.api.Twitter.Model.Bookmark
import killua.dev.twitterdownloader.api.Twitter.Model.BookmarksPageData
import killua.dev.twitterdownloader.api.Twitter.Model.LikesPageData
import killua.dev.twitterdownloader.api.Twitter.Model.Media
import killua.dev.twitterdownloader.api.Twitter.Model.MediaPageData
import killua.dev.twitterdownloader.api.Twitter.Model.RootDto
import killua.dev.twitterdownloader.api.Twitter.Model.Tweet
import killua.dev.twitterdownloader.api.Twitter.Model.TwitterUser


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

