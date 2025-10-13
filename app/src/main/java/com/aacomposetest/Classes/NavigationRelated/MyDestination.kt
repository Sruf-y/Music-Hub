package com.aacomposetest.Classes.NavigationRelated

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder

data class MyDestination(val name:String,val composable: NavGraphBuilder.()-> Unit){}
