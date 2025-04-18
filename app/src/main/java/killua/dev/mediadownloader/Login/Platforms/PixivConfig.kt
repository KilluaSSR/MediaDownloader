package killua.dev.mediadownloader.Login.Platforms

import killua.dev.mediadownloader.Login.CookieRule
import killua.dev.mediadownloader.Login.CookieRuleGroup
import killua.dev.mediadownloader.Login.PlatformConfig
import killua.dev.mediadownloader.datastore.writePixivPHPSSID

object PixivLoginChecker {
    fun isValidPHPSESSID(sessionId: String): Boolean {
        return sessionId.contains("_") &&
                sessionId.split("_").size == 2
    }
}
class PixivConfig : PlatformConfig {
    override val loginUrl = "https://accounts.pixiv.net/login"
    override val cookieDomain = "https://pixiv.net"
    override val titleText = "Login your Pixiv account"
    override val cookieRuleGroups = listOf(
        CookieRuleGroup(
            rules = listOf(
                CookieRule(
                    name = "PHPSESSID",
                    pattern = "PHPSESSID=([^;]+)",
                    saveFunction = { context, cookieInfo ->
                        if (PixivLoginChecker.isValidPHPSESSID(cookieInfo.value)) {
                            println("PIXIV = ${cookieInfo.value}")
                            context.writePixivPHPSSID(cookieInfo.value)
                        }
                    }
                ),
            ),
            matchOne = false
        )
    )
}