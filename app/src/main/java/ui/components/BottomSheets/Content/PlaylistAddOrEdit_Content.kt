package ui.components.BottomSheets.Content

import AppDatabase.PlaylistRelated.Playlist
import MyViewModels.SheetViewmodel
import SrufComposeComponents.Text
import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aacomposetest.Functions.rename
import com.aacomposetest.ui.theme.SrufTheme
import com.aacomposetest.ui.theme.MyColor
import kotlinx.coroutines.launch
import ui.components.BottomSheets.Content.BottomSheetContentDefaults.VerticalPadding
import ui.components.CancelConfirm
import ui.components.Text.BasicTextFieldExpanded
import ui.components.PriorityLayout
import ui.components.Text.OutlinedTextFieldExpanded


@Preview(
   showBackground = true,
   uiMode = Configuration.UI_MODE_NIGHT_YES,
   name = "Dark Mode"
)
@Composable
private fun Preview(){
   SrufTheme {
      Box(
         modifier = Modifier
            .fillMaxSize()
            .background(MyColor.Black),
         contentAlignment = Alignment.BottomCenter
      ) {
         PlaylistAddOrEdit_Content()
      }
      
      
   }
}

@Composable
fun PlaylistAddOrEdit_Content(
   existingPlaylists: List<Playlist>? = null,
   bottomSheetVM: SheetViewmodel? = null,
   playlist: Playlist? = null
) {
   val contextRef = LocalContext.current
   val scope = rememberCoroutineScope()
   
   var myText by rememberSaveable { mutableStateOf(playlist?.title ?: "") }
   
   PriorityLayout(
      modifier = Modifier.fillMaxWidth()
         .wrapContentHeight()
         .padding(vertical = VerticalPadding),
      bottom = {
         CancelConfirm(
            onCancel = {
               scope.launch {
                  bottomSheetVM?.dismissCurrent()
               }
            },
            onConfirm = {
               
               if (myText.isNotEmpty()) {
                  if (!(existingPlaylists?.map { it -> it.title }?.contains(myText)
                        ?: false)
                  ) {
                     val invalidChars = "[\\\\/:*?\"<>]".toRegex()
                     myText = myText.replace(invalidChars, "_")
                     myText = myText.replace("|", "\\u2503")
                     
                     
                     val file = playlist?.getFileIfExists(contextRef)
                     
                     if (file != null) {
                        file.rename(myText)
                     } else {
                        Playlist(myText).save(contextRef)
                     }
                     scope.launch {
                        bottomSheetVM?.dismissCurrent()
                     }
                     
                     
                  } else {
                     Toast.makeText(contextRef, "Playlist already exists!", Toast.LENGTH_SHORT)
                        .show()
                  }
               } else {
                  Toast.makeText(contextRef, "Title must not be blank!", Toast.LENGTH_SHORT).show()
               }
            }
         )
   }
   ) {
      var hasBegunWritting by remember { mutableStateOf(false) }
      Box(Modifier
         .wrapContentHeight()
         .fillMaxWidth(0.75f)
         .padding(bottom = VerticalPadding),
         contentAlignment = Alignment.Center
      ){
         val errorText by remember {
            derivedStateOf {
               if (myText.isEmpty() && hasBegunWritting) {
                  "Title cannot be blank!"
               } else if(existingPlaylists?.map { it.title }?.contains(myText) == true){
                  "Playlist already exists!"
               }else{
                  null
               }
            }
         }
         
         PriorityLayout(
            bottom = {
               errorText?.let { Text(it, color = MyColor.Red) }
            }
            ) {
            OutlinedTextFieldExpanded(
               value = myText,
               onValueChange = {
                  myText = it
                  if(!hasBegunWritting){
                     hasBegunWritting = true
                  }
               },
               textStyle = TextStyle(color = MyColor.TextColor()),
               isError = errorText!=null,
               placeholder = {
                  Text("Title",color = MyColor.Inactive)
               }
            )
         }
      }
      
      
      
   }
   
      
      
   
         
      
      
   
   
   
   
}