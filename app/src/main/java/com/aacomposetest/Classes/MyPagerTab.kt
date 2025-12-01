package com.aacomposetest.Classes

import androidx.compose.runtime.Composable

data class MyPagerTab(val title:String,val content:@Composable ()-> Unit) {
}