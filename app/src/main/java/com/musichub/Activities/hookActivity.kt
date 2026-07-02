package com.musichub.Activities

import android.app.Activity
import android.os.Bundle
import android.util.Log
import com.musichub.Classes.ActivityLifecycleCallbacks_Open

/**
 * Default Log tag is [ActivityHook]
 * */
fun hookActivity(
   activity:Activity,
   onLifecycleEvent: (String) -> Unit = {
   Log.i("ActivityHook", it)
}){
   val callbacks = object:ActivityLifecycleCallbacks_Open(){
      override fun onActivityResumed(activity: Activity) {
         super.onActivityResumed(activity)
         onLifecycleEvent.invoke("RESUMED")
      }
      
      override fun onActivityPaused(activity: Activity) {
         super.onActivityPaused(activity)
         onLifecycleEvent.invoke("PAUSED")
      }
      
      override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
         super.onActivityCreated(activity, savedInstanceState)
         onLifecycleEvent.invoke("CREATED")
      }
      
      override fun onActivityDestroyed(activity: Activity) {
         super.onActivityDestroyed(activity)
         onLifecycleEvent.invoke("DESTROYED")
      }
      
      override fun onActivityStarted(activity: Activity) {
         super.onActivityStarted(activity)
         onLifecycleEvent.invoke("STARTED")
      }
      
      override fun onActivityStopped(activity: Activity) {
         super.onActivityStopped(activity)
         onLifecycleEvent.invoke("STOPPED")
      }
   }
   
   activity.unregisterActivityLifecycleCallbacks(callbacks)
   
   activity.registerActivityLifecycleCallbacks(callbacks)
}

fun Activity.hookLifecycle(
   onLifecycleEvent: (String) -> Unit = {
      Log.i("DefaultTag", it)
   }
) {
   val callbacks = object:ActivityLifecycleCallbacks_Open(){
      override fun onActivityResumed(activity: Activity) {
         super.onActivityResumed(activity)
         onLifecycleEvent.invoke("RESUMED")
      }
      
      override fun onActivityPaused(activity: Activity) {
         super.onActivityPaused(activity)
         onLifecycleEvent.invoke("PAUSED")
      }
      
      override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
         super.onActivityCreated(activity, savedInstanceState)
         onLifecycleEvent.invoke("CREATED")
      }
      
      override fun onActivityDestroyed(activity: Activity) {
         super.onActivityDestroyed(activity)
         onLifecycleEvent.invoke("DESTROYED")
      }
      
      override fun onActivityStarted(activity: Activity) {
         super.onActivityStarted(activity)
         onLifecycleEvent.invoke("STARTED")
      }
      
      override fun onActivityStopped(activity: Activity) {
         super.onActivityStopped(activity)
         onLifecycleEvent.invoke("STOPPED")
      }
   }
   
   this.unregisterActivityLifecycleCallbacks(callbacks)
   
   this.registerActivityLifecycleCallbacks(callbacks)
}