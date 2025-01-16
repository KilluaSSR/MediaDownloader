package api

import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import repository.LoginCredentials
import java.net.CookieManager
import java.net.HttpCookie
import java.net.URI

object TwitterApiClient {
    fun buildClient(credentials: LoginCredentials): OkHttpClient {
        val cookieManager = CookieManager().apply {
            val ct0Cookie = HttpCookie("ct0", credentials.ct0).apply { domain = "x.com" }
            val authCookie = HttpCookie("auth_token", credentials.authToken).apply { domain = "x.com" }
            cookieStore.add(URI("https://x.com"), ct0Cookie)
            cookieStore.add(URI("https://x.com"), authCookie)
        }
        return OkHttpClient.Builder()
            .cookieJar(object : CookieJar {
                override fun loadForRequest(url: HttpUrl): List<Cookie> {
                    return cookieManager.cookieStore.cookies.map {
                        Cookie.Builder()
                            .name(it.name)
                            .value(it.value)
                            .domain(it.domain)
                            .build()
                    }
                }
                override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {}
            })
            .build()
    }
}