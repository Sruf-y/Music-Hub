package ExoPlayer

import android.media.audiofx.DynamicsProcessing
import android.media.audiofx.Equalizer
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import com.aacomposetest.Log
import com.aacomposetest.map
import com.aacomposetest.tryTo
import ui.components.ConnectedSliderGroup
import ui.components.rememberSliderGroupState

/**
 * TODO: finish this
 * */
@OptIn(UnstableApi::class)
class EqualizerController2(sessionId: Int){
   /**
    * Equalizer affects only captured noise. This means there is no need to destroy it since it dies with the proccess.
    * */
   private var equalizer: DynamicsProcessing = DynamicsProcessing(sessionId)
   
   
   var enabled by mutableStateOf(false)
      private set
   
   fun enable(value:Boolean){
      equalizer.enabled = value
      enabled = equalizer.enabled
   }
   
   
   
   
   
   
   private fun onSettingsChanged(){
   
   }
   
   init {
      onSettingsChanged()
   }
   
   /**
    * Delete later, does nothing, just to see what functions do what
    * */
   fun functionToTryOutStuffAndDeleteLater(): Unit{
      
      for(i in 0..equalizer.channelCount){
         val a = equalizer.getChannelByChannelIndex(i)
         
      }
      
   }
   
   data class BandData(
      val bandIndex: Int,
      val threshold: Float,
      val ratio: Float,
      val attackMs: Float,
      val releaseMs: Float,
      val kneeWidthDb: Float,
      val makeupGainDb: Float,
      val enabled: Boolean
   )
   
   
   // TODO: Finish storage logic (REALLY need to automate this sometime)
   /**
    * @param filename the name/id_name of the file to save data to. Ex: song's id to be able to save per song configs
    * */
   fun onSaveLogic(
      filename: String = "Equalizer Data",
      folder: String = "Equalizer"
   ){
   
   }
   
   /**
    * @param filename the name/id_name of the file to restore the data from. Ex: song's id to be able to save per song configs
    * */
   //TODO: Finish storage logic and change its return type to match.
   fun onLoadLogic(
      filename: String = "Equalizer Data",
      folder: String = "Equalizer"
   ):Unit{
   
   }
   
   //TODO: Add logic for checking if a song has an entry in the equalizer config SQL table and update the settings based on that.
   
   
   companion object{
      @kotlin.OptIn(ExperimentalMaterial3Api::class)
      @Composable
      internal fun BandsTestPreview(){
         
         val data:List<Float>? by remember {
            derivedStateOf {
               listOf()
            }
         }
         
         val groupState = rememberSliderGroupState(data?:emptyList(), orientation = Orientation.Vertical,reverseVerticalDirection = true)
         
         Box(Modifier
            .fillMaxSize()
            .safeDrawingPadding()){
            Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
               
               ConnectedSliderGroup(
                  state = groupState,
                  lineWidth = 5.dp,
                  lineColor = Color.Green,
                  thumb = { state, modifier ->
                     Spacer(
                        modifier
                           .size(15.dp)
                           .background(Color.Green, CircleShape)
                     )
                  },
                  modifier = Modifier.wrapContentWidth().height(300.dp),
                  lineStartValue = 0.5f,
                  lineEndValue = 0.5f,
               )
               
               Button({
                  tryTo {
                     groupState.setValues {
                        data?:emptyList()
                     }
                  }
               }
               ) {
                  Text("Refresh")
               }
            }
         }
      }
   }
}