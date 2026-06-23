package com.aacomposetest.Services

import android.app.AlertDialog
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import com.aacomposetest.Log
import com.aacomposetest.Logs
import com.aacomposetest.MyApplication
import com.aacomposetest.MyApplication.Companion.showSystemAlert
import kotlin.reflect.KClass

/**
 * Manages the binding with the service in case it fails for whatever reason.
 * */
class ServiceBindManager(
   private val serviceKClass: KClass<out Service>,
   private val context: Context  // Pass context explicitly
) {
   
   private var boundService: Service? = null
   private var isBound = false
   private var retryCount = 0
   private val MAX_RETRIES = 3
   private val retryHandler = Handler(Looper.getMainLooper())
   
   /**
    * Called when service is ready
    * */
   var onServiceReady: ((Service) -> Unit)? = null
   /**
    * Called when service has failed for whatever reason
    * */
   var onServiceFailed: ((String) -> Unit)? = null
   
   private val serviceConnection = object : ServiceConnection {
      
      override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
         Log("Binding Successful", Logs.SERVICE)
         retryCount = 0 // Reset retry count on success
         
         // Store the service based on your binder pattern
         boundService = when (service) {
            is MusicService.MusicBinder -> service.getService()
            else -> null
         }
         
         isBound = true
         boundService?.let { onServiceReady?.invoke(it) }
      }
      
      override fun onServiceDisconnected(name: ComponentName?) {
         Log("Service Disconnected", Logs.SERVICE)
         boundService = null
         isBound = false
         handleServiceDeath("Service died unexpectedly. Rebind?")
      }
      
      override fun onNullBinding(name: ComponentName?) {
         Log("Null Binding Received", Logs.SERVICE)
         boundService = null
         isBound = false
         handleBindingFailure("Service returned null binder. Rebind?")
      }
      
      override fun onBindingDied(name: ComponentName?) {
         Log("Binding Died", Logs.SERVICE)
         boundService = null
         isBound = false
         handleBindingFailure("System binding failed. Rebind?")
      }
   }
   
   fun bind() {
      if (isBound) {
         Log("Already bound to service", Logs.SERVICE)
         boundService?.let { onServiceReady?.invoke(it) }
         return
      }
      
      bindToService()
   }
   /**
    * Try binding the service
    * */
   private fun bindToService() {
      val intent = Intent(context, serviceKClass.java)
      val success = context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
      
      if (!success) {
         Log("bindService() returned false", Logs.SERVICE)
         attemptRebind()
      }
   }
   
   private fun attemptRebind() {
      if (retryCount < MAX_RETRIES) {
         retryCount++
         Log("Attempting rebind #$retryCount", Logs.SERVICE)
         
         // Exponential backoff delay
         val delay = (1000 * retryCount).toLong()
         retryHandler.postDelayed({
            bindToService()
         }, delay)
      } else {
         Log("Max retries reached. Giving up.", Logs.SERVICE)
         showSystemAlert(
            context,
            "Binding Error",
            "Unable to connect to service after $MAX_RETRIES attempts",
            onRetry = { resetAndBind() }
         )
         isBound = false
         onServiceFailed?.invoke("Max retries reached")
      }
   }
   
   private fun resetAndBind() {
      retryCount = 0
      bind()
   }
   
   private fun handleServiceDeath(message: String) {
      showSystemAlert(
         context,
         "Service Died",
         message,
         onRetry = { attemptRebind() }
      )
   }
   
   private fun handleBindingFailure(message: String) {
      showSystemAlert(
         context,
         "Service Error",
         message,
         onRetry = { attemptRebind() }
      )
   }
   
   fun unbind() {
      retryHandler.removeCallbacksAndMessages(null)
      if (isBound) {
         context.unbindService(serviceConnection)
         isBound = false
      }
      boundService = null
      retryCount = 0
   }
   
   fun isReady(): Boolean = isBound && boundService != null
   
   fun getService(): Service? = boundService
   
   private fun showSystemAlert(context: Context, title: String, message: String, onRetry: () -> Unit) {
      // Implement your alert dialog here
      AlertDialog.Builder(context)
         .setTitle(title)
         .setMessage(message)
         .setPositiveButton("Retry") { _, _ -> onRetry() }
         .setNegativeButton("Cancel") { _, _ -> }
         .show()
   }
}