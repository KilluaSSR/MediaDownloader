package killua.dev.base.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import killua.dev.base.Model.AvailablePlatforms
import killua.dev.base.R
import killua.dev.base.ui.tokens.SizeTokens

@Composable
fun AppIcon(platforms: AvailablePlatforms, modifier: Modifier = Modifier) {
    val icon = when(platforms){
        AvailablePlatforms.Twitter -> R.drawable.logo_of_twitter
        AvailablePlatforms.Lofter -> R.drawable.lofter_logo
    }
    val sizeTokens = when(platforms){
        AvailablePlatforms.Twitter -> SizeTokens.Level100
        AvailablePlatforms.Lofter -> SizeTokens.Level152
    }
    Box(
        modifier = modifier
            .size(SizeTokens.Level152)
            .clip(CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Image(
            modifier = Modifier.size(sizeTokens),
            imageVector = ImageVector.vectorResource(id = icon),
            contentDescription = null
        )
    }
}
