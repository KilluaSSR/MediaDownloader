package killua.dev.base.Login.Platforms

import killua.dev.base.Login.CookieRule
import killua.dev.base.Login.CookieRuleGroup
import killua.dev.base.Login.PlatformConfig
import killua.dev.base.datastore.writeKuaikanPassToken

class KuaikanConfig : PlatformConfig {
    override val loginUrl = "https://www.kuaikanmanhua.com/webs/loginh"
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