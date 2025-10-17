package com.aacomposetest.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.aacomposetest.ui.theme.Theme.DarkColorScheme
import com.aacomposetest.ui.theme.Theme.LightColorScheme


object Theme{



     val DarkColorScheme = darkColorScheme(
        primary = MyColor.Myprimary,
         secondary = MyColor.Inactive,
         inversePrimary = MyColor.Inactive
     )

     val LightColorScheme = lightColorScheme(
         primary = MyColor.Myprimary
    )

    @Composable
    fun colorScheme() = if(isSystemInDarkTheme()) DarkColorScheme else LightColorScheme
}




@Composable
fun ComposeTestTheme(forceDarkMode:Boolean = false,
    content: @Composable () -> Unit
) {

    MaterialTheme(
        colorScheme = if(isSystemInDarkTheme() || forceDarkMode) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content
    )
}