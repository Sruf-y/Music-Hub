package com.aacomposetest

import ExoPlayer.EqualizerController2
import TestLayouts.NestedScrollTest
import androidx.compose.runtime.Composable
import ui.components.ConnectedSliderGroupPreview

object TestSource {
   
   enum class Sources{
      ConnnectedSlider,
      NestedScrolling,
      EqualizerBands
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
         
         Sources.EqualizerBands -> {
            EqualizerController2.BandsTestPreview()
         }
      }
   }
}