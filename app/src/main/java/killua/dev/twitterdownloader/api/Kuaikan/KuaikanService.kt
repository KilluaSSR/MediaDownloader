package killua.dev.twitterdownloader.api.Kuaikan

import killua.dev.base.di.ApplicationScope
import killua.dev.base.utils.USER_AGENT
import killua.dev.twitterdownloader.Model.NetworkResult
import killua.dev.twitterdownloader.api.Kuaikan.BuildRequest.KuaikanSingleChapterRequest
import killua.dev.twitterdownloader.api.Kuaikan.Model.MangaInfo
import killua.dev.twitterdownloader.api.NetworkHelper
import killua.dev.twitterdownloader.api.Pixiv.BuildRequest.addPixivPictureDownloadHeaders
import killua.dev.twitterdownloader.di.UserDataManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request
import javax.inject.Inject

class KuaikanService @Inject constructor(
    val userdata: UserDataManager,
    @ApplicationScope private val scope: CoroutineScope
) {
    suspend fun getSingleChapter(url: String): NetworkResult<MangaInfo> = withContext(Dispatchers.IO) {
        val pattern = """(?:comics/|comic-next/|comic/)(\d+)""".toRegex()

        val id = try {
            pattern.find(url)?.groupValues?.get(1)
                ?: throw IllegalArgumentException("URL中未找到数字ID: $url")
        } catch (e: Exception) {
            val errorMessage = "URL解析失败: ${e.message}"
            println(errorMessage)
            e.printStackTrace()
            return@withContext NetworkResult.Error(message = errorMessage)
        }
        try {
            NetworkHelper.doRequest(
                Request.Builder()
                    .get()
                    .url(KuaikanSingleChapterRequest(id))
                    .header("User-Agent", USER_AGENT)
                    .also {
                        NetworkHelper.setCookies("www.kuaikanmanhua.com", mapOf(
                            "passToken" to userdata.userKuaikanData.value
                        ))
                    }.build()
            ).use { response ->
                if (!response.isSuccessful) {
                    val errorMessage = "详情请求失败: ${response.code} ${response.message}"
                    return@withContext NetworkResult.Error(
                        code = response.code,
                        message = errorMessage
                    )
                }
                val htmlContent = response.body?.string()
                    ?: return@withContext NetworkResult.Error(message = "响应内容为空")
                val (title, chapter) = extractMangaInfo(htmlContent)
                    ?: return@withContext NetworkResult.Error(message = "无法提取标题和章节信息")
                val imageUrls = extractImageUrls(htmlContent)
                imageUrls.forEach{
                    println(it)
                }
                if (imageUrls.isEmpty()) {
                    return@withContext NetworkResult.Error(message = "未找到图片URL")
                }

                NetworkResult.Success(MangaInfo(title, url, chapter, imageUrls))
            }
        } catch (e: Exception) {
            val errorMessage = "请求处理失败: ${e.message}"
            println(errorMessage)
            e.printStackTrace()
            NetworkResult.Error(message = errorMessage)
        }
    }

    private fun extractImageUrls(htmlContent: String): List<String> {
        val scriptPattern = """window\.__NUXT__=(.*?)</script>""".toRegex(RegexOption.DOT_MATCHES_ALL)
        val scriptContent = scriptPattern.find(htmlContent)?.groupValues?.get(1)

        if (scriptContent == null) {
            return emptyList()
        }
        println(scriptContent)

        val urls = scriptContent
            .split(',')
            .filter { it.contains("http") && it.contains("webp-t.w") && it.contains("jpg.h?sign") }
            .map { it.trim('"').replace("\\u002F", "/") }
        return urls
    }

    private fun extractMangaInfo(htmlContent: String): Pair<String, String>? {
        // 使用正则表达式匹配标题和章节
        val titlePattern = """class="step-topic"[^>]*>([^<]+)""".toRegex()
        val chapterPattern = """class="step-comic"[^>]*>([^<]+)""".toRegex()

        val title = titlePattern.find(htmlContent)?.groupValues?.get(1)?.trim()
        val chapter = chapterPattern.find(htmlContent)?.groupValues?.get(1)?.trim()
        println(title)
        println(chapter)
        return if (title != null && chapter != null) {
            Pair(title, chapter)
        } else null
    }
}