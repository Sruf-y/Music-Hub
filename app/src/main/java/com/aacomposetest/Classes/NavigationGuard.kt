package com.aacomposetest.Classes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.SaveableStateHolder
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.LocalTime

class NavigationGuard(val debounceMs:Long = 500) {
    var isNavigating = false
        private set
    var lastNavigationTime = System.currentTimeMillis().minus(1000)

    private var scope = CoroutineScope(Dispatchers.Main)


    fun use(operation:()-> Unit = {}){

        if(System.currentTimeMillis()-lastNavigationTime>debounceMs){
            isNavigating = true
            scope.launch {
                operation.invoke()
            }.invokeOnCompletion {
                isNavigating = false
            }
        }
    }
}

@Composable
fun rememberNavigationGuard(debounceMs:Long = 500): NavigationGuard{

    return remember() { NavigationGuard(debounceMs) }
}