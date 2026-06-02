package com.aacomposetest

import MyViewModels.SongsViewModel
import ExoPlayer.myExoPlayer
import SrufComposeComponents.ElevatedButton
import SrufComposeComponents.Text
import TestLayouts.TestActivity
import android.database.ContentObserver
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import android.Manifest
import android.content.Intent
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewModelScope
import com.aacomposetest.Activities.MainActivityView
import com.aacomposetest.ui.theme.SrufTheme
import kotlinx.coroutines.launch

class TestEnviromentActivity_OLD : ComponentActivity() {

    private  var mediastoreObserver: ContentObserver? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val contextRef = this

        val songViewModel: SongsViewModel  = SongsViewModel(this)


        mediastoreObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                super.onChange(selfChange)
                // Trigger a database update here

                songViewModel.viewModelScope.launch {
                    songViewModel.resyncALL_withMediaStore(context = contextRef, alsoPictures = true,forceUpdate = false)
                }

            }
        }
        mediastoreObserver?.apply {
            contentResolver.registerContentObserver(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                true, // notify for descendants
                this
            )
        }


        myExoPlayer.initializePlayer(this)




        setContent {

            MainScreen(songViewModel)

        }
    }

    override fun onDestroy() {
        mediastoreObserver?.apply {
            contentResolver.unregisterContentObserver(this)
        }


        super.onDestroy()
    }
}



@Composable
fun MainScreen(viewModel: SongsViewModel?=null) {
    val context = LocalContext.current

    // Create the permission launcher to be called later. The request can only be shown up to 2 times, after
    // which, the user has to manually go an turn on the permission
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            // Permission granted, safe to query MediaStore
            viewModel?.viewModelScope?.launch {
                viewModel.resyncALL_withMediaStore(context = context, alsoPictures = true, forceUpdate = true)
            }
        } else {
            // Handle denied case
            Toast.makeText(context, "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }





    SrufTheme {

        val context = LocalContext.current

        Surface(modifier = Modifier.fillMaxSize()) {

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {


                ElevatedButton(onClick = {
                    // Launch permission dialogue when clicked
                    permissionLauncher.launch(MyApplication.Permissions.READ_MEDIA_AUDIO)
                }) {
                    Text("Request Audio Permission")
                }



                ElevatedButton(onClick = {

                    val intent = Intent(context, MainActivityView::class.java)

                    context.startActivity(intent)
                }) {
                    Text("See songs list")
                }

                ElevatedButton(onClick = {

                    val intent = Intent(context, TestActivity::class.java)

                    context.startActivity(intent)
                }) {
                    Text("Test navigation")
                }

                

            }
        }
    }
}