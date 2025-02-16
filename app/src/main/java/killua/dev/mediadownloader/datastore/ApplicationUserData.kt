package killua.dev.mediadownloader.datastore

data class ApplicationUserDataTwitter(
    val ct0: String,
    val auth: String,
    val twid: String
)

data class ApplicationUserDataLofter(
    val login_key: String,
    val login_auth: String,
    val start_time: Long,
    val end_time: Long
)