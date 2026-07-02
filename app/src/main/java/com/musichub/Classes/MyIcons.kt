package com.musichub.Classes

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

object MyIcons {
    
    val sizeSmall = 35.dp
    val sizeMedium = 45.dp
    val sizeBig = 65.dp
    

    val SrufSign by iconBuilder{
        ImageVector.Builder(
            defaultWidth = 24.dp,  // Standard icon size
            defaultHeight = 24.dp,
            viewportWidth = 50f,   // Viewport for path coordinates
            viewportHeight = 50f
        ).apply {

            path(
                strokeLineWidth = 5f,
                strokeLineCap = StrokeCap.Round,
                stroke = SolidColor(Color.Black)
            ) {
                moveTo(5f,45f)

                lineTo(45f,5f)
                moveTo(45f,5f)

                lineTo(5f,5f)
                moveTo(5f,5f)

                lineTo(45f,45f)
                moveTo(45f,45f)
            }
        }.build()
    }
}

private class iconBuilder(private val iconProvider: () -> ImageVector): ReadOnlyProperty<Any?,ImageVector>{
    var _icon : ImageVector? = null


    override fun getValue(thisRef: Any?, property: KProperty<*>, ): ImageVector {
        return _icon?:iconProvider().also { _icon = it }
    }
}
