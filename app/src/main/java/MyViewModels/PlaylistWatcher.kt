package MyViewModels

import AppDatabase.PlaylistRelated.Playlist
import android.content.Context
import android.os.FileObserver.CLOSE_WRITE
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.aacomposetest.Functions.FileWatcher
import com.aacomposetest.Functions.FileWatcher.Companion.FileWatchListener
import com.aacomposetest.Log
import com.aacomposetest.Logs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.File
import kotlin.math.log
import kotlin.random.Random
import kotlin.random.nextInt


class PlaylistWatcher(context: Context): ViewModel(){

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
            val old = _curPlaylist.value
            _curPlaylist.value = result

            file.value = result.getFileIfExists(context)!!
        }
    }


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
        ListFeedingVM.playlistFolderWatcher.addOnChangeListener(listener)
    }

    override fun onCleared() {
        super.onCleared()

        ListFeedingVM.playlistFolderWatcher.removeOnChangeListener(listenerKey)
    }

}