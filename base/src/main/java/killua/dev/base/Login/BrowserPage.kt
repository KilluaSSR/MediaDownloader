package killua.dev.base.Login

import android.annotation.SuppressLint
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import killua.dev.base.Login.Platforms.LofterConfig
import killua.dev.base.Login.Platforms.TwitterConfig
import killua.dev.base.Model.AvailablePlatforms
import killua.dev.base.ui.LocalNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowserPage(
    platform: AvailablePlatforms
) {
    val navController = LocalNavController.current!!
    val loginSuccess = remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val platformConfig = remember {
        when(platform) {
            AvailablePlatforms.Twitter -> TwitterConfig()
            AvailablePlatforms.Lofter -> LofterConfig()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = platformConfig.titleText) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.surface,
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                AndroidView(
                    factory = { context ->
                        WebView(context).apply {
                            settings.apply {
                                javaScriptEnabled = true
                                domStorageEnabled = true
                                javaScriptCanOpenWindowsAutomatically = true
                            }
                            webViewClient = WebViewClient()
                            loadUrl(platformConfig.loginUrl)

                            val cookieManager = CookieManager.getInstance().apply {
                                setAcceptCookie(true)
                            }

                            coroutineScope.launch(Dispatchers.Main) {
                                try {
                                    while (isActive && !loginSuccess.value) {
                                        delay(1000)
                                        val cookies = cookieManager.getCookie(platformConfig.cookieDomain)
                                            ?: continue

                                        for (group in platformConfig.cookieRuleGroups) {
                                            val matchResults = mutableListOf<Pair<CookieRule, String>>()

                                            for (rule in group.rules) {
                                                val matchResult = rule.pattern.toRegex()
                                                    .find(cookies)
                                                if (matchResult != null) {
                                                    // 只获取第一个捕获组的值作为 String
                                                    val matchValue = matchResult.groupValues[1]
                                                    matchResults.add(rule to matchValue)

                                                    // 在调用 saveFunction 时再创建 CookieInfo
                                                    rule.saveFunction(context, CookieInfo(
                                                        key = rule.name,
                                                        value = matchValue
                                                    ))
                                                }
                                            }
                                            val shouldSave = if (group.matchOne) {
                                                matchResults.isNotEmpty()
                                            } else {
                                                matchResults.size == group.rules.size
                                            }
                                            if (shouldSave) {
                                                withContext(Dispatchers.Main) {
                                                    matchResults.forEach { (rule, value) ->
                                                        rule.saveFunction(context, CookieInfo(
                                                            key = rule.name,
                                                            value = value
                                                        ))
                                                    }

                                                    cookieManager.removeAllCookies { success ->
                                                        if (success) cookieManager.flush()
                                                    }

                                                    loginSuccess.value = true
                                                }
                                                break
                                            }
                                        }
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    if (loginSuccess.value) {
        navController.navigateUp()
    }
}