package com.aacomposetest.Classes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

class SimpleDropdownMenuState private constructor(initialState:Boolean, val content: @Composable ()-> Unit) {
   var state by mutableStateOf(initialState)
   
   companion object{
      @Composable
      fun rememberSimpleDropdownMenu(initialState:Boolean,content: @Composable ()-> Unit):SimpleDropdownMenuState{
         return remember(content) {
            SimpleDropdownMenuState(initialState,content)
         }
      }
   }
}



class DropdownMenuState private constructor(val onOpen:()->Unit = {},val onDismiss:()->Unit = {},val content: @Composable ()-> Unit = {}){
   companion object{
      @Composable
      fun rememberDropdownMenu(onOpen:()->Unit = {}, onDismiss:()->Unit = {}, content: @Composable ()-> Unit = {}):DropdownMenuState{
         return remember(key1 = onOpen, key2 = onDismiss, key3 = content) {
            DropdownMenuState(onOpen,onDismiss,content)
         }
      }
   }
}