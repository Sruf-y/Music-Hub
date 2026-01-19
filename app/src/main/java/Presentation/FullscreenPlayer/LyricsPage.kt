package Presentation.FullscreenPlayer


import AppDatabase.SongRelated.Song
import SrufComposeComponents.Text
import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import com.aacomposetest.Functions
import com.aacomposetest.MyApplication
import kotlinx.serialization.Serializable
import java.io.File

@Composable
internal fun LyricsPage(modifier: Modifier = Modifier){
    Box(modifier = modifier){
        Text("TODO()",modifier = Modifier.align(Alignment.Center))
    }
}


@Composable
private fun TextArea(value:String,onValueChange:(String)-> Unit,allowEdit: Boolean){
    
    
    
    
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = allowEdit
    )
    
    
}

@Serializable
data class Lyrics(val id:Long,var title:String,var text:String){
    companion object{
        val LYRICS_DIR = "Lyrics"
        
        fun getFolder(context: Context): File {
            val folder = File(context.filesDir, LYRICS_DIR)
            if(!folder.exists()){
                folder.mkdir()
            }
            return folder
        }
    }
}

class LyricsManager(): ViewModel(){
    
    
    val text = mutableStateOf("")
    
    fun loadText(songId:Long){
        // use a FolderWatcher, not the one that watches the PLaylist folder from MyApplication
    }
    
    fun saveText(){
    
    }
    
    override fun onCleared() {
        super.onCleared()
        
        saveText()
    }
    
    companion object{
    
    }
}