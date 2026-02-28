package com.aacomposetest.ui.theme

import SrufComposeComponents.Text
import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.graphics.BlendMode
import android.graphics.RuntimeShader
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.aacomposetest.ui.theme.Theme.DarkColorScheme
import com.aacomposetest.ui.theme.Theme.LightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.tooling.preview.Preview
import com.aacomposetest.isDarkMode

object Theme{

   

     var DarkColorScheme = darkColorScheme(
        primary = MyColor.Myprimary,
         secondary = MyColor.Inactive,
         inversePrimary = MyColor.Inactive
     )

     var LightColorScheme = lightColorScheme(
         primary = MyColor.Myprimary
    )

    @Composable
    fun colorScheme(forceDarkMode: Boolean = false) = if(isSystemInDarkTheme() || forceDarkMode) DarkColorScheme else LightColorScheme
   
   
   
}

@Composable
fun SrufTheme(
   forceDarkMode:Boolean = false,
   content: @Composable () -> Unit
) {
   
   
      MaterialTheme(
         colorScheme = Theme.colorScheme(forceDarkMode),
         typography = Typography,
         content = content
      )
   
   
}


