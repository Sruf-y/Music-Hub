package ui.components.BottomSheets.Content

import AppDatabase.PlaylistRelated.Playlist
import ui.components.SheetViewmodel
import SrufComposeComponents.Text
import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aacomposetest.Functions.rename
import com.aacomposetest.ui.theme.SrufTheme
import com.aacomposetest.ui.theme.MyColor
import com.aacomposetest.ui.theme.Theme
import kotlinx.coroutines.launch
import ui.components.CancelConfirm
import ui.components.CustomTextField


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
   
   
   var myText = rememberSaveable { mutableStateOf(playlist?.title?:"") }
   
   
   
   
   
   Column(Modifier) {
      
      
      
      Row(
         modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 30.dp)
         ,
         horizontalArrangement = Arrangement.Center,
         verticalAlignment = Alignment.CenterVertically
      ) {
         
         Spacer(Modifier.weight(0.5f))
         if(false)
            OutlinedTextField(modifier = Modifier
                .weight(3f)
                .fillMaxWidth(),
                value = myText.value,
                label = {
                    Text(text = "Playlist name", color = MyColor.Inactive)
                },
                onValueChange = { myText.value = it },
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedTextColor = Theme.colorScheme().inverseSurface,
                    focusedTextColor = Theme.colorScheme().inverseSurface,
                    focusedContainerColor = Theme.colorScheme().secondary)
            )
         else{
            CustomTextField(
               text = myText as MutableState<String?>,
               onValueChange = {
                  myText.value = it.toString()
               },
               modifier = Modifier.weight(3f).height(100.dp)
                  .border(2.dp, MyColor.TextColor(), RoundedCornerShape(10.dp)).padding(15.dp),
               color = MyColor.TextColor()
            )
         }
         
         Spacer(Modifier.weight(0.5f))
         
      }
      
      CancelConfirm(
         onCancel = {
            scope.launch {
               bottomSheetVM?.dismissCurrent()
            }
         },
         onConfirm = {
            
            if(myText.value.isNotEmpty()){
               if( !(existingPlaylists?.map { it->it.title }?.contains(myText.value)?:false)){
                  val invalidChars = "[\\\\/:*?\"<>]".toRegex()
                  myText.value = myText.value.replace(invalidChars, "_")
                  myText.value = myText.value.replace("|", "\\u2503")
                  
                  
                  val file = playlist?.getFileIfExists(contextRef)
                  
                  if (file != null) {
                     file.rename(myText.value)
                  } else {
                     Playlist(myText.value).save(contextRef)
                  }
                  scope.launch {
                     bottomSheetVM?.dismissCurrent()
                  }
                  
                  
               }else{
                  Toast.makeText(contextRef,"Playlist already exists!", Toast.LENGTH_SHORT).show()
               }
            }
            else{
               Toast.makeText(contextRef,"Title must not be blank!", Toast.LENGTH_SHORT).show()
            }
         }
      )
   }
}