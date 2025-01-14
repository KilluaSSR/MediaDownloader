package api.Model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TwitterUser(
    @SerialName("id")
    val id: String? = null,

    @SerialName("screen_name")
    val screenName: String? = null,

    @SerialName("name")
    val name: String? = null,

    @SerialName("description")
    val description: String? = null,

    @SerialName("created_time")
    val createdTime: Long = 0
)