package SrufComposeComponents


import android.os.Build
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Indication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.musichub.ui.theme.Theme

@Composable
fun IconButton(
   @DrawableRes resId: Int,
   modifier: Modifier = IconButtonDefaults.modifier,
   /**Float scalar. Must be between 0f and 1f*/
    iconScale:Size = IconButtonDefaults.iconSize,
   tint:Color = IconButtonDefaults.iconTint(),
   ripple:Indication? = IconButtonDefaults.ripple(),
   enabled:Boolean = true,
   onClick:(()->Unit)
) {
    _baseIconButton(
        data = resId,
        modifier = modifier,
        iconSize= iconScale,
        iconTint = tint,
        ripple = ripple,
        enabled = enabled,
        onclick = onClick
    )
}

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@Composable
fun IconButton(
    imageVector: ImageVector,
    modifier: Modifier = IconButtonDefaults.modifier,
    /**Float scalar. Must be between 0f and 1f*/
    iconSize:Size = IconButtonDefaults.iconSize,
    tint:Color = IconButtonDefaults.iconTint(),
    ripple:Indication? = IconButtonDefaults.ripple(),
    enabled:Boolean = true,
    onClick:()->Unit
) {
    _baseIconButton(
        data = imageVector,
        modifier = modifier,
        iconSize= iconSize,
        iconTint = tint,
        ripple = ripple,
        enabled = enabled,
        onclick = onClick
    )
}

@Composable
private fun _baseIconButton(
    data: Any,
    modifier: Modifier,
    /**Float scalar. Must be between 0f and 1f*/
    iconSize: Size,
    iconTint: Color,
    ripple: Indication?,
    enabled: Boolean,
    onclick: (() -> Unit),
) {
    Box(
        modifier = modifier.run {
                if (enabled) {
                    clickable(
                        onClick = {
                            onclick.invoke()
                        }, indication = ripple, // This adds the ripple effect
                        interactionSource = remember { MutableInteractionSource() })
                } else {
                    this
                }
            }, contentAlignment = Alignment.Center
    ) {
       when (data) {
          is ImageVector -> {
             Icon(
                imageVector = data,
                contentDescription = null,
                modifier = Modifier
                   .fillMaxWidth(iconSize.width)
                   .fillMaxHeight(iconSize.height)
                   .align(Alignment.Center),
                tint = iconTint
             )
          }
          
          is Int -> {
             Icon(
                painter = painterResource(data),
                contentDescription = null,
                modifier = Modifier
                   .fillMaxWidth(iconSize.width)
                   .fillMaxHeight(iconSize.height),
                tint = iconTint
             )
          }
       }
        
    }
}

object IconButtonDefaults{

    val modifier = Modifier.size(45.dp,45.dp)

    val iconSize:Size = Size(1f,1f)
    
    @Composable
    fun rippleColor() = Theme.IconTint()
    
    @Composable
    fun ripple(): Indication{
        return ripple(bounded = false,color = rippleColor())
    }

    @Composable
    fun iconTint():Color = Theme.IconTint()
}