package com.aacomposetest.Classes

import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.core.content.ContextCompat


fun AppCompatActivity.createPermissionLauncher(
   permission: String,
   onGrant: (() -> Unit)? = null,
   onDeny: (() -> Unit)? = null,
   onFinish: ((Boolean) -> Unit)? = null,
): PermissionLauncher {
   return PermissionLauncher(
      permission = permission,
      launcher = this.registerForActivityResult(
         ActivityResultContracts.RequestPermission()
      ) { result ->
         when (result) {
            true -> {
               onGrant?.invoke()
            }
            
            false -> {
               onDeny?.invoke()
            }
         }
         onFinish?.invoke(result)
      }
   )
}

fun ComponentActivity.createPermissionLauncher(
   permission: String,
   onGrant: (() -> Unit)? = null,
   onDeny: (() -> Unit)? = null,
   onFinish: ((Boolean) -> Unit)? = null,
): PermissionLauncher {
   return PermissionLauncher(
      permission = permission,
      launcher = this.registerForActivityResult(
         contract = ActivityResultContracts.RequestPermission(),
         registry = this.activityResultRegistry
      ) { result ->
         
         when (result) {
            true -> {
               onGrant?.invoke()
            }
            
            false -> {
               onDeny?.invoke()
            }
         }
         onFinish?.invoke(result)
         
      }
      
   )
   
}
   



class PermissionLauncher(val permission:String, private val launcher: ActivityResultLauncher<String>){
   
   fun checkSelf(context:Context) = ContextCompat.checkSelfPermission(context,permission) == PackageManager.PERMISSION_GRANTED
   
   fun launch(){
      launcher.launch(permission)
   }
}

/**
 * For composables to use, requires the Manifest permission parameter on Launch.
 * */
@Composable
fun rememberLauncher(
   onGrant: (() -> Unit)? = null,
   onDeny: (() -> Unit)? = null,
   onFinish: ((Boolean) -> Unit)? = null,
): ManagedActivityResultLauncher<String, Boolean> {
   val launcher =  rememberLauncherForActivityResult(
      contract = ActivityResultContracts.RequestPermission()
   ) { granted ->
      if (granted) {
         onGrant?.invoke()
      } else {
         onDeny?.invoke()
      }
      onFinish?.invoke(granted)
   }
   
   return launcher
}