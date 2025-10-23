package MyViewModels

import AppDatabase.PlaylistRelated.Playlist
import ExoPlayer.myExoPlayer
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aacomposetest.Functions.FolderWatcher
import com.aacomposetest.Logs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PlaylistViewModel(context: Context): ViewModel() {


    private val folderWatcher: FolderWatcher = FolderWatcher(Playlist.getFolder(context),
        onChange = {
            Log.i(Logs.RUNNING_INFO.name,"Playlists directory noticed a change")
            updateList(context)
    })

    fun startWatchingFolder(){
        folderWatcher.startWatching()
    }


    private val _allPlaylists: MutableStateFlow<ArrayList<Playlist>> = MutableStateFlow(ArrayList())
    val allPlaylists: StateFlow<ArrayList<Playlist>> = _allPlaylists.asStateFlow()


    val currentMediaPlaylistTitle = myExoPlayer.currentPlaylistTitle

    fun saveCurrentPlaylist(context: Context){
        viewModelScope.launch {

            currentMediaPlaylistTitle.value?.apply {
                Playlist.getPlaylist(context,this)?.save(context)
            }

        }

    }

    fun delete(playlist: Playlist,context:Context){
        playlist.getFileIfExists(context)?.delete()
        updateList(context)
    }

    fun updateList(context: Context,onfinished: (() -> Unit)? = null) {

        val filesList = Playlist.getFolder(context).listFiles()

        val newlist: ArrayList<Playlist> = ArrayList()

        if (filesList != null) {

            viewModelScope.launch {

                filesList.forEachIndexed { index, file ->

                    Playlist.getFromFile(file)?.let { loadedPlaylist ->
                        newlist.add(loadedPlaylist)
                    }

                }

                _allPlaylists.value = newlist
                onfinished?.invoke()
            }


        } else {
            _allPlaylists.value = newlist
            onfinished?.invoke()
        }
    }







    override fun onCleared() {
        folderWatcher.stopWatching()
        super.onCleared()
    }

}