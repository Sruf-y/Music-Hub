package com.aacomposetest.Classes

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.aacomposetest.Log
import io.ktor.util.reflect.instanceOf
import okhttp3.internal.wait

/**
 * Allows a composable to listen to the lifecycle of the activity and perform actions based on the callback given.
 *
 * For an easier time overriding only the functions you want, extend the [ActivityLifecycleCallbacks_Open] class
 *
 * @param key any key a [LaunchedEffect] could take
 * @param callbacks use [ActivityLifecycleCallbacks_Open] for an easier time overriding just the required functions.
 *
 * @sample com.sruf.licenta.Data.ActivityLifecycleEffect_Preview
 * */

@Composable
fun ActivityLifecycleEffect(
   key:Any?,
   callbacks: Application.ActivityLifecycleCallbacks
){
   val activity = LocalActivity.current
   
   LaunchedEffect(key) {
      
      if(activity == null){
         Log.w("ActivityLifecycleEffect","There is no activity to tie the effect to.")
      }
      
      activity?.unregisterActivityLifecycleCallbacks(callbacks)
      
      activity?.registerActivityLifecycleCallbacks(callbacks)
   }
}

/**
 * Wrapper implementation made so i won't have to implement everything else besides exactly what i need.
 * */
open class ActivityLifecycleCallbacks_Open(): Application.ActivityLifecycleCallbacks{
   override fun onActivityCreated(
      activity: Activity,
      savedInstanceState: Bundle?,
   ) {
   
   }
   
   override fun onActivityStarted(activity: Activity) {
   
   }
   
   override fun onActivityResumed(activity: Activity) {
   
   }
   
   override fun onActivityPaused(activity: Activity) {
   
   }
   
   override fun onActivityStopped(activity: Activity) {
   
   }
   
   override fun onActivitySaveInstanceState(
      activity: Activity,
      outState: Bundle,
   ) {
   
   }
   
   override fun onActivityDestroyed(activity: Activity) {
   
   }
   
   override fun onActivityPostSaveInstanceState(
      activity: Activity,
      outState: Bundle,
   ) {
      super.onActivityPostSaveInstanceState(activity, outState)
   }
   
   override fun onActivityPreCreated(
      activity: Activity,
      savedInstanceState: Bundle?,
   ) {
      super.onActivityPreCreated(activity, savedInstanceState)
   }
   
   override fun onActivityPostCreated(
      activity: Activity,
      savedInstanceState: Bundle?,
   ) {
      super.onActivityPostCreated(activity, savedInstanceState)
   }
   
   override fun onActivityPostStarted(activity: Activity) {
      super.onActivityPostStarted(activity)
   }
   
   override fun onActivityPostResumed(activity: Activity) {
      super.onActivityPostResumed(activity)
   }
   
   override fun onActivityPostPaused(activity: Activity) {
      super.onActivityPostPaused(activity)
   }
   
   override fun onActivityPreStopped(activity: Activity) {
      super.onActivityPreStopped(activity)
   }
   
   override fun onActivityPostStopped(activity: Activity) {
      super.onActivityPostStopped(activity)
   }
   
   override fun onActivityPreSaveInstanceState(
      activity: Activity,
      outState: Bundle,
   ) {
      super.onActivityPreSaveInstanceState(activity, outState)
   }
   
   override fun onActivityPostDestroyed(activity: Activity) {
      super.onActivityPostDestroyed(activity)
   }
   
   override fun onActivityPreDestroyed(activity: Activity) {
      super.onActivityPreDestroyed(activity)
   }
   
   override fun onActivityPrePaused(activity: Activity) {
      super.onActivityPrePaused(activity)
   }
   
   override fun onActivityPreResumed(activity: Activity) {
      super.onActivityPreResumed(activity)
   }
   
   override fun onActivityPreStarted(activity: Activity) {
      super.onActivityPreStarted(activity)
   }
}


@Composable
private fun ActivityLifecycleEffect_Preview(){
   
   var isActivityInUse by remember { mutableStateOf(true) }
   
   ActivityLifecycleEffect(
      key = Unit,
      callbacks = object : ActivityLifecycleCallbacks_Open(){
         
         override fun onActivityResumed(activity: Activity) {
            super.onActivityResumed(activity)
            isActivityInUse = true
         }
         
         override fun onActivityPaused(activity: Activity) {
            super.onActivityPaused(activity)
            isActivityInUse = false
         }
         
      }
   )
}