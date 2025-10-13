package com.aacomposetest.Classes.NavigationRelated

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder

data class MyPagerTab(val title:String, val content: @Composable ()-> Unit = {}) {
}