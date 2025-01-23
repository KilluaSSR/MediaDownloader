package killua.dev.twitterdownloader

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import dagger.hilt.android.AndroidEntryPoint
import killua.dev.base.datastore.readApplicationUserAuth
import killua.dev.base.datastore.readApplicationUserCt0
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity : ComponentActivity() {
    @OptIn(ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        installSplashScreen()
        val ct0: String
        val auth: String
        runBlocking{
            ct0 = this@SplashActivity.readApplicationUserCt0().first()
            auth = this@SplashActivity.readApplicationUserAuth().first()
        }
        if(ct0.isNotBlank() && auth.isNotBlank()){
            startActivity(Intent(this, MainActivity::class.java))
        }else{
            startActivity(Intent(this,killua.dev.setup.MainActivity::class.java))
        }
    }
}
