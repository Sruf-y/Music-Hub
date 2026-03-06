package com.aacomposetest.Classes

import android.content.pm.PackageManager
import android.os.Bundle
import android.os.PersistableBundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.annotation.CallSuper
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.aacomposetest.buildSystemBarsStyle
import com.aacomposetest.ui.theme.MyColor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

open class CustomComponentActivity(): ComponentActivity(){
   /**
    * A scope for each activity for bussiness logic. The default dispatcher is [Dispatchers.Main]
    * */
   val activityScope = CoroutineScope(Dispatchers.Main)
   
   /**
    * Set the keyboard pan mode to [WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING]
    *
    * Called by onCreate's super function
    * */
   private fun setupKeyboardPanMode(){
      window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
   }
   
   fun defaultSetupNavBars(){
      val statusBarStyle = buildSystemBarsStyle(darkMode = MyColor.Transparent.toArgb())
      val navigationBarStyle = buildSystemBarsStyle(darkMode = MyColor.Black.toArgb())
      enableEdgeToEdge(statusBarStyle = statusBarStyle, navigationBarStyle = navigationBarStyle)
   }
   
   @CallSuper
   override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
      super.onCreate(savedInstanceState, persistentState)
      
      setupKeyboardPanMode()
   }
   
   /**
    * @return true if the activity has this permission, false otherwise
    * */
   fun hasPermission(permission:String):Boolean{
      return checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
   }
}