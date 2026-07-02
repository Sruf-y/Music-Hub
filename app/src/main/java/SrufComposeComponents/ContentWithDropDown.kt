package SrufComposeComponents

import androidx.compose.foundation.background
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import com.musichub.ui.theme.MyColor

@Composable
fun withDropDown(
   modifier: Modifier = Modifier.background(MyColor.Background()),
    state: MutableState<Boolean> = mutableStateOf(false),
    actions: List<Pair<String,()-> Unit>>,
) {
   // The dropdown menu render
   DropdownMenu(
      modifier = modifier,
      expanded = state.value,
      onDismissRequest = { state.value = false }
   ) {
      
      actions.forEach {
         DropdownMenuItem(
            text = {
               Text(it.first)
            }, onClick = {
               it.second.invoke()
            }
         )
      }
   }
    
}

@Composable
fun withCustomDropdown(
   modifier: Modifier = Modifier.background(MyColor.Background()),
   state: MutableState<Boolean> = mutableStateOf(false),
   actions: List<@Composable ()-> Unit>
){
   // The dropdown menu render
   DropdownMenu(
      modifier = modifier,
      expanded = state.value,
      onDismissRequest = { state.value = false }
   ) {
      
      actions.forEach {
         it.invoke()
      }
   }
}