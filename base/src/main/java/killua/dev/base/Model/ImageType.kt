package killua.dev.base.Model

enum class ImageType(val extension: String) {
    GIF("gif"),
    PNG("png"),
    JPG("jpg");

    companion object {
        fun fromUrl(url: String): ImageType = when {
            url.contains("gif", true) -> GIF
            url.contains("png", true) -> PNG
            else -> JPG
        }
    }
}