package killua.dev.mediadownloader.api.MissEvan.Model

import kotlinx.serialization.Serializable

@Serializable
data class MissEvanDramaResult(
    val title: String,
    val author: String,
    val dramaList: List<MissEvanEpisodes>
)
@Serializable
data class MissEvanDownloadDrama(
    val title: String,
    val id: String,
    val mainTitle: String
)

@Serializable
data class MissEvanEpisodes(
    val sound_id: String,
    val name: String,
)

@Serializable
data class MissEvanEntireDramaResponse(
    val info: MissEvanEntireDramaInfoResponse
)

@Serializable
data class MissEvanEntireDramaInfoResponse(
    val drama: MissEvanDramaResponse,
    val episodes: MissEvanEpisodesResponse,
)

@Serializable
data class MissEvanDramaResponse(
    val name: String,
    val author: String
)

@Serializable
data class MissEvanEpisodesResponse(
    val episode: List<MissEvanEpisodes>
)

@Serializable
data class MissEvanDramaSoundResponse(
    val info: MissEvanSoundInfoResponse
)

@Serializable
data class MissEvanSoundInfoResponse(
    val sound: MissEvanSoundResponse
)

@Serializable
data class MissEvanSoundResponse(
    val id: String,
    val soundstr: String,
    val soundurl: String
)