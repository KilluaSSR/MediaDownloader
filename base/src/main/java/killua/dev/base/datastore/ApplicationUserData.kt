package killua.dev.base.datastore

data class ApplicationUserDataTwitter(
    val ct0: String,
    val auth: String,
    val twid: String
)

data class ApplicationUserDataLofter(
    val login_key: String,
    val login_auth: String
)