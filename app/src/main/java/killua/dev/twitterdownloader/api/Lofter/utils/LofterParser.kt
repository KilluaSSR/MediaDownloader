package killua.dev.twitterdownloader.api.Lofter.utils

import killua.dev.base.Model.ImageType
import killua.dev.base.utils.StringUtils
import org.jsoup.Jsoup

object LofterParser {
    private val imagePattern = "\"(http[s]{0,1}://imglf\\d{0,1}.lf\\d*.[0-9]{0,3}.net.*?)\"".toRegex()
    private val avatarPattern = "[1649]{2}[x,y][1649]{2}".toRegex()

    fun parseAuthorInfo(html: String): Pair<String, String> {
        val doc = Jsoup.parse(html)

        val authorName = doc.select("h1 a").first()?.text()
            ?: throw Exception("Author name not found")

        val authorId = doc.select("body iframe#control_frame").first()
            ?.attr("src")
            ?.split("blogId=")
            ?.getOrNull(1)
            ?: throw Exception("Author ID not found")

        return Pair(authorName, authorId)
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
}

