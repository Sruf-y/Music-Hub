package com.aacomposetest.ui.theme

import SrufComposeComponents.Text
import android.app.Activity
import android.content.Context
import android.content.res.Configuration
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
import androidx.compose.ui.tooling.preview.Preview
import com.aacomposetest.isDarkMode

object Theme{

   // TODO Create logic for loading and storing themes in the Application class
   private fun storeThemes(){}
   private fun loadThemes(){}
   
  

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
   
   
   
   
   internal var colorChangedBroadcaster by mutableIntStateOf(0)
   
   private fun _changeColor(isDarkMode:Boolean,colorScheme: ColorScheme){
      if(isDarkMode){
         DarkColorScheme = colorScheme
      }else{
         LightColorScheme = colorScheme
      }
      colorChangedBroadcaster = (colorChangedBroadcaster+1)%128
      
   }
   
   fun changeColor(context:Context,colorScheme: ColorScheme){
      _changeColor(context.isDarkMode(),colorScheme)
   }
   
   @Composable
   fun changeColor(colorScheme: ColorScheme){
      _changeColor(isSystemInDarkTheme(),colorScheme)
   }
}

@Composable
fun SrufTheme(
   forceDarkMode:Boolean = false,
   content: @Composable () -> Unit
) {
   
   key(Theme.colorChangedBroadcaster) {
      MaterialTheme(
         colorScheme = Theme.colorScheme(forceDarkMode),
         typography = Typography,
         content = content
      )
   }
   
}

