package Settings

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.datastore.preferences.preferencesDataStore
import com.aacomposetest.Classes.AutoStateFlow
import com.aacomposetest.Classes.accessCursorValue


private val Context.settingsDataStore by preferencesDataStore("app_settings")

class SettingsRepository(context: Context) {
   
   // Keys
   companion object {
      val PLAYER_WRITE_MAX_DEBOUNCE = "PLAYER_WRITE_MAX_DEBOUNCE"
   }
   val playerPositionWriteInterval by accessCursorValue(10000L,PLAYER_WRITE_MAX_DEBOUNCE,
      context.settingsDataStore
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