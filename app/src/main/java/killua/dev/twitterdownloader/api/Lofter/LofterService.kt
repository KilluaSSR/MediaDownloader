package killua.dev.twitterdownloader.api.Lofter

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import dagger.hilt.android.qualifiers.ApplicationContext
import killua.dev.base.Model.ImageType
import killua.dev.base.datastore.readLofterLoginAuth
import killua.dev.base.datastore.readLofterLoginKey
import killua.dev.base.utils.UserAgentUtils
import killua.dev.twitterdownloader.Model.LofterParseRequiredInformation
import killua.dev.twitterdownloader.Model.NetworkResult
import killua.dev.twitterdownloader.api.Lofter.BuildRequest.makeArchiveData
import killua.dev.twitterdownloader.api.Lofter.Model.BlogImage
import killua.dev.twitterdownloader.api.Lofter.Model.BlogInfo
import killua.dev.twitterdownloader.api.Lofter.utils.LofterParser
import killua.dev.twitterdownloader.api.Lofter.utils.LofterParser.parseArchivePage
import killua.dev.twitterdownloader.api.Lofter.utils.LofterParser.parseAuthorInfo
import killua.dev.twitterdownloader.api.Lofter.utils.LofterParser.parseFromArchiveInfos
import killua.dev.twitterdownloader.api.NetworkHelper
import killua.dev.twitterdownloader.utils.extractLofterUserDomain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import javax.inject.Inject

class LofterService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    suspend fun getBlogImages(blogUrl: String): NetworkResult<List<BlogInfo>> = withContext(Dispatchers.IO) {
        try {
            val results = mutableListOf<BlogInfo>()
            val loginKey = context.readLofterLoginKey().first()
            val loginAuth = context.readLofterLoginAuth().first()
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
                        type = ImageType.fromUrl(imageUrl)
                    )
                }
            )
            results.add(blogInfo)
            NetworkResult.Success(results)
        } catch (e: Exception) {
            println(e.message)
            NetworkResult.Error(message = e.message ?: "Failed")
        }
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    suspend fun getByAuthorTags(
        authorUrl: String,
        startTime: String,
        endTime: String,
        tags: Set<String>,
        saveNoTags: Boolean
    ): BlogInfo {
        val loginKey = context.readLofterLoginKey().first()
        val loginAuth = context.readLofterLoginAuth().first()
        val pageContent = when (val result = NetworkHelper.get(
            url = "$authorUrl/view",
            headers = UserAgentUtils.getHeaders(),
            cookies = mapOf(loginKey to loginAuth)
        ) { String(it, Charsets.UTF_8) }) {
            is NetworkResult.Success -> result.data
            is NetworkResult.Error -> throw IllegalStateException("Failed to fetch author page")
        }
        val document = Jsoup.parse(pageContent)
        val authorInfo = parseAuthorInfo(document, authorUrl)

        val archiveUrl = "${authorUrl}dwr/call/plaincall/ArchiveBean.getArchivePostByTime.dwr"
        val data = makeArchiveData(authorInfo.authorId, 50)
        val header = UserAgentUtils.makeLofterHeaders(authorUrl)
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

        val blogInfos = parseArchivePage(requiredInfo)

        return parseFromArchiveInfos(blogInfos, requiredInfo, tags, saveNoTags)
    }
}





























