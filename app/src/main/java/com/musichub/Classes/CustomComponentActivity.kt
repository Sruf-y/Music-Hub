package com.musichub.Classes

import ExoPlayer.myExoPlayer
import Settings.MySettings
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.PersistableBundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.annotation.CallSuper
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.musichub.Log
import com.musichub.buildSystemBarsStyle
import com.musichub.ui.theme.MyColor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
   
   // The call that us usually used
   @CallSuper
   override fun onCreate(savedInstanceState: Bundle?) {
      super.onCreate(savedInstanceState)
      
      _defaultOnCreate()
   }
   
   // WARNING!! This call is not used often but its best to override it too.
   @CallSuper
   override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
      super.onCreate(savedInstanceState, persistentState)
      
      _defaultOnCreate()
   }
   
   @CallSuper
   override fun onStop() {
      super.onStop()
      myExoPlayer.onSaveLogic()
   }
   
   /**
    * Starts watching the orientation flow and sets the IME input pan mode.
    * */
   private var orientationJob:Job? = null
   private fun _defaultOnCreate(){
      orientationJob?.cancel()
      orientationJob = null
      
      MySettings.initialize(this@CustomComponentActivity)
      lifecycleScope.launch {
         // Wait for settings to be initialized
         while (MySettings.settings == null) {
            delay(50)
         }
         
         // Now safe to collect
         repeatOnLifecycle(Lifecycle.State.STARTED) {
            MySettings.settings!!.orientation.stateFlow.collect { value ->
               this@CustomComponentActivity.requestedOrientation = value
            }
         }
      }
      
      setupKeyboardPanMode()
   }
}