package api.Model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Media(
    @SerialName("type")
    val type: String? = null,

    @SerialName("url")
    val url: String? = null,

    @SerialName("bitrate")
    val bitrate: Long? = null
)