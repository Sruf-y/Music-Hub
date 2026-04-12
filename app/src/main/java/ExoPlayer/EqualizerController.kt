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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
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
 * TODO: Replace the equalizer with a [DynamicsProcessing]
 * */
@OptIn(UnstableApi::class)
class EqualizerController(sessionId: Int){
   /**
    * Equalizer affects only captured noise. This means there is no need to destroy it since it dies with the proccess.
    * */
   private var equalizer: Equalizer = Equalizer(0,sessionId)
   
   /**
    * A list of the available presets as strings
    * */
   var presets by mutableStateOf(listOf<String>())
      private set
   
   /**
    * Gets the current preset.
    *
    * @return the preset that is set at the moment.
    * */
   var currentPreset by mutableIntStateOf(0)
      private set
   
   
   var enabled by mutableStateOf(false)
      private set
   
   fun enable(value:Boolean){
      equalizer.enabled = value
      enabled = equalizer.enabled
   }
   
   private fun refreshCurrentPreset(){
      currentPreset = equalizer.currentPreset.toInt()
   }
   
   // Will list the available fresets from the system
   private fun updatePresets(){
      val nr = equalizer.numberOfPresets
      
      presets = List(nr.toInt()) {it-> equalizer.getPresetName(it.toShort()) }
   }
   
   /**
    * Change the settings to a given preset from [presets] using its index
    * */
   fun changeSettingsToPreset(preset:Int){
      if(preset>=0 && preset < presets.size) {
         equalizer.usePreset(preset.toShort())
      }
      onSettingsChanged()
   }
   
   /**
    * Change the settings by providing a new instance
    * */
   fun changeSettings(block:(Equalizer.Settings)-> Equalizer.Settings){
      equalizer.properties = block.invoke(equalizer.properties)
      onSettingsChanged()
   }
   
   var bandLevels by mutableStateOf<List<Short>>(listOf())
      private set
   
   val levelRange:List<Short>
      get() {
         return ArrayList<Short>().apply {
            val range = equalizer.bandLevelRange.toList()
            addAll(range)
            Log("Range is from ${range.firstOrNull()} to ${range.lastOrNull()}")
         }
      }
   
   fun refreshData(){
      onSettingsChanged()
   }
   private fun onSettingsChanged(){
      refreshCurrentPreset()
      bandLevels = _getBandLevels()
      
   }
   
   init {
      updatePresets()
      onSettingsChanged()
   }
   
   /**
    * Delete later, does nothing, just to see what functions do what
    * */
   fun getBands(): Unit{
      val listOfCenterFrequencies = ArrayList<Int>()
      val numberOfBands = equalizer.numberOfBands
      
      for(i in 0 until numberOfBands) {
         val range = equalizer.getCenterFreq(i.toShort())
         
         listOfCenterFrequencies.add(range)
      }
      
      val levelOfBand0 = equalizer.getBandLevel(listOfCenterFrequencies[0].toShort())
   }
   
   private fun _getBandLevels():List<Short>{
      val list = mutableListOf<Short>()
      val numberOfBands = equalizer.numberOfBands
      for(i in 0 until numberOfBands) {
         val item = equalizer.getBandLevel(i.toShort())
         list.add(item)
         Log(item.toInt().toString())
      }
      
      return list
   }
   
   
   
   
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
         
         val data by remember {
            derivedStateOf {
               myExoPlayer.equalizerController?.bandLevels?.map {
                  it.toFloat().map(
                     myExoPlayer.equalizerController?.levelRange?.first()?.toFloat() ?: 0f,
                     myExoPlayer.equalizerController?.levelRange?.last()?.toFloat() ?: 1f,
                     0f,
                     1f
                  )
               }
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
                  myExoPlayer.equalizerController?.refreshData()
                  tryTo {
                     groupState.setValues {
                        data?:emptyList()
                     }
                  }
                  
               }) { Text("Refresh") }
            }
         }
      }
   }
}