package Presentation.FullscreenPlayer


import AppDatabase.SongRelated.Song
import ExoPlayer.myExoPlayer
import MyViewModels.rememberLyricsManager
import SrufComposeComponents.IconButton
import SrufComposeComponents.Text
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.TextField
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.aacomposetest.MyApplication
import com.aacomposetest.R
import com.aacomposetest.Toast
import com.aacomposetest.ui.theme.MyColor
import com.aacomposetest.ui.theme.Theme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ui.components.LoopingText
import ui.components.Text.BasicTextFieldExpanded

@Composable
fun LyricsPage(modifier: Modifier = Modifier,lyricsTextStyle: TextStyle = TextStyle.Default){
   val contextRef = LocalContext.current
   val scope = rememberCoroutineScope()
   
   val curSong by myExoPlayer.currentlyPlayingSong.stateFlow.collectAsState()
   val lyricsManager = rememberLyricsManager()
   
   var isEditing by remember { mutableStateOf(false) }
   
   LaunchedEffect(curSong) {
      lyricsManager.loadLyricsForSong(curSong?.id,contextRef)
   }
   
   BackHandler(isEditing) {
      
      MyApplication.showSystemAlert(
         parentContext = contextRef,
         title = "Unsaved changes!",
         message = "Changes will not be saved upon quiting EDIT.",
         onOkTitle = "Confirm",
         onOk = {
            isEditing = false
            scope.launch {
               lyricsManager.loadLyricsForSong(curSong?.id, contextRef)
            }
         },
         onCancel = {}
      )
   }
   
   
   Box(modifier = modifier){
      
      // Focus requester to automatically start writing
      val focusRequester = remember { FocusRequester() }
      
      Column(modifier = Modifier.fillMaxSize()) {
         /**
          * Top bar
          * */
         Row(modifier = Modifier
            .fillMaxWidth()
            .background(Theme.Sourcesets.ToolbarBackground()),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
         ) {
            
            LoopingText(curSong?.title?: Song.defaultTitle,modifier = Modifier.weight(2f,true))
            
            Row(modifier = Modifier.weight(1f,false)) {
               if (isEditing) {
                  // Confirm button
                  IconButton(
                     imageVector = Icons.Default.Check,
                     onClick = {
                        isEditing = false
                        
                        if (lyricsManager.lyrics.value != null) {
                           scope.launch {
                              lyricsManager.saveCurrentLyric(contextRef)
                           }
                           
                        }
                     }
                  )
                  // Close Button
                  IconButton(
                     imageVector = Icons.Default.Close,
                     onClick = {
                        isEditing = false
                        scope.launch {
                           lyricsManager.loadLyricsForSong(curSong?.id, contextRef)
                        }
                        
                     }
                  )
               } else {
                  IconButton(
                     resId = R.drawable.pen,
                     onClick = {
                        isEditing = true
                        focusRequester.requestFocus()
                     }
                  )
                  //TODO: add options button here <-
               }
            }
         }
         
         val textScroll = rememberScrollState()
         val fontSize = remember { 20.sp }
         
         BasicTextFieldExpanded(
            value = lyricsManager.lyrics.value?:"",
            onValueChange = {
               lyricsManager.lyrics.value = it
            },
            modifier = Modifier
               .focusRequester(focusRequester)
               .weight(1f)
               .fillMaxWidth()
               .verticalScroll(textScroll)
               .padding(horizontal = 5.dp)
               ,
            readOnly = !isEditing,
            textStyle = TextStyle(
               color = MyColor.TextColor(),
               fontSize = fontSize,
               lineHeight = fontSize * 1.3f
            ),
            cursorBrush = SolidColor(Theme.TextColor())
         )
         
        
      }
   }
}




