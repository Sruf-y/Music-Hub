package Settings

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.datastore.preferences.preferencesDataStore
import com.aacomposetest.Classes.accessCursorValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers


private val Context.settingsDataStore by preferencesDataStore("app_settings")

class SettingsRepository(context: Context) {
   
   // Keys
   companion object {
      private val settingsScope = CoroutineScope(Dispatchers.IO)
      
      private val PLAYER_WRITE_MAX_DEBOUNCE_KEY = "PLAYER_WRITE_MAX_DEBOUNCE"
      val playerPosition_WriteIntervalPossibleRange = 20000L..10000L
   }
   
   val playerPosition_WriteInterval by accessCursorValue(
      defaultValue = 10000L,
      writeCondition = {
         it in playerPosition_WriteIntervalPossibleRange
      },
      key = PLAYER_WRITE_MAX_DEBOUNCE_KEY,
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
   }
   
   fun initialize(context: Context){
      if(!isInitialized){
         _initialize(context)
      }
   }
}