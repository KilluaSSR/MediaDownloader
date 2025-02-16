package killua.dev.mediadownloader.Setup

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.CompositionLocalProvider
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import killua.dev.mediadownloader.Setup.ui.theme.MediaDownloaderTheme
import killua.dev.mediadownloader.ui.LocalNavController

@ExperimentalAnimationApi
@AndroidEntryPoint
class SetupActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        this
        setContent {
            MediaDownloaderTheme {
                val navController = rememberNavController()
                CompositionLocalProvider(
                    LocalNavController provides navController,
                    LocalLifecycleOwner provides LocalLifecycleOwner.current
                ) {
                    Setup()
                }
            }
        }
    }
}