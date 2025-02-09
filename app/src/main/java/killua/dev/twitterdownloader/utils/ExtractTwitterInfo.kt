package killua.dev.twitterdownloader.utils

import android.util.Log
import killua.dev.twitterdownloader.api.Twitter.Model.Bookmark
import killua.dev.twitterdownloader.api.Twitter.Model.BookmarkPageData
import killua.dev.twitterdownloader.api.Twitter.Model.BookmarksPageData
import killua.dev.twitterdownloader.api.Twitter.Model.RootDto
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

fun RootDto.extractLikePageData(cursor: String): BookmarksPageData {  // 返回 BookmarksPageData
    val instructions = data?.user?.result?.timeline_v2?.timeline?.instructions
        ?.firstOrNull { it.type == "TimelineAddEntries" }
        ?.entries ?: emptyList()

    val nextPage = instructions
        .lastOrNull { it.entryId?.startsWith("cursor-bottom-") == true }
        ?.content?.value ?: cursor

    val bookmarks = instructions
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

    return BookmarksPageData(
        bookmark = bookmarks,
        nextPage = nextPage
    )
}


fun RootDto.extractBookmarkPageData(cursor: String): BookmarksPageData {
    // 获取所有书签条目
    val instructions = data?.bookmark_timeline_v2?.timeline?.instructions
        ?.firstOrNull { it.type == "TimelineAddEntries" }
        ?.entries ?: emptyList()
    Log.d("TwitterAPI", "Found ${instructions.size} entries")
    // 获取下一页游标
    val nextPage = instructions
        .lastOrNull { it.entryId?.startsWith("cursor-bottom-") == true }
        ?.content?.value ?: cursor
    println(cursor)
    println(nextPage)
    // 处理每条推文
    val bookmarks = instructions
        .filter { it.entryId?.startsWith("tweet-") == true }
        .mapNotNull { entry ->
            entry.content?.itemContent?.tweet_results?.result?.let { result ->
                // 提取用户信息
                val user = TwitterUser(
                    id = result.core?.user_results?.result?.rest_id,
                    screenName = result.core?.user_results?.result?.legacy?.screen_name,
                    name = result.core?.user_results?.result?.legacy?.name
                )

                // 从 extended_entities 中提取媒体信息
                val mediaList = result.legacy?.extended_entities?.media ?: emptyList()

                // 处理图片
                val photoUrls = mediaList
                    .filter { it.type == "photo" }
                    .mapNotNull { it.media_url_https }

                // 处理视频（选择最高码率）
                val videoUrls = mediaList
                    .filter { it.type == "video" }
                    .mapNotNull { media ->
                        media.video_info?.variants
                            ?.filter { it.bitrate != null }
                            ?.maxByOrNull { it.bitrate!! }
                            ?.url
                    }

                // 只返回包含媒体的书签
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

    return BookmarksPageData(
        bookmark = bookmarks,
        nextPage = nextPage
    )
}