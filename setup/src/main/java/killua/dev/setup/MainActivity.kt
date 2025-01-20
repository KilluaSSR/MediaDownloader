package killua.dev.setup

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.CompositionLocalProvider
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import killua.dev.setup.ui.theme.TwitterDownloaderTheme
import killua.dev.twitterdownloader.ui.LocalNavController

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
                    killua.dev.twitterdownloader.ui.LocalNavController provides navController,
                    androidx.lifecycle.compose.LocalLifecycleOwner provides LocalLifecycleOwner.current
                ) {
                    Setup()
                }
            }
        }
    }
}