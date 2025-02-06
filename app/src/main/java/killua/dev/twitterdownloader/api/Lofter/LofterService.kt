package killua.dev.twitterdownloader.api.Lofter

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import killua.dev.base.Model.ImageType
import killua.dev.base.utils.UserAgentUtils
import killua.dev.twitterdownloader.Model.NetworkResult
import killua.dev.twitterdownloader.api.Lofter.Model.BlogImage
import killua.dev.twitterdownloader.api.Lofter.Model.BlogInfo
import killua.dev.twitterdownloader.api.Lofter.utils.LofterParser
import killua.dev.twitterdownloader.api.NetworkHelper
import killua.dev.twitterdownloader.utils.extractLofterUserDomain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class LofterService @Inject constructor(
    @ApplicationContext private val context: Context
){
    suspend fun getBlogImages(
        blogUrl: String,
        loginKey: String,
        loginAuth: String
    ): NetworkResult<List<BlogInfo>> = withContext(Dispatchers.IO) {
        try {
            val results = mutableListOf<BlogInfo>()
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
            val (authorName, authorId) = LofterParser.parseAuthorInfo(authorViewContent)
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
}