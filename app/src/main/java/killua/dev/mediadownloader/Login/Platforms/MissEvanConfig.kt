package killua.dev.mediadownloader.Login.Platforms

import killua.dev.mediadownloader.Login.CookieRule
import killua.dev.mediadownloader.Login.CookieRuleGroup
import killua.dev.mediadownloader.Login.PlatformConfig
import killua.dev.mediadownloader.datastore.writeMissEvanToken

class MissEvanConfig : PlatformConfig {
    override val loginUrl = "https://www.missevan.com/member/login"
    override val cookieDomain = "https://www.missevan.com"
    override val titleText = "Login your MissEvan account"
    override val cookieRuleGroups = listOf(
        CookieRuleGroup(
            rules = listOf(
                CookieRule(
                    name = "token",
                    pattern = "token=([^;]+)",
                    saveFunction = { context, cookieInfo ->
                        println("token = ${cookieInfo.value}")
                        context.writeMissEvanToken(cookieInfo.value)
                    }
                ),
            ),
            matchOne = false
        )
    )
}