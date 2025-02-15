package killua.dev.mediadownloader.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import killua.dev.base.datastore.ApplicationUserDataTwitter
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import java.net.CookieManager
import java.net.HttpCookie
import java.net.URI
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ProvideTwitterApiClient {
    @Provides
    @Singleton
    fun buildClient(userData: ApplicationUserDataTwitter): OkHttpClient {
        val cookieManager = CookieManager().apply {
            val ct0Cookie = HttpCookie("ct0", userData.ct0).apply { domain = "x.com" }
            val authCookie =
                HttpCookie("auth_token", userData.auth).apply { domain = "x.com" }
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