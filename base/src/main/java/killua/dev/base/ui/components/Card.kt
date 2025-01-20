package killua.dev.base.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import killua.dev.base.ui.tokens.SizeTokens

@ExperimentalMaterial3Api
@Composable
fun OverviewCard(
    modifier: Modifier = Modifier.Companion,
    title: String,
    icon: ImageVector,
    colorContainer: Color,
    onColorContainer: Color,
    content: @Composable ColumnScope.() -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.Companion
            .fillMaxWidth()
            .wrapContentHeight(),
        colors = CardDefaults.cardColors(colorContainer),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.Companion
                .padding(SizeTokens.Level16)
                .fillMaxWidth(),
            verticalAlignment = Alignment.Companion.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SizeTokens.Level16)
        ) {
            Column(
                modifier = Modifier.Companion.weight(
                    1f
                )
            ) {
                Row(
                    modifier = Modifier.Companion
                        .paddingBottom(SizeTokens.Level8)
                        .height(IntrinsicSize.Min),
                    verticalAlignment = Alignment.Companion.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(
                        SizeTokens.Level6
                    )
                ) {
                    Icon(
                        modifier = Modifier.Companion.intrinsicIcon(),
                        imageVector = icon,
                        tint = onColorContainer,
                        contentDescription = null,
                    )
                    LabelLargeText(
                        text = title,
                        color = onColorContainer,
                        fontWeight = FontWeight.Companion.SemiBold
                    )
                }
                content()
            }
        }
    }
}