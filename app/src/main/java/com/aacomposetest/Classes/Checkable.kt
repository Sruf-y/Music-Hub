package com.aacomposetest.Classes

import androidx.compose.runtime.mutableStateOf

class Checkable<T>(val item:T,isChecked:Boolean = false) {
    val isChecked = mutableStateOf(isChecked)


}