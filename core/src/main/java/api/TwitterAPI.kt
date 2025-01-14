package api

import api.Constants.GetTweetDetailFeatures
import api.Constants.TwitterAPIURL
import api.Model.GraphQLResponse
import api.Model.Media
import api.Model.MediaEntity
import api.Model.TimelineEntry
import api.Model.Tweet
import api.Model.TweetDetailVariables
import api.Model.VideoInfo
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder
import java.nio.charset.StandardCharsets


class TwitterAPI(
    private val okHttpClient: OkHttpClient
){
    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
    }
    suspend fun getTweetDetail(tweetId: String): List<Tweet>? {
        val variables = TweetDetailVariables(tweetId)
        val parameters = mapOf(
            "variables" to json.encodeToString<TweetDetailVariables>(TweetDetailVariables.serializer(),variables),
            "features" to GetTweetDetailFeatures,
            "fieldToggles" to """{"withArticleRichContentState":true,"withArticlePlainText":false,"withGrokAnalyze":false,"withDisallowedReplyControls":false}"""
        )
        return try {
            val response = sendRequest<GraphQLResponse>(
                baseUrl = "TWEET_DETAIL_URL",
                parameters = parameters
            ) ?: return null

            response.data.threadedConversationV2?.instructions
                ?.asSequence()
                ?.filter { it.type == "TimelineAddEntries" }
                ?.flatMap { instruction ->
                    instruction.entries
                        ?.asSequence()
                        ?.filter { it.entryId.startsWith("tweet-") }
                        ?.mapNotNull { entry ->
                            processTweet(entry)
                        }
                        ?: emptySequence()
                }
                ?.toList()
        } catch (ex: Exception) {
            println("${ex.message}")
            null
        }

    }
    private fun processTweet(entry: TimelineEntry): Tweet? {
        return try {
            val tweetResult = entry.content.itemContent?.tweetResults?.result
                ?: return null

            if (tweetResult.tombstone != null) return null

            val tweetData = tweetResult.tweet ?: tweetResult
            val userInfo = tweetData.core?.userResults?.result
                ?: return null
            val userId = userInfo.restId

            Tweet(
                id = tweetData.restId,
                userId = userId,
                text = tweetData.legacy?.fullText.orEmpty(),
                hashtags = tweetData.legacy?.entities?.hashtags
                    ?.map { it.text }
                    ?: emptyList(),
                createdAt = tweetData.legacy?.createdAt,
                media = processMedia(tweetData.legacy?.entities?.media)
            )
        } catch (ex: Exception) {
            println("${ex.message}")
            null
        }
    }

    private fun processMedia(mediaEntities: List<MediaEntity>?): List<Media> {
        if (mediaEntities == null) return emptyList()

        return mediaEntities.map { m ->
            Media(
                type = m.type,
                url = when (m.type) {
                    "photo" -> getOriginalImageUrl(m.mediaUrlHttps)
                    else -> getHighestQualityVideoUrl(m.videoInfo)
                },
                bitrate = if (m.type == "video") getHighestBitrate(m.videoInfo) else null
            )
        }
    }

    private fun getOriginalImageUrl(url: String): String {
        val parts = url.split(".")
        val ext = parts.last()
        val basePath = parts.dropLast(1).joinToString(".")
        return "$basePath?format=$ext&name=orig"
    }

    private fun getHighestQualityVideoUrl(videoInfo: VideoInfo?): String {
        return videoInfo?.variants
            ?.filter { it.bitrate != null }
            ?.maxByOrNull { it.bitrate ?: 0L }
            ?.url
            ?: ""
    }

    private fun getHighestBitrate(videoInfo: VideoInfo?): Long? {
        return videoInfo?.variants
            ?.mapNotNull { it.bitrate }
            ?.maxOrNull()
    }

    private fun buildUrl(baseUrl: String, parameters: Map<String, String>?): String {
        if (parameters.isNullOrEmpty()) return baseUrl

        val queryString = parameters.map { (key, value) ->
            "${URLEncoder.encode(key, StandardCharsets.UTF_8.name())}=" +
                    "${URLEncoder.encode(value, StandardCharsets.UTF_8.name())}"
        }.joinToString("&")

        return "$baseUrl?$queryString"
    }

    private suspend inline fun <reified T> sendRequest(baseUrl: String, parameters: Map<String, String>): T? {
        val url = buildUrl(baseUrl, parameters)
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer ${TwitterAPIURL.Bearer}")
            .addHeader("x-csrf-token", "ct0")
            .addHeader("x-twitter-active-user", "yes")
            .addHeader("x-twitter-auth-type", "OAuth2Session")
            .addHeader("x-twitter-client-language", "en")
            .build()

        return okHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return null

            val responseBody = response.body?.string() ?: return null
            json.decodeFromString<T>(responseBody)
        }
    }
}