package ui.components.BottomSheets.Content

import AppDatabase.PlaylistRelated.Playlist
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.aacomposetest.ui.theme.MyColor
import com.aacomposetest.ui.theme.Theme
import kotlinx.coroutines.launch
import ui.components.BottomSheets.Content.BottomSheetContentDefaults.VerticalPadding
import ui.components.CancelConfirm

@Composable
fun PlaylistDelete_Content(playlist: Playlist?, bottomSheetVM: ViewModels.BottomSheet.SheetViewmodel?){
   
   val context = LocalContext.current
   
   val scope = rememberCoroutineScope()
   
   val myTitle = playlist?.title
   
   val question = buildAnnotatedString {
      append("Are you sure you want to ")
      
      withStyle(style = SpanStyle(color = Color.Red, fontWeight = FontWeight.Bold)) {
         append("DELETE")
      }
   }
   
   val playlistTitle = buildAnnotatedString {
      withStyle(style = SpanStyle(color = MyColor.Orange)) {
         append(myTitle)
      }
      append(" ?")
   }
   
   
   Column(
      modifier = Modifier
         .fillMaxWidth()
         .wrapContentHeight()
         .padding(vertical = VerticalPadding),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Bottom
   ) {
      Box(
         modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp), contentAlignment = Alignment.Center
      ) {
         Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = question, color = Theme.colorScheme().inverseSurface)
            Text(text = playlistTitle, color = Theme.colorScheme().inverseSurface)
         }
      }
      CancelConfirm(onCancel = {
         scope.launch {
            bottomSheetVM?.dismissCurrent()
         }
      },
         onConfirm = {
            playlist?.delete(context)
            
            scope.launch {
               bottomSheetVM?.dismissCurrent()
            }
         }
      )
   }
   
   
}