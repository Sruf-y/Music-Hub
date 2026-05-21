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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aacomposetest.Log
import com.aacomposetest.ui.theme.MyColor
import com.aacomposetest.ui.theme.Theme

@Composable
fun EqualizerPage(modifier: Modifier = Modifier.fillMaxSize()) {
   
   val state = rememberLazyListState()
   
   // Background
   Theme.RenderDefaultBackground()
   /*
   LazyColumn(
      state = state,
      modifier = modifier
   ) {
      
      // a header
      item {
         myExoPlayer.equalizerController?.let {equalizer->
            Box(modifier = Modifier
               .fillMaxWidth()
               .wrapContentHeight()
               .background(MyColor.Red)
            ){
               Text(
                  myExoPlayer.equalizerController?.presets?.get(equalizer.currentPreset)?:"nothing",
                  modifier = Modifier.align(Alignment.CenterStart)
               )
               
               Switch(equalizer.enabled, onCheckedChange = {equalizer.enable(it)},modifier = Modifier.align(Alignment.CenterEnd))
            }
            
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
   
    */
}