package killua.dev.twitterdownloader.api

import com.google.gson.GsonBuilder
import killua.dev.twitterdownloader.Model.NetworkResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.CookieManager
import java.net.HttpCookie
import java.net.URI
import java.util.concurrent.TimeUnit

object NetworkHelper {
    private val cookieManager = CookieManager()
    private val gson = GsonBuilder().setPrettyPrinting().create()

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .cookieJar(object : CookieJar {
                override fun loadForRequest(url: HttpUrl): List<Cookie> {
                    return cookieManager.cookieStore.get(url.toURI()).map { cookie ->
                        Cookie.Builder()
                            .name(cookie.name)
                            .value(cookie.value)
                            .domain(cookie.domain)
                            .build()
                    }
                }

                override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
                    cookies.forEach { cookie ->
                        cookieManager.cookieStore.add(
                            url.toURI(),
                            HttpCookie(cookie.name, cookie.value).apply {
                                domain = cookie.domain
                            }
                        )
                    }
                }
            })
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                var request = chain.request()
                var response = chain.proceed(request)
                var tryCount = 0
                while (!response.isSuccessful && tryCount < 3) {
                    tryCount++
                    response.close()
                    response = chain.proceed(request)
                }
                response
            }
            .build()
    }

    suspend fun <T> get(
        url: String,
        headers: Map<String, String> = emptyMap(),
        cookies: Map<String, String> = emptyMap(),
        parser: (ByteArray) -> T
    ): NetworkResult<T> = withContext(Dispatchers.IO) {
        executeRequest(
            Request.Builder()
                .url(url)
                .apply {
                    headers.forEach { (k, v) -> addHeader(k, v) }
                }
                .get()
                .build(),
            cookies,
            parser
        )
    }

    suspend fun <T> post(
        url: String,
        body: Any,
        headers: Map<String, String> = emptyMap(),
        cookies: Map<String, String> = emptyMap(),
        parser: (ByteArray) -> T  // 改为接收ByteArray
    ): NetworkResult<T> = withContext(Dispatchers.IO) {
        executeRequest(
            Request.Builder()
                .url(url)
                .apply {
                    headers.forEach { (k, v) -> addHeader(k, v) }
                }
                .post(
                    when (body) {
                        is FormBody -> body
                        is String -> body.toRequestBody("application/json".toMediaType())
                        else -> gson.toJson(body).toRequestBody("application/json".toMediaType())
                    }
                )
                .build(),
            cookies,
            parser
        )
    }

    fun doRequest(request: Request): Response {
        request.header("Cookie")?.split(";")?.forEach {
            val parts = it.trim().split("=")
            if (parts.size == 2) {
                setCookies(request.url.host, mapOf(parts[0] to parts[1]))
            }
        }

        return client.newCall(request).execute()
    }

    private fun <T> executeRequest(
        request: Request,
        cookies: Map<String, String>,
        parser: (ByteArray) -> T
    ): NetworkResult<T> {
        return try {
            cookies.forEach { (k, v) ->
                setCookies(request.url.host, mapOf(k to v))
            }

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.bytes() ?: ByteArray(0)
                    NetworkResult.Success(parser(body))
                } else {
                    NetworkResult.Error(
                        code = response.code,
                        message = "请求失败: ${response.message}"
                    )
                }
            }
        } catch (e: Exception) {
            NetworkResult.Error(message = e.message ?: "未知错误")
        }
    }

    fun setCookies(domain: String, cookies: Map<String, String>) {
        val uri = URI("https://$domain")
        cookies.forEach { (name, value) ->
            cookieManager.cookieStore.add(uri, HttpCookie(name, value).apply {
                this.domain = domain
            })
        }
    }

    private fun HttpUrl.toURI(): URI = URI(this.toString())
}