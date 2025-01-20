package api.Model

import kotlinx.serialization.Contextual
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object AnySerializer : KSerializer<Any> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Any")

    override fun serialize(encoder: Encoder, value: Any) {
        // 简单实现，可根据需要扩展
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): Any {
        return decoder.decodeString()
    }
}

@Serializable
data class GraphQLResponse(
    @SerialName("data")
    val data: DataContent
)

@Serializable
data class DataContent(
    @SerialName("user")
    val user: UserContent? = null,
    @SerialName("bookmark_timeline_v2")
    val bookmarkTimelineV2: TimelineV2Content? = null,
    @SerialName("user_result_by_screen_name")
    val userResultByScreenName: UserResults? = null,
    @SerialName("threaded_conversation_with_injections_v2")
    val threadedConversationV2: TimelineContent? = null
)

@Serializable
data class UserContent(
    @SerialName("result")
    val result: UserResult
)

@Serializable
data class UserResult(
    @SerialName("__typename")
    val typeName: String,
    @SerialName("timeline_v2")
    val timelineV2: TimelineV2Content
)

@Serializable
data class TimelineV2Content(
    @SerialName("timeline")
    val timeline: TimelineContent
)

@Serializable
data class TimelineContent(
    @SerialName("instructions")
    val instructions: List<Instruction>,
    @SerialName("metadata")
    val metadata: TimelineMetadata? = null
)

@Serializable
data class TimelineMetadata(
    @SerialName("scribeConfig")
    val scribeConfig: ScribeConfig
)

@Serializable
data class ScribeConfig(
    @SerialName("page")
    val page: String
)

@Serializable
data class Instruction(
    @SerialName("type")
    val type: String,
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
    val entryId: String,
    @SerialName("sortIndex")
    val sortIndex: String,
    @SerialName("content")
    val content: EntryContent
)

@Serializable
data class EntryContent(
    @SerialName("entryType")
    val entryType: String,
    @SerialName("__typename")
    val typeName: String,
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
    val entryId: String,
    @SerialName("item")
    val item: ItemDetail
)

@Serializable
data class ItemDetail(
    @SerialName("itemContent")
    val itemContent: ItemContent
)

@Serializable
data class ItemContent(
    @SerialName("itemType")
    val itemType: String,
    @SerialName("__typename")
    val typeName: String,
    @SerialName("tweet_results")
    val tweetResults: TweetResults? = null,
    @SerialName("tweetDisplayType")
    val tweetDisplayType: String? = null
)

@Serializable
data class TweetResults(
    @SerialName("result")
    val result: TweetResult
)

@Serializable
data class TweetResult(
    @SerialName("__typename")
    val typeName: String,
    @SerialName("rest_id")
    val restId: String,
    @SerialName("core")
    val core: CoreInfo? = null,
    @SerialName("legacy")
    val legacy: LegacyInfo? = null,
    @SerialName("tweet")
    val tweet: TweetResult? = null,
    @SerialName("tombstone")
    @Contextual
    val tombstone: Any? = null,
    @SerialName("views")
    val views: ViewInfo? = null
)

@Serializable
data class ViewInfo(
    @SerialName("count")
    val count: String,
    @SerialName("state")
    val state: String
)

@Serializable
data class CoreInfo(
    @SerialName("user_results")
    val userResults: UserResults
)

@Serializable
data class UserResults(
    @SerialName("result")
    val result: UserResultInfo
)

@Serializable
data class UserResultInfo(
    @SerialName("__typename")
    val typeName: String,
    @SerialName("id")
    val id: String,
    @SerialName("rest_id")
    val restId: String,
    @SerialName("legacy")
    val legacy: UserLegacy
)

@Serializable
data class UserLegacy(
    @SerialName("screen_name")
    val screenName: String,
    @SerialName("name")
    val name: String,
    @SerialName("description")
    val description: String
)

@Serializable
data class LegacyInfo(
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("full_text")
    val fullText: String,
    @SerialName("entities")
    val entities: Entities,
    @SerialName("extended_entities")
    val extendedEntities: ExtendedEntities? = null
)

@Serializable
data class Entities(
    @SerialName("hashtags")
    val hashtags: List<HashTag>,
    @SerialName("media")
    val media: List<MediaEntity>? = null
)

@Serializable
data class ExtendedEntities(
    @SerialName("media")
    val media: List<MediaEntity>
)

@Serializable
data class HashTag(
    @SerialName("text")
    val text: String
)

@Serializable
data class MediaEntity(
    @SerialName("type")
    val type: String,
    @SerialName("media_url_https")
    val mediaUrlHttps: String,
    @SerialName("video_info")
    val videoInfo: VideoInfo? = null
)

@Serializable
data class VideoInfo(
    @SerialName("variants")
    val variants: List<VideoVariant>
)

@Serializable
data class VideoVariant(
    @SerialName("bitrate")
    val bitrate: Long? = null,
    @SerialName("url")
    val url: String
)

private fun LegacyInfo.filter(function: () -> kotlin.Boolean) {}
