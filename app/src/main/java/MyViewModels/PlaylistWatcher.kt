package MyViewModels

import AppDatabase.PlaylistRelated.Playlist
import android.content.Context
import android.os.FileObserver.CLOSE_WRITE
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.aacomposetest.Functions
import com.aacomposetest.Functions.FileWatcher.Companion.FileWatchListener
import com.aacomposetest.MyApplication
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import kotlin.random.Random

/**
 * Strictly persists a playlist as a state. That is this class' one and ONLY purpose.
 * */
class PlaylistWatcher(context: Context,fileWatcher: Functions.FileWatcher): ViewModel(){
    
    /**
     * Save onInstanceCleared callback on initialization to use later, as after initialization, the constructor parameter is no longer accesible.
     *
     * WARNING! Making the constructor value a stored variable [val] or [var] might duplicate instances leading to circular dependency.
     * */
    private val onClear:()-> Unit = {
        fileWatcher.removeOnChangeListener(listenerKey)
    }

    var file:MutableState<File?> = mutableStateOf(null)
        private set
    private val _curPlaylist = MutableStateFlow<Playlist?>(null)
    val playlist = _curPlaylist.asStateFlow()

    val listenerKey = Random.nextInt(200)
    
    


    private fun onChangeNoticed(context: Context,title:String? = playlist.value?.title) {

        val result = Playlist.getPlaylist(context = context, title = title)

        if (result == null) {
            _curPlaylist.value = null
            file.value = null
        } else {
            _curPlaylist.value = result
            file.value = result.getFileIfExists(context)!!
        }
    }

    /**
     * Sets the playlist to a real, existing one.
     * */
    fun changePlaylist(playlistTitle: String?,context: Context){
        onChangeNoticed(context,playlistTitle)
    }

    val listener = FileWatchListener(
        key = listenerKey,
        onEvent = { event, name ->

            if (name == file.value?.name && event == CLOSE_WRITE) {

                onChangeNoticed(context)
            }

        }
    )
    
    

    init {
        fileWatcher.addOnChangeListener(listener)
    }

    override fun onCleared() {
        super.onCleared()
        
        onClear.invoke()
    }

}