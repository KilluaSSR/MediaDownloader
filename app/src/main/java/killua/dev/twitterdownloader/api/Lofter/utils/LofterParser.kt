package killua.dev.twitterdownloader.api.Lofter.utils

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import killua.dev.base.Model.ImageType
import killua.dev.base.utils.StringUtils
import killua.dev.base.utils.UserAgentUtils
import killua.dev.base.utils.isTimeEarlierThan
import killua.dev.base.utils.isTimeLaterThan
import killua.dev.base.utils.parseTimestamp
import killua.dev.twitterdownloader.Model.AuthorInfo
import killua.dev.twitterdownloader.Model.LofterParseRequiredInformation
import killua.dev.twitterdownloader.Model.NetworkResult
import killua.dev.twitterdownloader.api.Lofter.Model.ArchiveInfo
import killua.dev.twitterdownloader.api.Lofter.Model.BlogImage
import killua.dev.twitterdownloader.api.Lofter.Model.BlogInfo
import killua.dev.twitterdownloader.api.NetworkHelper
import kotlinx.coroutines.delay
import okhttp3.FormBody
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.net.URLDecoder
import java.util.regex.Pattern
import kotlin.random.Random

@OptIn(ExperimentalStdlibApi::class)
object LofterParser {
    private val imagePattern = "\"(http[s]{0,1}://imglf\\d{0,1}.lf\\d*.[0-9]{0,3}.net.*?)\"".toRegex()
    private val avatarPattern = "[1649]{2}[x,y][1649]{2}".toRegex()
    private val IMG_URL_PATTERN = Pattern.compile(""""(http[s]?://imglf\d?.lf\d*.[0-9]{0,3}.net.*?)"""")
    private val OLD_IMG_URL_PATTERN = Pattern.compile(""""(http[s]?://imglf\d.nosdn\d*.[0-9]{0,3}\d.net.*?)"""")
    private val TAGS_PATTERN = Pattern.compile(""""http[s]?://.*?\.lofter\.com/tag/(.*?)"""")

    fun parseAuthorInfo(html: String): Pair<String, String> {
        val doc = Jsoup.parse(html)

        val authorName = doc.select("h1.w-bttl2.w-bttl-hd > a:last-child").text()
            ?: throw Exception("未找到作者名")

        val authorId = doc.select("body iframe#control_frame").first()
            ?.attr("src")
            ?.split("blogId=")
            ?.getOrNull(1)
            ?: throw Exception("Author ID not found")

        return Pair(authorName, authorId)
    }

    fun parseAuthorInfo(document: Document, authorUrl: String): AuthorInfo {
        Log.d("LofterParser", "开始解析作者信息，原始URL: $authorUrl")

        // 解析作者ID
        Log.d("LofterParser", "正在解析作者ID...")
        val controlFrame = document.select("iframe#control_frame").first()
        Log.d("LofterParser", "control_frame元素: ${controlFrame != null}")

        val frameSrc = controlFrame?.attr("src")
        Log.d("LofterParser", "frame源地址: $frameSrc")

        val authorId = frameSrc?.split("blogId=")?.getOrNull(1)
            ?: throw IllegalStateException("Failed to parse author ID").also {
                Log.e("LofterParser", "解析作者ID失败", it)
            }
        Log.d("LofterParser", "成功解析作者ID: $authorId")

        // 解析作者域名
        Log.d("LofterParser", "正在解析作者域名...")
        val domainPattern = Regex("""https?://([^.]+)\.lofter\.com/""")
        val domainMatch = domainPattern.find(authorUrl)
        Log.d("LofterParser", "域名匹配结果: ${domainMatch != null}")

        val authorDomain = domainMatch?.groupValues?.getOrNull(1)
            ?: throw IllegalStateException("Failed to parse author IP").also {
                Log.e("LofterParser", "解析作者域名失败", it)
            }
        Log.d("LofterParser", "成功解析作者域名: $authorDomain")

        // 解析作者名称
        Log.d("LofterParser", "正在解析作者名称...")
        val nameSelector = "h1.w-bttl2.w-bttl-hd > a:last-child"
        val nameElement = document.select(nameSelector)
        Log.d("LofterParser", "名称元素是否存在: ${nameElement.isNotEmpty()}")

        val authorName = nameElement.text().takeIf { it.isNotEmpty() }
            ?: throw Exception("未找到作者名").also {
                Log.e("LofterParser", "解析作者名称失败", it)
            }
        Log.d("LofterParser", "成功解析作者名称: $authorName")

        return AuthorInfo(
            authorId = authorId,
            authorDomain = authorDomain,
            authorName = authorName
        ).also {
            Log.d("LofterParser", "作者信息解析完成: $it")
        }
    }

    fun parseImages(content: String): List<String> {
        return imagePattern.findAll(content)
            .map { it.groupValues[1] }
            .filter { !it.contains("&amp;") }
            .filter { avatarPattern.find(it) == null }
            .map { it.split("imageView")[0] }
            .distinct()
            .toList()
    }

    fun generateFilename(
        authorName: String,
        authorDomain: String,
        index: Int,
        imageUrl: String
    ): String {
        val safeAuthorName = StringUtils.sanitizeFilename(authorName)
        val imageType = ImageType.fromUrl(imageUrl)
        return "$safeAuthorName[$authorDomain] ($index).${imageType.extension}"
    }

    suspend fun postFormContent(
        url: String,
        data: Map<String, String>,
        headers: Map<String, String>,
        cookies: Map<String, String>? = null
    ): NetworkResult<String> {
        val formBody = FormBody.Builder().apply {
            data.forEach { (key, value) ->
                add(key, value)
            }
        }.build()

        return NetworkHelper.post(
            url = url,
            body = formBody,
            headers = headers,
            cookies = cookies ?: emptyMap()
        ) { bytes ->
            String(bytes, Charsets.UTF_8)
        }
    }

    suspend fun parseArchivePage(info: LofterParseRequiredInformation): MutableList<ArchiveInfo> {
        val allBlogInfo = mutableListOf<String>()
        val data = info.data.toMutableMap()
        val queryNum = 50

        var pageCount = 0
        while (true) {
            pageCount++

            when (val result = postFormContent(info.archiveURL, info.data, info.header, info.cookies)) {
                is NetworkResult.Success -> {
                    val pageData = result.data

                    val regex = Regex(
                        """s\d+\.blogId=\d+.*?values=s\d+.*?imgurl="([^"]+)".*?permalink="([^"]+)".*?noticeLinkTitle""",
                        RegexOption.DOT_MATCHES_ALL
                    )
                    val newBlogsInfo = regex.findAll(pageData)
                        .map { it.value }
                        .toList()

                    allBlogInfo.addAll(newBlogsInfo)

                    if (newBlogsInfo.size != queryNum) {
                        break
                    }

                    val currentTimestamp = data["c0-param2"]?.removePrefix("number:")
                    if (currentTimestamp != null &&
                        info.startTime != null &&
                        isTimeEarlierThan(currentTimestamp, info.startTime)) {
                        break
                    }

                    val lastIndexRegex = Regex("""s${queryNum - 1}\.time=(.*);s.*type""")
                    val nextTimestamp = lastIndexRegex.find(pageData)
                        ?.groupValues
                        ?.getOrNull(1)
                    if (nextTimestamp == null) {
                        break
                    }

                    data["c0-param2"] = "number:$nextTimestamp"
                    delay(Random.nextLong(1000, 2000))
                }

                is NetworkResult.Error -> {
                    break
                }
            }
        }

        val parsedBlogInfo = mutableListOf<ArchiveInfo>()
        var blogNum = 0

        for (blogInfo in allBlogInfo) {

            val timestamp = Regex("""s[\d]*.time=(\d*);""")
                .find(blogInfo)
                ?.groupValues
                ?.getOrNull(1)
            if (timestamp == null) {
                continue
            }

            if (info.startTime != null && isTimeEarlierThan(timestamp, info.startTime)) {
                break
            }

            if (info.endTime != null && isTimeLaterThan(timestamp, info.endTime)) {
                continue
            }

            blogNum++

            val imgUrl = Regex("""[\d]*.imgurl="(.*?)"""")
                .find(blogInfo)
                ?.groupValues
                ?.getOrNull(1)
            if (imgUrl == null || imgUrl.isEmpty()) {
                continue
            }

            val blogIndex = Regex("""s[\d]*.permalink="(.*?)"""")
                .find(blogInfo)
                ?.groupValues
                ?.getOrNull(1)
            if (blogIndex == null) {
                continue
            }

            val archiveInfo = ArchiveInfo(
                imgUrl = imgUrl,
                blogUrl = "${info.authorURL}post/$blogIndex",
                time = parseTimestamp(timestamp.toLong() / 1000)
            )
            parsedBlogInfo.add(archiveInfo)
        }

        return parsedBlogInfo
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    suspend fun parseFromArchiveInfos(
        archiveInfos: List<ArchiveInfo>,
        info: LofterParseRequiredInformation,
        targetTags: Set<String> = setOf(),
        saveNoTags: Boolean
    ): BlogInfo {

        val images = mutableListOf<BlogImage>()
        var lastTime = ""
        var lastIndex = 0
        var processedCount = 0

        for (archive in archiveInfos) {
            processedCount++
            when (val result = NetworkHelper.get(
                url = archive.blogUrl,
                headers = UserAgentUtils.getHeaders() + mapOf("Referer" to info.authorURL+"view"),
                cookies = info.cookies
            ) { it.decodeToString() }) {
                is NetworkResult.Success -> {
                    val content = result.data
                    if (!matchTags(content, targetTags.toList(), saveNoTags)) {
                        continue
                    }

                    val imageUrls = findImageUrls(content)

                    val startIndex = if (archive.time == lastTime) lastIndex else 0

                    val newImages = createBlogImages(
                        urls = imageUrls,
                        time = archive.time,
                        blogURL = archive.blogUrl,
                        info = info,
                        startIndex = startIndex
                    )

                    images.addAll(newImages)

                    lastTime = archive.time
                    lastIndex = startIndex + imageUrls.size
                }
                is NetworkResult.Error -> {
                    continue
                }
            }
        }

        return BlogInfo(
            authorName = info.authorName,
            authorId = info.authorID,
            authorDomain = info.authorDomain,
            images = images
        )
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private fun matchTags(content: String, targetTags: List<String>?, saveNoTags: Boolean): Boolean {
        // 如果没有目标标签，则全部保存
        if (targetTags.isNullOrEmpty()) return true

        val pageTags = TAGS_PATTERN.matcher(content)
            .results()
            .map { URLDecoder.decode(it.group(1), "UTF-8").replace("\u00a0", " ") }
            .toList()

        // 如果页面没有标签
        if (pageTags.isEmpty()) {
            return saveNoTags // 根据saveNoTags决定是否保存无标签内容
        }

        // 检查是否有任意一个目标标签匹配
        return pageTags.any { it in targetTags }
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private fun findImageUrls(content: String): List<String> {
        var urls = IMG_URL_PATTERN.matcher(content)
            .results()
            .map { it.group(1) }
            .toList()

        if (urls.isEmpty()) {
            urls = OLD_IMG_URL_PATTERN.matcher(content)
                .results()
                .map { it.group(1) }
                .toList()
        }
        return return urls
            .filter { it.contains("imglf", true) }  // 确保是Lofter图片服务器的URL
            .filterNot { it.contains("&amp;") }     // 过滤掉包含 &amp; 的URL
            .filterNot { url ->                     // 过滤掉头像图片
                Regex("[1649]{2}[x,y][1649]{2}").find(url) != null
            }
            .map { it.split("imageView")[0] }       // 删除图片链接中的大小参数
            .distinct()
    }

    private fun createBlogImages(
        urls: List<String>,
        time: String,
        blogURL: String,
        info: LofterParseRequiredInformation,
        startIndex: Int
    ): List<BlogImage> {
        return urls.mapIndexed { index, url ->
            val type = ImageType.fromUrl(url)
            val filename = "${info.authorName}[${info.authorID}] $time(${startIndex + index + 1}).${type.extension}"
            BlogImage(url = url, filename = filename, type = type, blogUrl = blogURL)
        }
    }
}

