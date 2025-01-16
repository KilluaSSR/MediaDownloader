package killua.dev.setup

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import api.ExecuteDownload
import api.TwitterAPI
import dagger.hilt.android.AndroidEntryPoint
import killua.dev.setup.MainActivity
import killua.dev.setup.ui.theme.TwitterDownloaderTheme
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import ui.LocalNavController
@ExperimentalAnimationApi
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val context = this
        setContent {
            TwitterDownloaderTheme {
                val navController = rememberNavController()
                CompositionLocalProvider(
                    LocalNavController provides navController,
                    androidx.lifecycle.compose.LocalLifecycleOwner provides LocalLifecycleOwner.current
                ) {
                    Setup()
                }
            }
        }
    }
}

fun main() {
    TwitterAPI(OkHttpClient()).getTweetDetail("1875451750695760028")
}