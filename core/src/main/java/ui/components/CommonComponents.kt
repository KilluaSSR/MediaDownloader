package ui.components

import Model.CurrentState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.KeyboardDoubleArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
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
    state: CurrentState,
    trainlingIcon: @Composable (RowScope.() -> Unit)? = null,
    content: @Composable RowScope.() -> Unit,
){
    val interactionSource = remember { MutableInteractionSource() }
    Card (
        modifier = modifier.wrapContentHeight(),
        shape = RoundedCornerShape(SizeTokens.Level16),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = state.backgroundColor,
        ),
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
@Composable
fun PermissionButton(
    enabled: Boolean = true,
    onClick: () -> Unit,
    title: String,
    description: String,
    state: CurrentState,
    onSetting: (()->Unit)? = null
){
    ActionButton(
        enabled = enabled,
        icon = state.leadingIcon,
        onClick = onClick,
        trainlingIcon = {
            if(onSetting != null){
                Surface (
                    modifier = Modifier.size(SizeTokens.Level36),
                    shape = CircleShape,
                    onClick = onSetting
                ){
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = null
                    )
                }
            }
        },
        state = state
    ){
        Column (modifier = Modifier.weight(1f)){
            LableTextLarge(text = title, color = state.textColor)
            BodySmallText(text = description, color = state.textColor)
        }
    }
}
@Composable
fun NavigationButtion(
    onClick: () -> Unit,
    title: String,
    icon: ImageVector
){
    Surface (
        modifier = Modifier
            .width(SizeTokens.Level128)
            .height(SizeTokens.Level36),
        shape = CircleShape,
        onClick = onClick
    ){
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = icon, contentDescription = null)
            Text(text = title)
        }
    }
}

@Preview
@Composable
fun PermissionButtonPreview(){
    NavigationButtion(
        onClick = {},
        title = "Notification",
        icon = Icons.Rounded.KeyboardDoubleArrowRight
    )
}