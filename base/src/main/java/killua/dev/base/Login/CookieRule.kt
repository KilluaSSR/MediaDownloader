package killua.dev.base.Login

import android.content.Context

data class CookieRule(
    val name: String,
    val pattern: String,
    val saveFunction: suspend (Context, CookieInfo) -> Unit
)

data class CookieInfo(
    val key: String,
    val value: String,
    val domain: String? = null,
    val expiration: String? = null
)

data class CookieRuleGroup(
    val rules: List<CookieRule>,
    val matchOne: Boolean = false
)