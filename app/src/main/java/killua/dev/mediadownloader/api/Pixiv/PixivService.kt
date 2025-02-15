package killua.dev.mediadownloader.api.Pixiv

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import killua.dev.base.di.ApplicationScope
import killua.dev.mediadownloader.Model.NetworkResult
import killua.dev.mediadownloader.api.NetworkHelper
import killua.dev.mediadownloader.api.Pixiv.BuildRequest.PixivRequestPicturesDetailsURL
import killua.dev.mediadownloader.api.Pixiv.BuildRequest.PixivRequestPicturesURL
import killua.dev.mediadownloader.api.Pixiv.BuildRequest.addPixivPictureFetchHeaders
import killua.dev.mediadownloader.api.Pixiv.Model.PixivImageInfo
import killua.dev.mediadownloader.api.Pixiv.Model.PixivPictureDetailResponse
import killua.dev.mediadownloader.api.Pixiv.Model.PixivPicturePageResponse
import killua.dev.mediadownloader.di.UserDataManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request
import javax.inject.Inject

class PixivService @Inject constructor(
    val userdata: UserDataManager,
    @ApplicationScope private val scope: CoroutineScope
) {
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    suspend fun getSingleBlogImage(url: String): NetworkResult<PixivImageInfo> = withContext(Dispatchers.IO) {
        val id = try {
            url.split("artworks/")[1]
        } catch (e: Exception) {
            println("URL解析失败: ${e.message}")
            return@withContext NetworkResult.Error(message = "URL格式错误: ${e.message}")
        }

        try {
            val detailResult = NetworkHelper.doRequest(
                Request.Builder()
                    .get()
                    .url(PixivRequestPicturesDetailsURL(id))
                    .addPixivPictureFetchHeaders(id)
                    .build()
                    .also {
                        NetworkHelper.setCookies("pixiv.net", mapOf(
                            "PHPSESSID" to userdata.userPixivPHPSSID.value
                        ))
                    }
            ).use { response ->
                if (!response.isSuccessful) {
                    println("详情请求失败: ${response.code} ${response.message}")
                    println("响应体: ${response.body?.string()}")
                    return@withContext NetworkResult.Error(
                        code = response.code,
                        message = "详情请求失败: ${response.code} ${response.message}"
                    )
                }
                try {
                    gson.fromJson(response.body?.string(), PixivPictureDetailResponse::class.java).body
                } catch (e: Exception) {
                    println("详情JSON解析失败: ${e.message}")
                    println("原始JSON: ${response.body?.string()}")
                    return@withContext NetworkResult.Error(message = "详情JSON解析失败: ${e.message}")
                }
            }

            val urls = NetworkHelper.doRequest(
                Request.Builder()
                    .get()
                    .url(PixivRequestPicturesURL(id))
                    .addPixivPictureFetchHeaders(id)
                    .build()
                    .also {
                        NetworkHelper.setCookies("pixiv.net", mapOf(
                            "PHPSESSID" to userdata.userPixivPHPSSID.value
                        ))
                    }
            ).use { response ->
                if (!response.isSuccessful) {

                    return@withContext NetworkResult.Error(
                        code = response.code,
                        message = "图片URL请求失败: ${response.code} ${response.message}"
                    )
                }

                try {
                    gson.fromJson(response.body?.string(), PixivPicturePageResponse::class.java)
                        .body
                        .map { it.urls.original }
                } catch (e: Exception) {

                    return@withContext NetworkResult.Error(message = "URL列表JSON解析失败: ${e.message}")
                }
            }

            println("请求成功: 用户名=${detailResult.userName}, ID=${detailResult.illustId}, 图片数量=${urls.size}")
            return@withContext NetworkResult.Success(
                PixivImageInfo(
                    userName = detailResult.userName,
                    userId = detailResult.userId,
                    title = detailResult.title,
                    illustId = detailResult.illustId,
                    originalUrls = urls
                )
            )
        } catch (e: Exception) {
            println("请求过程发生错误: ${e.message}")
            println("错误堆栈: ${e.stackTraceToString()}")
            NetworkResult.Error(message = "请求过程发生错误: ${e.message}")
        }
    }
}