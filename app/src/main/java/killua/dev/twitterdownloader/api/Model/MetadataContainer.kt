package killua.dev.twitterdownloader.api.Model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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