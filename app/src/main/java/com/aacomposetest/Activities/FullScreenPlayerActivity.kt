package com.aacomposetest.Activities

import MyViewModels.SongsViewModel
import Presentation.FullscreenPlayer.FullscreenPlayerPage
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.aacomposetest.Classes.CustomComponentActivity
import com.aacomposetest.MyApplication
import com.aacomposetest.ui.theme.MyColor
import com.aacomposetest.ui.theme.SrufTheme
import ui.components.BottomSheets.renderDefaultBackground

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
            
            renderDefaultBackground()
            
            FullscreenPlayerPage(
               modifier = Modifier
                  .fillMaxSize()
                  .safeDrawingPadding()
            )
            
         }
         
      }
      
   }
   
}