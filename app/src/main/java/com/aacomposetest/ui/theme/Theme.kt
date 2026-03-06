package com.aacomposetest.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aacomposetest.ui.theme.MyColor.Black
import com.aacomposetest.ui.theme.MyColor.White
import com.valentinilk.shimmer.LocalShimmerTheme
import com.valentinilk.shimmer.defaultShimmerTheme
import com.valentinilk.shimmer.shimmer
import ui.components.BottomSheets.renderDefaultBackground
import ui.components.SongItem

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
   
   
   
   val confirmBlue = MyColor.ConfirmBlue
   val confirmGreen = MyColor.ConfirmGreen
   val cancel = MyColor.Inactive
   
   @Composable
   fun background() = MyColor.Background()
   
   @Composable
   fun onBackground() = MyColor.OnBackground()
   
   
   
   @Composable
   fun IconTint():Color = MyColor.IconTint()
   
   @Composable
   fun SemiTransparent():Color = MyColor.SemiTransparent()
   
   @Composable
   fun TextColor():Color = MyColor.TextColor()
   @Composable
   fun HintColor():Color = MyColor.TextColor().copy(alpha = 0.5f)
   
   @Composable
   fun SystemColor():Color = MyColor.SystemColor()
   
   
   
   @Composable
   fun DefaultBackground():Int{
      return remember{ com.aacomposetest.R.drawable.gradient1 }
   }
   
   @Composable
   fun ErrorImage():Int{
      return remember{ com.aacomposetest.R.drawable.exclamation_sign }
   }
   
   @Composable
   fun ToolBar(darkMode:Boolean = isSystemInDarkTheme()):Color{
      return if(darkMode){
         MyColor.Inactive
      }else{
         MyColor.SeparatorGray
      }
   }
   
   object Glint{
      /**
       * Used to hold previews.
       * */
      @Composable
      fun GlintBox(
         modifier: Modifier = Modifier,
         content: @Composable () -> Unit = {}
      ){
         Box(
            modifier = modifier
               .shimmer()
               .background(MyColor.eTransparent)
         ){
            content.invoke()
         }
      }
      /**
       * Used to replace content in previews.
       * */
      @Composable
      fun GlintMaskBox(modifier:Modifier = Modifier.size(45.dp)){
         Box(
            modifier = modifier.background(GlintMaskColor())
         )
      }
      // Returns just the glint color
      @Composable
      fun GlintMaskColor(isDarkMode:Boolean = isSystemInDarkTheme()): Color{
         return if(isDarkMode)
            Color.DarkGray
         else
            Color.LightGray
      }
   }
   
   var Sourcesets by mutableStateOf(object:ThemeSourceSet(){})
      private set
   
   
   
   
   
}

abstract class ThemeSourceSet{
   @Composable
   open fun SongsBackground():Color = Theme.background().copy(alpha = 0.4f)
   
   @Composable
   open fun TabRowBackground():Color  = Theme.SystemColor().copy(alpha = 0.4f)
   
   @Composable
   open fun ToolbarBackground():Color = Theme.ToolBar()
   
   @Composable
   open fun AppbarBackground():Color = ToolbarBackground().copy(alpha = 0.4f)
   
   open val thumb:Color = MyColor.White
   open val seekBar:Color = MyColor.ConfirmBlue
   open val progressBar:Color = MyColor.Green
   open val barBackground:Color = MyColor.SeparatorGray
}


val myShimmerTheme = defaultShimmerTheme.copy(
   // Rotatie in grade
   rotation = 15f
)
@Composable
fun SrufTheme(
   forceDarkMode:Boolean = false,
   content: @Composable () -> Unit
) {
   
   CompositionLocalProvider(
      LocalShimmerTheme provides myShimmerTheme
   ) {
      
      MaterialTheme(
         colorScheme = Theme.colorScheme(forceDarkMode),
         typography = Typography,
         content = content
      )
   }
   
}

@Composable
@Preview
private fun Preview(){
   SrufTheme {
         Box(
            modifier = Modifier
               .shimmer()
               .background(MyColor.eTransparent),
            contentAlignment = Alignment.Center
         ) {
            SongItem()
         }
      
      
   }
}
