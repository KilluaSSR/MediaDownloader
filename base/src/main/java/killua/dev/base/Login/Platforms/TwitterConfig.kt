package killua.dev.base.Login.Platforms

import killua.dev.base.Login.CookieRule
import killua.dev.base.Login.CookieRuleGroup
import killua.dev.base.Login.PlatformConfig
import killua.dev.base.datastore.writeApplicationUserAuth
import killua.dev.base.datastore.writeApplicationUserCt0

class TwitterConfig : PlatformConfig {
    override val loginUrl = "https://x.com/i/flow/login"
    override val cookieDomain = "https://x.com"
    override val titleText = "Login your Twitter account"
    override val cookieRuleGroups = listOf(
        CookieRuleGroup(
            rules = listOf(
                CookieRule(
                    name = "ct0",
                    pattern = "ct0=([^;]+)",
                    saveFunction = { context, cookieInfo ->
                        println(cookieInfo.value)
                        context.writeApplicationUserCt0(cookieInfo.value)
                    }
                ),
                CookieRule(
                    name = "auth_token",
                    pattern = "auth_token=([^;]+)",
                    saveFunction = { context, cookieInfo ->
                        println(cookieInfo.value)
                        context.writeApplicationUserAuth(cookieInfo.value)
                    }
                )
            ),
            matchOne = false
        )
    )
}