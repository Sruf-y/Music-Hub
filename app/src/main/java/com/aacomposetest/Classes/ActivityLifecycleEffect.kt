package com.aacomposetest.Classes

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.aacomposetest.Log
import io.ktor.util.reflect.instanceOf
import okhttp3.internal.wait

/**
 * @param callbacks use [ActivityLifecycleCallbacks_Impl] for an easier time overriding just the required functions.
 * */
@Composable
inline fun ActivityLifecycleEffect(
   key:Any?,
   callbacks: ActivityLifecycleCallbacks_Impl
){
   val activity = LocalActivity.current
   
   LaunchedEffect(key) {
      activity?.unregisterActivityLifecycleCallbacks(callbacks)
      
      activity?.registerActivityLifecycleCallbacks(callbacks)
   }
}

/**
 * Wrapper implementation made so i won't have to implement everything else besides exactly what i need.
 * */
open class ActivityLifecycleCallbacks_Impl(): Application.ActivityLifecycleCallbacks{
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