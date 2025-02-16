package killua.dev.mediadownloader.Login.Platforms

import killua.dev.mediadownloader.Login.CookieRule
import killua.dev.mediadownloader.Login.CookieRuleGroup
import killua.dev.mediadownloader.Login.PlatformConfig
import killua.dev.mediadownloader.datastore.writeLofterCookieExpiration
import killua.dev.mediadownloader.datastore.writeLofterLoginAuth
import killua.dev.mediadownloader.datastore.writeLofterLoginKey

class LofterConfig : PlatformConfig {
    override val loginUrl = "https://www.lofter.com/front/login"
    override val cookieDomain = "https://lofter.com"
    override val titleText = "Login your Lofter account"
    override val cookieRuleGroups = listOf(
        CookieRuleGroup(
            rules = listOf(
                CookieRule(
                    name = "LOFTER-PHONE-LOGIN-AUTH",
                    pattern = "LOFTER-PHONE-LOGIN-AUTH=([^;]+)",
                    saveFunction = { context, cookieInfo ->
                        context.writeLofterLoginKey("LOFTER-PHONE-LOGIN-AUTH")
                        println("LOFTER = " + cookieInfo.value)
                        context.writeLofterLoginAuth(cookieInfo.value)
                        val tenDaysLater = System.currentTimeMillis() + (10L * 24 * 60 * 60 * 1000)
                        context.writeLofterCookieExpiration(tenDaysLater.toString())
                    }
                ),
                CookieRule(
                    name = "LOFTER_SESS",
                    pattern = "LOFTER_SESS=([^;]+)",
                    saveFunction = { context, cookieInfo ->
                        context.writeLofterLoginKey("LOFTER_SESS")
                        context.writeLofterLoginAuth(cookieInfo.value)
                        val tenDaysLater = System.currentTimeMillis() + (10L * 24 * 60 * 60 * 1000)
                        context.writeLofterCookieExpiration(tenDaysLater.toString())
                    }
                ),
                CookieRule(
                    name = "NTES_SESS",
                    pattern = "NTES_SESS=([^;]+)",
                    saveFunction = { context, cookieInfo ->
                        context.writeLofterLoginKey("NTES_SESS")
                        context.writeLofterLoginAuth(cookieInfo.value)
                        val tenDaysLater = System.currentTimeMillis() + (10L * 24 * 60 * 60 * 1000)
                        context.writeLofterCookieExpiration(tenDaysLater.toString())
                    }
                ),
                CookieRule(
                    name = "Authorization",
                    pattern = "Authorization=([^;]+)",
                    saveFunction = { context, cookieInfo ->
                        context.writeLofterLoginKey("Authorization")
                        context.writeLofterLoginAuth(cookieInfo.value)
                        val tenDaysLater = System.currentTimeMillis() + (10L * 24 * 60 * 60 * 1000)
                        context.writeLofterCookieExpiration(tenDaysLater.toString())
                    }
                )
            ),
            matchOne = true
        )
    )
}