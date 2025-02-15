package killua.dev.mediadownloader.api.Lofter

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import killua.dev.base.Model.ImageType
import killua.dev.base.di.ApplicationScope
import killua.dev.base.utils.UserAgentUtils
import killua.dev.mediadownloader.Model.LofterParseRequiredInformation
import killua.dev.mediadownloader.Model.NetworkResult
import killua.dev.mediadownloader.api.Lofter.BuildRequest.makeArchiveData
import killua.dev.mediadownloader.api.Lofter.Model.BlogImage
import killua.dev.mediadownloader.api.Lofter.Model.BlogInfo
import killua.dev.mediadownloader.api.Lofter.Model.extractLofterUserDomain
import killua.dev.mediadownloader.api.Lofter.utils.LofterParser
import killua.dev.mediadownloader.api.Lofter.utils.LofterParser.parseArchivePage
import killua.dev.mediadownloader.api.Lofter.utils.LofterParser.parseAuthorInfo
import killua.dev.mediadownloader.api.Lofter.utils.LofterParser.parseFromArchiveInfos
import killua.dev.mediadownloader.api.NetworkHelper
import killua.dev.mediadownloader.di.UserDataManager
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.jsoup.Jsoup
import javax.inject.Inject

class LofterService @Inject constructor(
    val userdata: UserDataManager,
    @ApplicationScope private val scope: CoroutineScope
) {
    fun getBlogImages(blogUrl: String): NetworkResult<BlogInfo>  {
        val loginKey = userdata.userLofterData.value.login_key
        val loginAuth = userdata.userLofterData.value.login_auth
        val deferred = CompletableDeferred<NetworkResult<BlogInfo>>()
        scope.launch{
            try {
                val blogContent = NetworkHelper.get(
                    url = blogUrl,
                    headers = UserAgentUtils.getHeaders(),
                    cookies = mapOf(loginKey to loginAuth)
                ) { it.decodeToString() }.let { result ->
                    when (result) {
                        is NetworkResult.Success -> result.data
                        is NetworkResult.Error -> throw Exception("Failed: ${result.message}")
                    }
                }
                val authorViewUrl = "${blogUrl.split("/post")[0]}/view"
                val authorViewContent = NetworkHelper.get(
                    url = authorViewUrl,
                    headers = UserAgentUtils.getHeaders(),
                    cookies = mapOf(loginKey to loginAuth)
                ) { it.decodeToString() }.let { result ->
                    when (result) {
                        is NetworkResult.Success -> result.data
                        is NetworkResult.Error -> throw Exception("Failed:  ${result.message}")
                    }
                }
                val (authorName, authorId) = parseAuthorInfo(authorViewContent)
                val authorDomain = blogUrl.extractLofterUserDomain()
                val imagesUrl = LofterParser.parseImages(blogContent)
                val blogInfo = BlogInfo(
                    authorName = authorName,
                    authorId = authorId,
                    authorDomain = authorDomain!!,
                    images = imagesUrl.mapIndexed { index, imageUrl ->
                        BlogImage(
                            url = imageUrl,
                            filename = LofterParser.generateFilename(
                                authorName = authorName,
                                authorDomain = authorDomain,
                                index = index + 1,
                                imageUrl = imageUrl
                            ),
                            type = ImageType.fromUrl(imageUrl),
                            blogUrl = blogUrl
                        )
                    }
                )
                deferred.complete(NetworkResult.Success(blogInfo))
            }catch (e: Exception) {
                deferred.complete(NetworkResult.Error(message = e.message ?: "Failed"))
            }
        }
        return runBlocking { deferred.await() }
    }


    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    suspend fun getByAuthorTags(
        authorUrl: String,
        tags: Set<String>,
        saveNoTags: Boolean = false
    ): BlogInfo {
        Log.d("LofterParser", "开始解析作者页面，URL: $authorUrl")
        Log.d("LofterParser", "标签集合: ${tags.joinToString()}")
        Log.d("LofterParser", "保存无标签内容: $saveNoTags")

        val loginKey = userdata.userLofterData.value.login_key
        val loginAuth = userdata.userLofterData.value.login_auth
        val startTime = userdata.userLofterData.value.start_time.toString()
        val endTime = userdata.userLofterData.value.end_time.toString()

        Log.d("LofterParser", "用户认证信息 - Key: $loginKey, 开始时间: $startTime, 结束时间: $endTime")

        val pageContent = try {
            Log.d("LofterParser", "正在获取作者页面内容...")
            when (val result = NetworkHelper.get(
                url = authorUrl + "view",
                headers = UserAgentUtils.getHeaders(),
                cookies = mapOf(loginKey to loginAuth)
            ) { String(it, Charsets.UTF_8) }) {
                is NetworkResult.Success -> {
                    Log.d("LofterParser", "页面内容获取成功，长度: ${result.data.length}")
                    result.data
                }
                is NetworkResult.Error -> {
                    Log.e("LofterParser", "页面获取失败: ${result.message}")
                    throw IllegalStateException("Failed to fetch author page")
                }
            }
        } catch (e: Exception) {
            Log.e("LofterParser", "获取页面时发生异常", e)
            throw e
        }

        Log.d("LofterParser", "开始解析文档...")
        val document = Jsoup.parse(pageContent)
        val authorInfo = parseAuthorInfo(document, authorUrl)
        Log.d("LofterParser", "作者信息解析完成 - ID: ${authorInfo.authorId}, 名称: ${authorInfo.authorName}")

        val archiveUrl = "${authorUrl}dwr/call/plaincall/ArchiveBean.getArchivePostByTime.dwr"
        Log.d("LofterParser", "构建存档URL: $archiveUrl")

        val data = makeArchiveData(authorInfo.authorId, 50)
        val header = UserAgentUtils.makeLofterHeaders(authorUrl)
        Log.d("LofterParser", "请求数据准备完成 - 数据大小: ${data.size}, 头部项数: ${header.size}")

        val requiredInfo = LofterParseRequiredInformation(
            archiveURL = archiveUrl,
            authorID = authorInfo.authorId,
            authorURL = authorUrl,
            authorName = authorInfo.authorName,
            authorDomain = authorInfo.authorDomain,
            cookies = mapOf(loginKey to loginAuth),
            header = header,
            data = data,
            startTime = startTime,
            endTime = endTime
        )
        Log.d("LofterParser", "必要信息整理完成 - 作者域名: ${authorInfo.authorDomain}")

        Log.d("LofterParser", "开始解析存档页面...")
        val blogInfos = parseArchivePage(requiredInfo)
        Log.d("LofterParser", "存档页面解析完成，获取到 ${blogInfos.size} 条博客信息")

        Log.d("LofterParser", "开始从存档信息解析最终数据...")
        return parseFromArchiveInfos(blogInfos, requiredInfo, tags, saveNoTags).also {
            Log.d("LofterParser", "数据解析完成")
        }
    }
}





























