package ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ui.tokens.SizeTokens

@Composable
fun Section(title: String, content: @Composable ColumnScope.() -> Unit){
    Column (
        modifier = Modifier.padding(SizeTokens.Level16,0.dp),
        verticalArrangement = Arrangement.spacedBy(SizeTokens.Level8)
    ){
        LableTextLarge(text = title, fontWeight = FontWeight.SemiBold)
        content()
    }
}
@Composable
fun ActionButton(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector,
    onClick: () -> Unit = {},
    content: @Composable RowScope.() -> Unit,
    trainlingIcon: @Composable (RowScope.() -> Unit)? = null
){
    val interactionSource = remember { MutableInteractionSource() }
    Card (
        modifier = modifier.wrapContentHeight(),
        shape = RoundedCornerShape(SizeTokens.Level16),
        onClick = onClick,
        enabled = enabled,
        interactionSource = interactionSource,
    ){
        Row (
            modifier = Modifier.padding(SizeTokens.Level12).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SizeTokens.Level10)
        ){
            Surface(
                modifier = Modifier.size(SizeTokens.Level36),
                shape = CircleShape,
                enabled = enabled,
                onClick = onClick,
                interactionSource = interactionSource
            ) {
                Icon(imageVector = icon, contentDescription = null,modifier = Modifier.size(SizeTokens.Level8))
            }
            content()
            trainlingIcon?.invoke(this)
        }
    }
}
//@Composable
//fun PermissionButton(
//    enabled: Boolean = true,
//    state: EnvState,
//    title: String,
//    description: String,
//    onClick: () -> Unit,
//    onSetting: () -> Unit
//){
//    ActionButton(
//        enabled = enabled,
//        icon = state.icon,
//    )
//}