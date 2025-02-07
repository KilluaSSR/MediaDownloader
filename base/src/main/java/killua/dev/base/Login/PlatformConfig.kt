package killua.dev.base.Login

interface PlatformConfig {
    val loginUrl: String
    val cookieDomain: String
    val titleText: String
    val cookieRuleGroups: List<CookieRuleGroup>
}