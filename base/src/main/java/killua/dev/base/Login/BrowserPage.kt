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

                                        // 检查每个规则组
                                        for (group in platformConfig.cookieRuleGroups) {
                                            var matchedRules = 0

                                            // 遍历组内规则
                                            for (rule in group.rules) {
                                                val matchResult = rule.pattern.toRegex().find(cookies)
                                                if (matchResult != null) {
                                                    val cookieInfo = CookieInfo(
                                                        key = matchResult.groupValues.getOrElse(1) { "" },
                                                        value = matchResult.groupValues.getOrElse(2) { "" },
                                                        domain = matchResult.groupValues.getOrElse(3) { null },
                                                        expiration = matchResult.groupValues.getOrElse(4) { null }
                                                    )

                                                    rule.saveFunction(context, cookieInfo)
                                                    matchedRules++

                                                    // 如果是matchOne模式且已匹配，直接结束
                                                    if (group.matchOne) {
                                                        loginSuccess.value = true
                                                        break
                                                    }
                                                }
                                            }

                                            // 非matchOne模式需要所有规则都匹配
                                            if (!group.matchOne && matchedRules == group.rules.size) {
                                                loginSuccess.value = true
                                            }
                                        }

                                        // 如果登录成功，清理cookie
                                        if (loginSuccess.value) {
                                            cookieManager.removeAllCookies { success ->
                                                if (success) cookieManager.flush()
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