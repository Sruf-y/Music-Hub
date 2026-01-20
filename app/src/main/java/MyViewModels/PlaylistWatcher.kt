package MyViewModels

import AppDatabase.PlaylistRelated.Playlist
import MyViewModels.FileWatcher.Companion.FileWatchListener
import android.content.Context
import android.os.FileObserver.CLOSE_WRITE
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.aacomposetest.CrashReporter
import com.aacomposetest.MyApplication
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.lang.ref.WeakReference
import java.util.UUID
import kotlin.random.Random

/**
 * Strictly persists a playlist as a state. That is this class' one and ONLY purpose.
 * */
class PlaylistWatcher(fileWatcher: FileWatcher, val logger: CrashReporter  = MyApplication.crashLogger): ViewModel(){
    
    val contextRef = WeakReference(MyApplication.instance)
    
    /**
     * Save onInstanceCleared callback on initialization to use later, as after initialization, the constructor parameter is no longer accesible.
     *
     * WARNING! Making the constructor value a stored variable [val] or [var] might duplicate instances leading to circular dependency.
     * */
    private val onClear:()-> Unit = {
        try {
            fileWatcher.removeOnChangeListener(listenerKey)
        }catch (ex: Exception){
            contextRef.get()?.apply {
                logger.writeCrashToFile(ex,this, headerDetails = "Failed in onClear")
            }
            
        }
    }

    var file:MutableState<File?> = mutableStateOf(null)
        private set
    private val _curPlaylist = MutableStateFlow<Playlist?>(null)
    val playlist = _curPlaylist.asStateFlow()

    val listenerKey = UUID.randomUUID().hashCode()
    
    


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
        fileNameFilter = {name->
            name == file.value?.name
        },
        eventFilter = {event->
            event == CLOSE_WRITE
        },
        onEvent = { event, name ->
            
            contextRef.get()?.apply {
                onChangeNoticed(this)
            }
            
        }
    )
    
    

    init {
        fileWatcher.addOnChangeListener(listener)
        // do NOT try-catch here. If this fails, the circular dependency is BAD bad
    }

    override fun onCleared() {
        super.onCleared()
        
        onClear.invoke()
    }

}