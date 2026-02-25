package com.aacomposetest.Classes

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.reflect.KProperty

class AutoStateFlow<T>(initialValue:T) {
    private val _stateFlow = MutableStateFlow<T>(initialValue)
    val stateFlow: StateFlow<T> = _stateFlow.asStateFlow()


    fun setValue(value:T){
        _stateFlow.value = value
    }
    
    val getValue :T
        get() {return stateFlow.value}
}