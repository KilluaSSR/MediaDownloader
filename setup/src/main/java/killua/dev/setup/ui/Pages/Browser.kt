package killua.dev.setup.ui.Pages

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
import killua.dev.base.datastore.writeApplicationUserAuth
import killua.dev.base.datastore.writeApplicationUserCt0
import killua.dev.base.ui.LocalNavController
import killua.dev.base.utils.navigateSingle
import killua.dev.setup.SetupRoutes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
//
//@SuppressLint("SuspiciousIndentation", "SetJavaScriptEnabled")
//@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
//@Composable
//fun BrowserPage(){
//    val navController = LocalNavController.current!!
//    val loginSuccess = remember { mutableStateOf(false) }
//    val coroutineScope = rememberCoroutineScope()
//        Scaffold(
//            topBar = {
//                TopAppBar(
//                    title = { Text(text = "Login your twitter account") },
//                    navigationIcon = {
//                        IconButton(
//                            onClick = {
//                                navController.navigateSingle(SetupRoutes.PermissionsPage.route)
//                            }
//                        ) { Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack,null) }
//                    }
//                )
//            },
//            containerColor = MaterialTheme.colorScheme.surface,
//        ) { innerPadding ->
//            Column(
//                modifier = Modifier.padding(innerPadding)
//            ) {
//                Box(
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .background(MaterialTheme.colorScheme.background)
//                ) {
//
//                    AndroidView(
//                        factory = { context ->
//                            WebView(context).apply {
//                                settings.apply {
//                                    javaScriptEnabled = true
//                                    domStorageEnabled = true
//                                    javaScriptCanOpenWindowsAutomatically = true
//                                }
//                                webViewClient = WebViewClient()
//                                loadUrl("https://x.com/i/flow/login")
//                                val cookieManager = CookieManager.getInstance()
//                                cookieManager.setAcceptCookie(true)
//
//                                coroutineScope.launch(Dispatchers.Main) {
//                                    try {
//                                        while (isActive && !loginSuccess.value) {
//                                            delay(1000)
//                                            val cookies = cookieManager.getCookie("https://x.com")
//                                            if (cookies == null) continue
//
//                                            val ct0Regex = "ct0=([^;]+)".toRegex()
//                                            val authRegex = "auth_token=([^;]+)".toRegex()
//                                            val ct0Match = ct0Regex.find(cookies)?.groupValues?.getOrNull(1)
//                                            val authMatch = authRegex.find(cookies)?.groupValues?.getOrNull(1)
//
//                                            if (ct0Match != null && authMatch != null) {
//                                                withContext(Dispatchers.Main) {
//                                                    context.writeApplicationUserCt0(ct0Match)
//                                                    context.writeApplicationUserAuth(authMatch)
//                                                    cookieManager.removeAllCookies { success ->
//                                                        if (success) {
//                                                            cookieManager.flush()
//                                                        }
//                                                    }
//                                                    loginSuccess.value = true
//                                                }
//                                            }
//                                        }
//                                    } catch (e: Exception) {
//                                        e.printStackTrace()
//                                    }
//                                }
//                            }
//                        },
//                        modifier = Modifier.fillMaxSize()
//                    )
//                }
//            }
//        }
//    if (loginSuccess.value) {
//        navController.navigateSingle(SetupRoutes.PermissionsPage.route)
//    }
//
//
//}