package SrufComposeComponents


import android.health.connect.datatypes.units.Percentage
import android.os.Build
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
fun IconButton(
    @DrawableRes resId: Int,
    modifier: Modifier = Modifier.size(45.dp,45.dp),
    /**Float scalar. Must be between 0f and 1f*/
    iconSize:Size = Size(1f,1f),
    iconTint:Color = if(isSystemInDarkTheme()) Color.White else Color.Black,
    onclick:(()->Unit)? = null
) {

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Button(
            onClick = {
                onclick?.invoke()
            },
            colors = ButtonColors(
                containerColor = Color.Transparent,
                contentColor = Color.White,
                disabledContentColor = Color.Transparent,
                disabledContainerColor = Color.Transparent
            ),
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxSize()
        ) {}

        Icon(
            painter = painterResource(resId),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth(iconSize.width)
                .fillMaxHeight(iconSize.height),
            tint = iconTint
        )
    }


}

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@Composable
fun IconButton(
    imageVector: ImageVector,
    modifier: Modifier = Modifier.size(45.dp,45.dp),
    /**Float scalar. Must be between 0f and 1f*/
    iconSize:Size = Size(1f,1f),
    iconTint:Color = if(isSystemInDarkTheme()) Color.White else Color.Black,
    onclick:(()->Unit)? = null
) {

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Button(
            onClick = {
                onclick?.invoke()
            },
            colors = ButtonColors(
                containerColor = Color.Transparent,
                contentColor = Color.White,
                disabledContentColor = Color.Transparent,
                disabledContainerColor = Color.Transparent
            ),
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxSize()
        ) {}

        Icon(
            imageVector = imageVector,
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth(iconSize.width)
                .fillMaxHeight(iconSize.height)
                .align(Alignment.Center),
            tint = iconTint
        )
    }


}