package com.aacomposetest.Classes

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

data class Size(val width: Dp,val height: Dp) {

}

fun Modifier.size(size:Size): Modifier{
    return this.width(size.width).height(size.height)
}