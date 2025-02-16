package killua.dev.mediadownloader.Login.Platforms

import killua.dev.mediadownloader.Login.CookieRule
import killua.dev.mediadownloader.Login.CookieRuleGroup
import killua.dev.mediadownloader.Login.PlatformConfig
import killua.dev.mediadownloader.datastore.writeKuaikanPassToken

class KuaikanConfig : PlatformConfig {
    override val loginUrl = "https://m.kuaikanmanhua.com/mob/login?"
    override val cookieDomain = "https://kuaikanmanhua.com"
    override val titleText = "Login your Kuaikan account"
    override val cookieRuleGroups = listOf(
        CookieRuleGroup(
            rules = listOf(
                CookieRule(
                    name = "passToken",
                    pattern = "passToken=([^;]+)",
                    saveFunction = { context, cookieInfo ->
                        println("passToken = ${cookieInfo.value}")
                        context.writeKuaikanPassToken(cookieInfo.value)
                    }
                ),
            ),
            matchOne = false
        )
    )
}