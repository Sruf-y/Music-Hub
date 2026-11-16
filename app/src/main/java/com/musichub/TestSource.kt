package com.musichub

import ExoPlayer.EqualizerController
import TestLayouts.DragNFlingTest
import TestLayouts.NestedScrollTest
import androidx.compose.runtime.Composable
import ui.components.ConnectedSliderGroupPreview

object TestSource {
   
   enum class Sources{
      ConnnectedSlider,
      NestedScrolling,
      EqualizerBands,
      DragNFling
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
            EqualizerController.BandsTestPreview()
         }
         
         Sources.DragNFling -> {
            DragNFlingTest()
         }
      }
   }
}