package killua.dev.base.Login.Platforms

import killua.dev.base.Login.CookieRule
import killua.dev.base.Login.CookieRuleGroup
import killua.dev.base.Login.PlatformConfig
import killua.dev.base.datastore.writeLofterCookieExpiration
import killua.dev.base.datastore.writeLofterLoginAuth
import killua.dev.base.datastore.writeLofterLoginKey

class LofterConfig : PlatformConfig {
    private val possibleLoginKeys = listOf(
        "LOFTER-PHONE-LOGIN-AUTH",
        "Authorization",
        "LOFTER_SESS",
        "NTES_SESS"
    )

    override val loginUrl = "https://www.lofter.com/front/login"
    override val cookieDomain = "https://lofter.com"
    override val titleText = "Login your Lofter account"
    override val cookieRuleGroups: List<CookieRuleGroup> = listOf(
        CookieRuleGroup(
            rules = listOf(
                CookieRule(
                    name = "login",
                    pattern = "(${possibleLoginKeys.joinToString("|")})=([^;]+);[^;]*domain=([^;]+);[^;]*expires=([^;]+)",
                    saveFunction = { context, cookieInfo ->
                        context.writeLofterLoginKey(cookieInfo.key)
                        context.writeLofterLoginAuth(cookieInfo.value)
                        context.writeLofterCookieExpiration(cookieInfo.expiration ?: "")
                    }
                )
            ),
            matchOne = true
        )
    )
}