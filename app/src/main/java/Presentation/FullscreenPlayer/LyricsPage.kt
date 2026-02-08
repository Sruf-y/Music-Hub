package Presentation.FullscreenPlayer


import ExoPlayer.myExoPlayer
import MyViewModels.rememberLyricsManager
import SrufComposeComponents.IconButton
import android.app.Activity
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.sp
import com.aacomposetest.ui.theme.MyColor
import com.aacomposetest.ui.theme.SrufTheme
import kotlinx.coroutines.launch
import ui.components.CustomTextField

@Composable
internal fun LyricsPage(modifier: Modifier = Modifier,lyricsTextStyle: TextStyle = TextStyle.Default){
   val contextRef = LocalContext.current
   val scope = rememberCoroutineScope()
   
   val curSong by myExoPlayer.currentlyPlayingSong.collectAsState()
   val lyricsManager = rememberLyricsManager()
   
   var isEditing by remember { mutableStateOf(false) }
   
   LaunchedEffect(curSong) {
      lyricsManager.loadLyricsForSong(curSong?.id,contextRef)
   }
   
   
   Box(modifier = modifier){
      Column(modifier = Modifier.fillMaxSize()) {
         /**
          * Top bar
          * */
         Row(modifier = Modifier.fillMaxWidth()) {
            val buttonsSize = remember { 40.dp }
            
            if(isEditing){
               Spacer(modifier = Modifier.weight(1f))
               // Confirm button
               IconButton(
                  imageVector = Icons.Default.Check,
                  onclick = {
                     isEditing = false
                     
                     if(lyricsManager.lyrics.value !=null){
                        scope.launch {
                           lyricsManager.saveCurrentLyric(contextRef)
                        }
                        
                     }
                  }
               )
               // Close Button
               IconButton(
                  imageVector = Icons.Default.Close,
                  onclick = {
                     isEditing = false
                     scope.launch {
                        lyricsManager.loadLyricsForSong(curSong?.id,contextRef)
                     }
                     
                  }
               )
            }
            else{
               Spacer(modifier = Modifier.weight(1f))
               IconButton(
                  imageVector = Icons.Default.Build,
                  onclick = {
                     isEditing = true
                  }
               )
               IconButton(
                  imageVector = Icons.Default.Settings,
                  onclick = {
                  
                  }
               )
            }
         }
         
         CustomTextField(
            text = lyricsManager.lyrics,
            onValueChange = {
               lyricsManager.lyrics.value = it.toString()
            },
            modifier = Modifier
               .weight(1f)
               .background(MyColor.Red),
            enabled = isEditing
         )
      }
   }
}

