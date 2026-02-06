package MyViewModels

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.aacomposetest.CrashReporter
import com.aacomposetest.Functions
import com.aacomposetest.MyApplication
import java.io.File


/**
 * @property lyrics stateful property dictated by [loadLyricsForSong] or by the user editing the text.
 * */
class LyricsManager(private val logger: CrashReporter? = MyApplication.crashLogger) {
   
   val lyrics = mutableStateOf<String?>(null)
   private var songId by mutableLongStateOf(defaultID)
   
   private fun errorResetValues(){
      lyrics.value = ""
      songId = -1L
   }
   
   suspend fun loadLyricsForSong(id:Long?, context: Context){
      
      /** Retrieve the current lyric */
      try {
         // Early return in case of null or invalid
         if(id==defaultID || id ==null){
            errorResetValues()
            return
         }
         
         songId = id
         val dir = getDir(context)
         
         lyrics.value =
            Functions.JsonOp.loadFromJson<String>(
               context,
               filename = id.toString(),
               parent = dir
            ) ?: ""
         
      }catch (ex: Exception){
         errorResetValues()
         try{
            logger?.writeCrashToFile(
               throwable = ex,
               context = context,
               headerDetails = "Lyrics Manager caused this error on Setting the Song. Error is made to be handled."
            )
         }catch (ex: Exception){
            ex.printStackTrace()
         }
      }
   }
   
   suspend fun saveCurrentLyric(context: Context){
      val curLyrics = lyrics.value  //Protect against race conditions
      
      if(songId!=-1L && curLyrics!=null){
         val dir = getDir(context)
         Functions.JsonOp.saveAsJson(context,songId.toString(),curLyrics, parent = dir)
      }
      
   }
   
   companion object{
      val DIR = "Lyrics"
      val defaultID = -1L
      
      fun getDir(context: Context):File{
         val folder = File(context.filesDir,DIR)
         
         if(!folder.exists()){
            folder.mkdir()
         }
         
         return folder
      }
   }
}

@Composable
fun rememberLyricsManager(): LyricsManager{
   
   return remember {
      LyricsManager()
   }
}