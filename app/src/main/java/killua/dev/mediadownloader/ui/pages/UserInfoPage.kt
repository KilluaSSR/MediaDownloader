package killua.dev.mediadownloader.ui.pages

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import killua.dev.mediadownloader.ui.LocalNavController
import killua.dev.mediadownloader.ui.getRandomColors

@Composable
@ExperimentalFoundationApi
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
fun UserInfoPage() {
    getRandomColors()
    val navController = LocalNavController.current!!
}
