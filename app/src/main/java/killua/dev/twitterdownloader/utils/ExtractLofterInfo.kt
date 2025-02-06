package killua.dev.twitterdownloader.utils

fun String.extractLofterUserDomain(): String? {
    val pattern = "https://([^.]+)\\.lofter".toRegex()
    return pattern.find(this)?.groupValues?.get(1)
}