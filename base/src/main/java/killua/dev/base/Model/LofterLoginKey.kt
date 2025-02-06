package killua.dev.base.Model

enum class LofterLoginKey(val key: String) {
    NULL(""),
    AUTHORIZATION("Authorization"),
    PHONE_LOGIN_AUTH("LOFTER-PHONE-LOGIN-AUTH"),
    LOFTER_SESS("LOFTER_SESS"),
    NTES_SESS("NTES_SESS");

    companion object {
        fun fromString(key: String): LofterLoginKey {
            return entries.find { it.key == key } ?: NULL
        }
    }
}