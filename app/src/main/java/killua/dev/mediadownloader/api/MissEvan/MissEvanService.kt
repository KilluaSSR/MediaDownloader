package killua.dev.mediadownloader.api.MissEvan

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import killua.dev.mediadownloader.Model.NetworkResult
import killua.dev.mediadownloader.api.MissEvan.BuildRequest.MissEvanGetDramaListRequest
import killua.dev.mediadownloader.api.MissEvan.BuildRequest.MissEvanGetSoundRequest
import killua.dev.mediadownloader.api.MissEvan.BuildRequest.addMissEvanDramaFetchHeaders
import killua.dev.mediadownloader.api.MissEvan.BuildRequest.addMissEvanSoundFetchHeaders
import killua.dev.mediadownloader.api.MissEvan.Model.MissEvanDramaResult
import killua.dev.mediadownloader.api.MissEvan.Model.MissEvanDramaSoundResponse
import killua.dev.mediadownloader.api.MissEvan.Model.MissEvanEntireDramaResponse
import killua.dev.mediadownloader.api.MissEvan.Model.MissEvanSoundResponse
import killua.dev.mediadownloader.api.NetworkHelper
import killua.dev.mediadownloader.di.ApplicationScope
import killua.dev.mediadownloader.di.UserDataManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request
import javax.inject.Inject

class MissEvanService @Inject constructor(
    val userdata: UserDataManager,
    @ApplicationScope private val scope: CoroutineScope
) {
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    suspend fun getEntireDrama(id: String): NetworkResult<MissEvanDramaResult> = withContext(Dispatchers.IO){
        try {
            val detailResult = NetworkHelper.doRequest(
                Request.Builder()
                    .get()
                    .url(MissEvanGetDramaListRequest(id))
                    .addMissEvanDramaFetchHeaders(id)
                    .build()
            ).use { response ->
                if (!response.isSuccessful) {
                    return@withContext NetworkResult.Error(
                        code = response.code,
                        message = "详情请求失败: ${response.code} ${response.message}"
                    )
                }
                try {
                    gson.fromJson(response.body?.string(), MissEvanEntireDramaResponse::class.java)
                } catch (e: Exception) {
                    return@withContext NetworkResult.Error(message = "详情JSON解析失败: ${e.message}")
                }
            }
            val (title, author) = Pair(detailResult.info.drama.name, detailResult.info.drama.author)
            val dramaList = detailResult.info.episodes.episode.toList()
            return@withContext NetworkResult.Success(MissEvanDramaResult(title, author, dramaList))
        } catch (e: Exception) {
            NetworkResult.Error(message = "请求过程发生错误: ${e.message}")
        }
    }

    suspend fun getDrama(id: String): NetworkResult<MissEvanSoundResponse> = withContext(Dispatchers.IO){
        try {
            val detailResult = NetworkHelper.doRequest(
                Request.Builder()
                    .get()
                    .url(MissEvanGetSoundRequest(id))
                    .also {
                        NetworkHelper.setCookies("www.missevan.com", mapOf(
                            "token" to userdata.userMissEvanData.value,
                        ))
                    }
                    .addMissEvanSoundFetchHeaders(id)
                    .build()
            ).use { response ->
                if (!response.isSuccessful) {
                    return@withContext NetworkResult.Error(
                        code = response.code,
                        message = "详情请求失败: ${response.code} ${response.message}"
                    )
                }
                try {
                    gson.fromJson(response.body?.string(), MissEvanDramaSoundResponse::class.java).info.sound
                } catch (e: Exception) {
                    return@withContext NetworkResult.Error(message = "详情JSON解析失败: ${e.message}")
                }
            }

            return@withContext NetworkResult.Success(detailResult)
        } catch (e: Exception) {
            NetworkResult.Error(message = "请求过程发生错误: ${e.message}")
        }
    }

}

fun extractMissEvanSoundId(url: String): String? {
    // 匹配可能的 URL 模式
    val patterns = listOf(
        """missevan\.com/sound/(\d+)""".toRegex(),         // 普通链接
        """missevan\.com/sound/player\?id=(\d+)""".toRegex() // 播放器链接
    )

    patterns.forEach { pattern ->
        pattern.find(url)?.groupValues?.getOrNull(1)?.let { id ->
            return id
        }
    }

    return null
}