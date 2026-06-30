package Settings

import android.content.Context
import android.content.pm.ActivityInfo
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.datastore.preferences.preferencesDataStore
import com.aacomposetest.Classes.DataStoreCursorValue
import com.aacomposetest.Classes.genericKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers


private val Context.settingsDataStore by preferencesDataStore("app_settings")

class SettingsRepository(context: Context) {
   
   // Keys
   companion object {
      private val settingsScope = CoroutineScope(Dispatchers.IO)
      
      private val PLAYER_WRITE_MAX_DEBOUNCE_KEY = "PLAYER_WRITE_MAX_DEBOUNCE"
      val playerPosition_WriteIntervalPossibleRange = 20000L..10000L
      
      private val PLAYER_ALLOW_SHUFFLE_KEY = "PLAYER_ALLOW_SHUFFLE"
      private val PLAYER_ALLOW_LOOP_KEY = "PLAYER_ALLOW_LOOP"
      private val PLAYER_ALLOW_LOOP_TO_END_KEY = "PLAYER_ALLOW_LOOP_TO_END"
      private val PLAYER_ALLOW_LOOP_TO_START_KEY = "PLAYER_ALLOW_LOOP_TO_START"
   }
   
   val playerPosition_WriteInterval = DataStoreCursorValue(
      defaultValue = 10000L,
      writeCondition = {
         it in playerPosition_WriteIntervalPossibleRange
      },
      key = genericKey(PLAYER_WRITE_MAX_DEBOUNCE_KEY),
      datastoreDir = context.settingsDataStore,
      scope = settingsScope
   )
   
   val playerAllowShuffle = DataStoreCursorValue(
      defaultValue = false,
      writeCondition = {true},
      key = genericKey(PLAYER_ALLOW_SHUFFLE_KEY),
      datastoreDir = context.settingsDataStore,
      scope = settingsScope
   )
   
   val playerAllowLoopSong = DataStoreCursorValue(
      defaultValue = false,
      writeCondition = {true},
      key = genericKey(PLAYER_ALLOW_LOOP_KEY),
      datastoreDir = context.settingsDataStore,
      scope = settingsScope
   )
   /**
    * When looping from the first song to the last.
    * */
   val playerAllowLoopAround = DataStoreCursorValue(
      defaultValue = false,
      writeCondition = {true},
      key = genericKey(PLAYER_ALLOW_LOOP_TO_END_KEY),
      datastoreDir = context.settingsDataStore,
      scope = settingsScope
   )
   /**
    * When the player gets to the the last song.
    * */
   val playerAllowLoopBack = DataStoreCursorValue(
      defaultValue = true,
      writeCondition = {true},
      key = genericKey(PLAYER_ALLOW_LOOP_TO_START_KEY),
      datastoreDir = context.settingsDataStore,
      scope = settingsScope
   )
   
   val orientation = DataStoreCursorValue(
      defaultValue = MySettings.ScreenOrientation.Auto.value,
      writeCondition = {
         MySettings.ScreenOrientation.contains(it)
      },
      key = genericKey(PLAYER_WRITE_MAX_DEBOUNCE_KEY),
      datastoreDir = context.settingsDataStore,
      scope = settingsScope
   )
   
}

object MySettings {
   var settings by mutableStateOf<SettingsRepository?>(null)
      private set
   
   private var isInitialized = false
   private fun _initialize(context: Context){
      settings = SettingsRepository(context)
      isInitialized = true
   }
   
   fun initialize(context: Context){
      if(!isInitialized){
         _initialize(context)
      }
   }
   
   enum class ScreenOrientation(val value:Int){
      Auto(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED),
      Portrait(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT),
      LandScape(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
      
      companion object {
         fun fromInt(value: Int): ScreenOrientation {
            return when (value) {
               ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED -> {
                  Auto
               }
               ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT -> {
                  Portrait
               }
               ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE -> {
                  LandScape
               }
               else -> {
                  Auto
               }
            }
         }
         
         fun contains(value:Int):Boolean{
            return when(value){
               ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED -> {
                  true
               }
               ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT -> {
                  true
               }
               ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE -> {
                  true
               }
               else -> {
                  false
               }
            }
         }
      }
   }
}