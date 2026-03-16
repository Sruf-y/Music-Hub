package ui.components.BottomSheets.Content

import AppDatabase.PlaylistRelated.Playlist
import ui.components.SheetViewmodel
import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aacomposetest.Functions.rename
import com.aacomposetest.ui.theme.SrufTheme
import com.aacomposetest.ui.theme.MyColor
import com.composeunstyled.Text
import kotlinx.coroutines.launch
import ui.components.CancelConfirm
import ui.components.CustomTextField
import ui.components.PriorityBox


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
   
   
   var myText by rememberSaveable { mutableStateOf(playlist?.title?:"") }
   
   
   PriorityBox(
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
      Box(Modifier
         .wrapContentHeight()
         .fillMaxWidth(),
         contentAlignment = Alignment.Center
      ) {
         PriorityBox(
            modifier = Modifier
               .fillMaxWidth(0.75f)
               .padding(vertical = 30.dp),
            bottom = {
               if (myText.isNotEmpty()) {
                  Text("Text is not empty!", color = MyColor.Green)
               }
            }
         ) {
            CustomTextField(
               hint = "Playlist title",
               value = myText ,
               onValueChange = {
                  myText = it.toString()
               },
               modifier = Modifier
                  .wrapContentHeight()
                  .border(2.dp, MyColor.TextColor(), RoundedCornerShape(10.dp))
                  .imePadding(),
               color = MyColor.TextColor(),
               contentPadding = PaddingValues(15.dp)
            )
         }
      }
      
      
   }
}