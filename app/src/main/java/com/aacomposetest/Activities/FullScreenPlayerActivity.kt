package com.aacomposetest.Activities

import ViewModels.SongsViewModel
import Presentation.FullscreenPlayer.FullscreenPlayerPage
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.ui.Modifier
import com.aacomposetest.Classes.CustomComponentActivity
import com.aacomposetest.MyApplication
import com.aacomposetest.ui.theme.SrufTheme
import com.aacomposetest.ui.theme.Theme

class FullScreenPlayerActivity : CustomComponentActivity(){
   
   val songViewModel: SongsViewModel
      get() {
         return MyApplication.instance.songViewModel
      }
   
   override fun onCreate(savedInstanceState: Bundle?) {
      super.onCreate(savedInstanceState)
      
      defaultSetupNavBars()
      
      setContent {
         SrufTheme {
            
            Theme.RenderDefaultBackground()
            
            FullscreenPlayerPage(
               modifier = Modifier
                  .fillMaxSize()
                  .safeDrawingPadding()
            )
            
         }
         
      }
      
   }
   
}