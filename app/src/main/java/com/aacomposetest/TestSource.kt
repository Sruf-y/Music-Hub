package com.aacomposetest

import TestLayouts.NestedScrollTest
import androidx.compose.runtime.Composable
import ui.components.ConnectedSliderGroupPreview

object TestSource {
   
   enum class Sources{
      ConnnectedSlider,
      NestedScrolling
   }
   
   
   
   
   
   
   
   @Composable
   fun TestSwitch(value: TestSource.Sources){
      when(value){
         Sources.ConnnectedSlider -> {
            ConnectedSliderGroupPreview()
         }
         
         Sources.NestedScrolling -> {
            NestedScrollTest()
         }
      }
   }
}