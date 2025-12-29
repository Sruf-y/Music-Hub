package com.aacomposetest.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color

object MyColor{

    @Stable val Pink80 = Color(0xFFEFB8C8)

    @Stable val Purple40 = Color(0xFF6650a4)
    @Stable val PurpleGrey40 = Color(0xFF625b71)
    @Stable val Pink40 = Color(0xFF7D5260)

    @Stable val Transparent = Color(0x00FFFFFF)

    @Stable val Black = Color(0xFF000000)
    @Stable val White = Color(0xFFFFFFFF)

    @Stable private val BackgroundLight = Color(0xFFE5E5E5)
    @Stable private val BackgroundDark = Color(0xFF2B2B2B)


    @Stable val Orange = Color(0xFFFF9900)
    @Stable val Red = Color(0xFFFF0000)
    @Stable val ConfirmBlue = Color(0xFF0088FF)
    @Stable val Blue = Color(0xFF0066FF)
    @Stable val Green = Color(0xFF30DC3E)
    @Stable val Teal = Color(0xFF00FFA6)
    @Stable val SemiTransparentBackground = Color(0x9A4E4E4E)
    @Stable val SemiTransparentDark = Color(0x54000000)
    @Stable val SemiTransparentLight = Color(0x54FFFFFF)
    @Stable val Purple = Color(0xFF7227C7)
    @Stable val SelectedBlue = Color(0xFF55ACFF)
    @Stable val Unselected = Color(0xFF787878)
    @Stable val SeparatorGray = Color(0xFFB6B6B6)
    @Stable val SurfaceDark  = Color(0xFF4B6572)
    @Stable val Magenta = Color(0xFFFF00FF)
    @Stable val WorkInProgress = Magenta
    val Inactive = Color(0xFF565656)

    val Myprimary = Color(0xFF44CBA2)

    val SelectedGold = Color(0xFFEFD345)

    @Composable
    fun SemiTransparent(isDarkMode:Boolean = isSystemInDarkTheme()): Color{
        return if(isDarkMode)
            SemiTransparentDark
        else
            SemiTransparentLight
    }
    @Composable
    fun TextColor(isDarkMode:Boolean = isSystemInDarkTheme()): Color{
        return if(isDarkMode)
            White
        else
            Black
    }

    @Composable
    fun Background(isDarkMode:Boolean = isSystemInDarkTheme()): Color{
        return if(isDarkMode)
            BackgroundDark
        else
            BackgroundLight
    }
}

