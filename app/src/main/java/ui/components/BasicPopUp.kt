package ui.components

import SrufComposeComponents.Experimental.ColorPicker
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Popup
import com.aacomposetest.ui.theme.MyColor

@Composable
fun BasicPopUp(
   popupState: MutableState<Boolean> =
      remember { mutableStateOf(false) },
   content:@Composable ()-> Unit
){
   if(popupState.value) {
      Popup(
         alignment = Alignment.Center,
         onDismissRequest = {
            popupState.value  = false
         }
      ) {
         BackHandler {
            popupState.value = false
         }
         
         content.invoke()
      }
   }
}

@Composable
fun BasicPopUp(
   popupState: Boolean = false,
   onDismissRequest:()-> Unit,
   content:@Composable ()-> Unit
){
   if(popupState) {
      Popup(
         alignment = Alignment.Center,
         onDismissRequest = onDismissRequest
      ) {
         BackHandler {
            onDismissRequest.invoke()
         }
         
         content.invoke()
      }
   }
}
