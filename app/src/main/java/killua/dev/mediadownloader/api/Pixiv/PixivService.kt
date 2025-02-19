package killua.dev.mediadownloader.api.Pixiv

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import killua.dev.mediadownloader.Model.NetworkResult
import killua.dev.mediadownloader.api.NetworkHelper
import killua.dev.mediadownloader.api.Pixiv.BuildRequest.PixivRequestEntireNovelURL
import killua.dev.mediadownloader.api.Pixiv.BuildRequest.PixivRequestSingleNovelURL
import killua.dev.mediadownloader.api.Pixiv.BuildRequest.PixivRequestPicturesDetailsURL
import killua.dev.mediadownloader.api.Pixiv.BuildRequest.PixivRequestPicturesURL
import killua.dev.mediadownloader.api.Pixiv.BuildRequest.addPixivNovelFetchHeaders
import killua.dev.mediadownloader.api.Pixiv.BuildRequest.addPixivPictureFetchHeaders
import killua.dev.mediadownloader.api.Pixiv.Model.NovelInfo
import killua.dev.mediadownloader.api.Pixiv.Model.PixivBlogInfo
import killua.dev.mediadownloader.api.Pixiv.Model.PixivEntireNovelDetailResponse
import killua.dev.mediadownloader.api.Pixiv.Model.PixivImageInfo
import killua.dev.mediadownloader.api.Pixiv.Model.PixivNovelDetailResponse
import killua.dev.mediadownloader.api.Pixiv.Model.PixivPictureDetailResponse
import killua.dev.mediadownloader.api.Pixiv.Model.PixivPicturePageResponse
import killua.dev.mediadownloader.di.ApplicationScope
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
    suspend fun getNovel(id: String): NetworkResult<PixivBlogInfo> = withContext(Dispatchers.IO){
        try {
            val detailResult = NetworkHelper.doRequest(
                Request.Builder()
                    .get()
                    .url(PixivRequestSingleNovelURL(id))
                    .addPixivNovelFetchHeaders(id)
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
                        message = "详情请求失败: ${response.code} ${response.message}"
                    )
                }
                try {
                    gson.fromJson(response.body?.string(), PixivNovelDetailResponse::class.java).body
                } catch (e: Exception) {
                    return@withContext NetworkResult.Error(message = "详情JSON解析失败: ${e.message}")
                }
            }
            return@withContext NetworkResult.Success(detailResult)

        } catch (e: Exception) {
            NetworkResult.Error(message = "请求过程发生错误: ${e.message}")
        }

    }

    suspend fun getEntireNovel(id: String): NetworkResult<List<NovelInfo>> = withContext(Dispatchers.IO){
        try {
            val detailResult = NetworkHelper.doRequest(
                Request.Builder()
                    .get()
                    .url(PixivRequestEntireNovelURL(id))
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
                        message = "详情请求失败: ${response.code} ${response.message}"
                    )
                }
                try {
                    gson.fromJson(response.body?.string(), PixivEntireNovelDetailResponse::class.java).body
                } catch (e: Exception) {
                    return@withContext NetworkResult.Error(message = "详情JSON解析失败: ${e.message}")
                }
            }
            return@withContext NetworkResult.Success(detailResult.thumbnails.novel)
        } catch (e: Exception) {
            NetworkResult.Error(message = "请求过程发生错误: ${e.message}")
        }
    }

    suspend fun getSingleBlogImage(id: String): NetworkResult<PixivImageInfo> = withContext(Dispatchers.IO) {
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
                    return@withContext NetworkResult.Error(
                        code = response.code,
                        message = "详情请求失败: ${response.code} ${response.message}"
                    )
                }
                try {
                    gson.fromJson(response.body?.string(), PixivPictureDetailResponse::class.java).body
                } catch (e: Exception) {
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
            NetworkResult.Error(message = "请求过程发生错误: ${e.message}")
        }
    }
}