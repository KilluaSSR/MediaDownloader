package killua.dev.mediadownloader.Login

interface PlatformConfig {
    val loginUrl: String
    val cookieDomain: String
    val titleText: String
    val cookieRuleGroups: List<CookieRuleGroup>
}