package killua.dev.twitterdownloader.api.Kuaikan

import killua.dev.base.di.ApplicationScope
import killua.dev.twitterdownloader.Model.NetworkResult
import killua.dev.twitterdownloader.api.Kuaikan.BuildRequest.KuaikanSingleChapterRequest
import killua.dev.twitterdownloader.api.Kuaikan.Model.MangaInfo
import killua.dev.twitterdownloader.api.NetworkHelper
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
        val id = try {
            url.split("comic-next/")[1]
        } catch (e: Exception) {
            println("URL解析失败: ${e.message}")
            return@withContext NetworkResult.Error(message = "URL格式错误: ${e.message}")
        }
        try {
            NetworkHelper.doRequest(
                Request.Builder()
                    .get()
                    .url(KuaikanSingleChapterRequest(id))
                    .build()
                    .also {
                        NetworkHelper.setCookies("kuaikanmanhua.com", mapOf(
                            "passToken" to userdata.userKuaikanData.value
                        ))
                    }
            ).use { response ->
                if (!response.isSuccessful) {
                    return@withContext NetworkResult.Error(
                        code = response.code,
                        message = "详情请求失败: ${response.code} ${response.message}"
                    )
                }
                val htmlContent = response.body?.string()
                    ?: return@withContext NetworkResult.Error(message = "响应内容为空")

                val (title, chapter) = extractMangaInfo(htmlContent)
                    ?: return@withContext NetworkResult.Error(message = "无法提取标题和章节信息")

                val imageUrls = extractImageUrls(htmlContent)
                if (imageUrls.isEmpty()) {
                    return@withContext NetworkResult.Error(message = "未找到图片URL")
                }

                NetworkResult.Success(MangaInfo(title,url, chapter, imageUrls))

            }
        } catch (e: Exception) {
            NetworkResult.Error(message = "Error: ${e.message}")
        }
    }

    private fun extractImageUrls(htmlContent: String): List<String> {
        val scriptPattern = """window\.__NUXT__=(.*?)</script>""".toRegex(RegexOption.DOT_MATCHES_ALL)
        val scriptContent = scriptPattern.find(htmlContent)?.groupValues?.get(1)

        if (scriptContent == null) {
            return emptyList()
        }

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

        return if (title != null && chapter != null) {
            Pair(title, chapter)
        } else null
    }
}