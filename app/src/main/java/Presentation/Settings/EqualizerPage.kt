package Presentation.Settings

import ExoPlayer.myExoPlayer
import SrufComposeComponents.Text
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aacomposetest.Log
import com.aacomposetest.ui.theme.MyColor

@Composable
fun EqualizerPage(modifier: Modifier = Modifier.fillMaxSize()) {
   
   val state = rememberLazyListState()
   
   LaunchedEffect(Unit) {
      Log("Equalizer presets are ${myExoPlayer.equalizerController?.presets?.size}")
   }
   
   LazyColumn(
      state = state,
      modifier = Modifier.fillMaxSize()
   ) {
      
      item {
         myExoPlayer.equalizerController?.currentPreset?.let {
            Text(
               myExoPlayer.equalizerController?.presets?.get(it)?:"nothing",
               modifier = Modifier
               .fillMaxWidth()
               .background(MyColor.Red)
            )
         }
      }
      
      myExoPlayer.equalizerController?.let {
         itemsIndexed(
            items = it.presets,
            key = {
                  index,
                  it,
               ->
               index
            }
         ) { index, name ->
            Box(
               modifier = Modifier
                  .fillMaxWidth()
                  .wrapContentHeight()
                  .clickable { it.changeSettingsToPreset(index) }
                  .padding(10.dp)
            ){
               Text(name)
            }
         }
      }
      
   }
}