package killua.dev.mediadownloader.api.Pixiv.BuildRequest

fun PixivRequestPicturesURL(id: String) = "https://www.pixiv.net/ajax/illust/$id/pages"
fun PixivRequestSingleNovelURL(id: String) = "https://www.pixiv.net/ajax/novel/$id"
fun PixivRequestEntireNovelURL(id: String) = "https://www.pixiv.net/ajax/novel/series_content/$id"
fun PixivRequestPicturesDetailsURL(id: String) = "https://www.pixiv.net/ajax/illust/$id"