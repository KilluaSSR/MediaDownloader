package killua.dev.mediadownloader.api.Pixiv.BuildRequest

fun PixivRequestPicturesURL(id: String) = "https://www.pixiv.net/ajax/illust/$id/pages"
fun PixivRequestNovelURL(id: String) = "https://www.pixiv.net/ajax/novel/$id"
fun PixivRequestPicturesDetailsURL(id: String) = "https://www.pixiv.net/ajax/illust/$id"