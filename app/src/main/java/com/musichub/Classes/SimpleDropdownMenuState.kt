package com.musichub.Classes

import SrufComposeComponents.Text
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.musichub.Classes.SimpleDropdownMenuState

class SimpleDropdownMenuState(
   initialState:Boolean,
   private val content: List<Pair<String,()->Unit>>
) {
   var state by mutableStateOf(initialState)
      private set
   
   fun open(){
      state = true
      onOpen.invoke()
   }
   fun close(){
      state = false
      onClose.invoke()
   }
   
   var onClose:()->Unit = {}
   var onOpen:()->Unit = {}
   
   @Composable
   fun RenderMenu(){
      DropdownMenu(
         state,
         onDismissRequest = {
            close()
         }
      ) {
         content.forEach {
            DropdownMenuItem(
               text = {
                  Text(it.first)
               },
               onClick = {
                  it.second.invoke()
                  close()
               }
            )
         }
      }
   }
}
/**
 *
 * */
@Composable
fun rememberSimpleDropdownMenu(
   content: List<Pair<String,()->Unit>>,
   initialState:Boolean = false
):SimpleDropdownMenuState{
   return remember(content) {
      SimpleDropdownMenuState(initialState,content)
   }
}