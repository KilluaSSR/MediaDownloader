package killua.dev.twitterdownloader.api.Twitter.Model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RootDto(
    @SerialName("data")
    val data: DataContent? = null
)

@Serializable
data class DataContent(
    @SerialName("user")
    val user: UserContent? = null,

    @SerialName("bookmark_timeline_v2")
    val bookmark_timeline_v2: TimelineV2Content? = null,

    @SerialName("user_result_by_screen_name")
    val user_result_by_screen_name: UserResults? = null,

    @SerialName("threaded_conversation_with_injections_v2")
    val threaded_conversation_with_injections_v2: TimelineContent? = null
)

@Serializable
data class UserContent(
    @SerialName("result")
    val result: UserResult? = null
)

@Serializable
data class UserResult(
    @SerialName("__typename")
    val __typename: String? = null,

    @SerialName("timeline_v2")
    val timeline_v2: TimelineV2Content? = null
)

@Serializable
data class TimelineV2Content(
    @SerialName("timeline")
    val timeline: TimelineContent? = null
)

@Serializable
data class TimelineContent(
    @SerialName("instructions")
    val instructions: List<Instruction>? = null,

    @SerialName("metadata")
    val metadata: TimelineMetadata? = null
)

@Serializable
data class TimelineMetadata(
    @SerialName("scribeConfig")
    val scribeConfig: ScribeConfig? = null
)

@Serializable
data class ScribeConfig(
    @SerialName("page")
    val page: String? = null
)

@Serializable
data class Instruction(
    @SerialName("type")
    val type: String? = null,

    @SerialName("direction")
    val direction: String? = null,

    @SerialName("entries")
    val entries: List<TimelineEntry>? = null,

    @SerialName("moduleItems")
    val moduleItems: List<ItemMedia>? = null
)

@Serializable
data class TimelineEntry(
    @SerialName("entryId")
    val entryId: String? = null,

    @SerialName("sortIndex")
    val sortIndex: String? = null,

    @SerialName("content")
    val content: EntryContent? = null
)

@Serializable
data class EntryContent(
    @SerialName("entryType")
    val entryType: String? = null,

    @SerialName("__typename")
    val __typename: String? = null,

    @SerialName("itemContent")
    val itemContent: ItemContent? = null,

    @SerialName("value")
    val value: String? = null,

    @SerialName("items")
    val items: List<ItemMedia>? = null,

    @SerialName("cursorType")
    val cursorType: String? = null
)

@Serializable
data class ItemMedia(
    @SerialName("entryId")
    val entryId: String? = null,

    @SerialName("item")
    val item: ItemDetail? = null
)

@Serializable
data class ItemDetail(
    @SerialName("itemContent")
    val itemContent: ItemContent? = null
)

@Serializable
data class ItemContent(
    @SerialName("itemType")
    val itemType: String? = null,

    @SerialName("__typename")
    val __typename: String? = null,

    @SerialName("tweet_results")
    val tweet_results: TweetResults? = null,

    @SerialName("tweetDisplayType")
    val tweetDisplayType: String? = null
)

@Serializable
data class TweetResults(
    @SerialName("result")
    val result: TweetResult? = null
)

@Serializable
data class TweetResult(
    @SerialName("__typename")
    val __typename: String? = null,

    @SerialName("rest_id")
    val rest_id: String? = null,

    @SerialName("core")
    val core: CoreInfo? = null,

    @SerialName("legacy")
    val legacy: LegacyInfo? = null,

    @SerialName("tweet")
    val tweet: TweetResult? = null,

    @SerialName("tombstone")
    val tombstone: Any? = null,

    @SerialName("views")
    val views: ViewInfo? = null
)

@Serializable
data class ViewInfo(
    @SerialName("count")
    val count: String? = null,

    @SerialName("state")
    val state: String? = null
)

@Serializable
data class CoreInfo(
    @SerialName("user_results")
    val user_results: UserResults? = null
)

@Serializable
data class UserResults(
    @SerialName("result")
    val result: UserResultInfo? = null
)

@Serializable
data class UserResultInfo(
    @SerialName("__typename")
    val __typename: String? = null,

    @SerialName("id")
    val id: String? = null,

    @SerialName("rest_id")
    val rest_id: String? = null,

    @SerialName("legacy")
    val legacy: UserLegacy? = null
)

@Serializable
data class UserLegacy(
    @SerialName("screen_name")
    val screen_name: String? = null,

    @SerialName("name")
    val name: String? = null,

    @SerialName("description")
    val description: String? = null
)

@Serializable
data class LegacyInfo(
    @SerialName("created_at")
    val created_at: String? = null,

    @SerialName("full_text")
    val full_text: String? = null,

    @SerialName("entities")
    val entities: Entities? = null,

    @SerialName("extended_entities")
    val extended_entities: ExtendedEntities? = null
)

@Serializable
data class Entities(
    @SerialName("hashtags")
    val hashtags: List<HashTag>? = null,

    @SerialName("media")
    val media: List<MediaEntity>? = null
)

@Serializable
data class ExtendedEntities(
    @SerialName("media")
    val media: List<MediaEntity>? = null
)

@Serializable
data class HashTag(
    @SerialName("text")
    val text: String? = null
)

@Serializable
data class MediaEntity(
    @SerialName("type")
    val type: String? = null,

    @SerialName("media_url_https")
    val media_url_https: String? = null,

    @SerialName("video_info")
    val video_info: VideoInfo? = null
)

@Serializable
data class VideoInfo(
    @SerialName("variants")
    val variants: List<VideoVariant>? = null
)

@Serializable
data class VideoVariant(
    @SerialName("bitrate")
    val bitrate: Long? = null,

    @SerialName("url")
    val url: String? = null
)
@Serializable
data class Tweet(
    @SerialName("id")
    val id: String? = null,

    val userId: String? = null,

    @SerialName("text")
    val text: String? = null,

    @SerialName("hashtags")
    val hashtags: List<String> = listOf(),

    @SerialName("created_at")
    val createdAt: String? = null,

    @SerialName("media")
    val media: List<Media> = listOf()
)
@Serializable
data class MetadataContainer(
    @SerialName("current_page")
    val currentPage: String? = null,

    @SerialName("users")
    val users: Map<String, UserData> = mapOf()
) {
    @Serializable
    data class UserData(
        @SerialName("user")
        val userHistory: List<TwitterUser> = listOf(),

        @SerialName("tweet")
        val tweets: List<Tweet> = listOf()
    )
}
@Serializable
data class TwitterUser(
    @SerialName("id")
    val id: String? = null,

    @SerialName("screen_name")
    val screenName: String? = null,

    @SerialName("name")
    val name: String? = null,

    @SerialName("created_time")
    val createdTime: Long = 0
)

@Serializable
data class Media(
    @SerialName("type")
    val type: String? = null,

    @SerialName("url")
    val url: String? = null,

    @SerialName("bitrate")
    val bitrate: Long? = null
)

@Serializable
data class Bookmark(
    val user: TwitterUser?,
    val tweetId: String,
    val photoUrls: List<String>,
    val videoUrls: List<String>,
)

@Serializable
data class MediaPageData(
    val items: List<Bookmark>,
    val nextPage: String
)

@Serializable
data class UserBasicInfo(
    val id: String?,
    val name: String?,
    val screenName: String?
)