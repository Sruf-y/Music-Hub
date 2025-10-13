package com.aacomposetest.Classes

import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.core.app.ActivityOptionsCompat

class MyPermissionLauncher {


    private var onGrant:()->Unit = {}
    private var onDeny:()->Unit = {}
    private var onFinish:(success: Boolean)->Unit = {}

    private var launcher:ManagedActivityResultLauncher<String, Boolean>? = null



    @Composable
    fun buildChange(onGrant:(()->Unit)?= {}, onDeny:(()-> Unit)? = null, onFinish: ((success:Boolean) -> Unit)? = null): MyPermissionLauncher {
        if(onGrant!=null){
            this.onGrant = onGrant
        }
        if(onDeny!=null){
            this.onDeny = onDeny
        }
        if(onFinish!=null){
            this.onFinish = onFinish
        }

        buildLauncher()

        return this
    }

    @Composable
    private fun buildLauncher(){
        launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) {
                // Permission granted, safe to query MediaStore
                this.onGrant.invoke()
            } else {
                // Handle denied case
                this.onDeny.invoke()
            }
            this.onFinish.invoke(granted)
        }
    }



    fun launch(input:String, options: ActivityOptionsCompat? = null){
        launcher?.launch(input,options)
    }


}