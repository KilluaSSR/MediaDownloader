package killua.dev.twitterdownloader.api.Model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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