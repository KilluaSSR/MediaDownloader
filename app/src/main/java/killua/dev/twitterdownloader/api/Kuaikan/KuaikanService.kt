package killua.dev.twitterdownloader.api.Kuaikan

import killua.dev.base.di.ApplicationScope
import killua.dev.base.utils.USER_AGENT
import killua.dev.twitterdownloader.Model.NetworkResult
import killua.dev.twitterdownloader.api.Kuaikan.BuildRequest.KuaikanSingleChapterRequest
import killua.dev.twitterdownloader.api.Kuaikan.BuildRequest.KuaikanWholeComicRequest
import killua.dev.twitterdownloader.api.Kuaikan.Model.MangaInfo
import killua.dev.twitterdownloader.api.NetworkHelper
import killua.dev.twitterdownloader.di.UserDataManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request
import javax.inject.Inject

data class Chapter(val name: String, val id: String)
data class NuxtParams(val returnObject: String, val parameters: String)
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
                val (title, chapter) = extractSingleMangaInfo(htmlContent)
                    ?: return@withContext NetworkResult.Error(message = "无法提取标题和章节信息")
                val imageUrls = extractSingleImageUrls(htmlContent)
                if (imageUrls.isEmpty()) {
                    return@withContext NetworkResult.Error(message = "未找到图片URL")
                }

                NetworkResult.Success(MangaInfo(title, url, chapter, imageUrls))
            }
        } catch (e: Exception) {
            val errorMessage = "请求处理失败: ${e.message}"
            e.printStackTrace()
            NetworkResult.Error(message = errorMessage)
        }
    }

    suspend fun getWholeComic(url: String): NetworkResult<List<Chapter>> = withContext(Dispatchers.IO) {
        val pattern = """(?:topic|mobile)/(\d+)(?:/list)?(?:[/?].*)?$""".toRegex()

        val id = try {
            pattern.find(url)?.groupValues?.get(1)
                ?: throw IllegalArgumentException("URL中未找到漫画ID: $url")
        } catch (e: Exception) {
            val errorMessage = "URL解析失败: ${e.message}"
            e.printStackTrace()
            return@withContext NetworkResult.Error(message = errorMessage)
        }
        try {
            NetworkHelper.doRequest(
                Request.Builder()
                    .get()
                    .url(KuaikanWholeComicRequest(id))
                    .header("User-Agent", USER_AGENT)
                    .build()
            ).use { response ->
                if (!response.isSuccessful) {
                    return@withContext NetworkResult.Error(
                        code = response.code,
                        message = "详情请求失败: ${response.code} ${response.message}"
                    )
                }

                val htmlContent = response.body?.string()
                    ?: return@withContext NetworkResult.Error(message = "响应内容为空")

                println("HTML内容长度: ${htmlContent.length}")
                println("HTML内容前500字符: ${htmlContent.take(500)}")

                val result = extractNuxtParams(htmlContent)
                if (result == null) {
                    return@withContext NetworkResult.Error(message = "无法提取NUXT参数")
                }

                val chapters = extractChapterInfo(result.parameters)
                if (chapters.isEmpty()) {
                    return@withContext NetworkResult.Error(message = "未找到任何章节信息")
                }

                return@withContext NetworkResult.Success(chapters)
            }
        } catch (e: Exception) {
            val errorMessage = "请求处理失败: ${e.message}"
            e.printStackTrace()
            return@withContext NetworkResult.Error(message = errorMessage)
        }
    }

    fun extractChapterInfo(paramsStr: String): List<Chapter> {
        val pattern = """"id":(\d{6}),"title":"([^"]+)""""
        val regex = Regex(pattern)

        val chapters = mutableListOf<Chapter>()
        regex.findAll(paramsStr).forEach { matchResult ->
            val chapterId = matchResult.groupValues[1]
            val chapterName = matchResult.groupValues[2]
            chapters.add(Chapter(chapterName, chapterId))
        }

        if (chapters.isEmpty()) {
            val backupPattern = """\.jpg","[^"]+","[^"]+",(\d{6}),"([^"]+)""""
            Regex(backupPattern).findAll(paramsStr).forEach { matchResult ->
                val chapterId = matchResult.groupValues[1]
                val chapterName = matchResult.groupValues[2]
                chapters.add(Chapter(chapterName, chapterId))
            }
        }

        return chapters.sortedBy { chapter ->
            Regex("""第(\d+)话""").find(chapter.name)
                ?.groupValues?.get(1)?.toIntOrNull() ?: 999
        }
    }

    fun extractNuxtParams(htmlContent: String): NuxtParams? {

        try {
            // 1. 提取NUXT内容
            val nuxtStartIndex = htmlContent.indexOf("window.__NUXT__=")
            if (nuxtStartIndex == -1) {
                return null
            }

            val nuxtEndIndex = htmlContent.indexOf(";</script>", nuxtStartIndex)
            if (nuxtEndIndex == -1) {
                return null
            }

            val nuxtContent = htmlContent.substring(
                nuxtStartIndex + "window.__NUXT__=".length,
                nuxtEndIndex
            ).trim()

            // 2. 提取return对象
            val returnStartIndex = nuxtContent.indexOf("return") + "return".length
            val returnEndIndex = nuxtContent.lastIndexOf("}")
            if (returnStartIndex == -1 || returnEndIndex == -1) {
                return null
            }

            val returnObject = nuxtContent.substring(returnStartIndex, returnEndIndex + 1).trim()

            // 3. 提取函数参数
            val paramsStartIndex = nuxtContent.lastIndexOf("(") + 1
            val paramsEndIndex = nuxtContent.lastIndexOf(")")
            if (paramsStartIndex <= 0 || paramsEndIndex <= paramsStartIndex) {
                return null
            }

            val parameters = nuxtContent.substring(paramsStartIndex, paramsEndIndex)


            return NuxtParams(returnObject, parameters)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }


    private fun extractSingleImageUrls(htmlContent: String): List<String> {
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

    private fun extractSingleMangaInfo(htmlContent: String): Pair<String, String>? {
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