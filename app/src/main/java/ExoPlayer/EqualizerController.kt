package ExoPlayer

import android.media.audiofx.Equalizer
import androidx.annotation.OptIn
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer

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
   
   fun updateCurrentPreset(){
      currentPreset = equalizer.currentPreset.toInt()
   }
   
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
      updateCurrentPreset()
   }
   
   /**
    * Change the settings by providing a new instance
    * */
   fun changeSettings(block:(Equalizer.Settings)-> Equalizer.Settings){
      equalizer.properties = block.invoke(equalizer.properties)
      updateCurrentPreset()
   }
   
   init {
      updatePresets()
      updateCurrentPreset()
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
   
}