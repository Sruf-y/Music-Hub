package ExoPlayer

import AppDatabase.PlaylistRelated.Playlist
import AppDatabase.SongRelated.Song
import MyViewModels.DataObserverViewModel
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ExoPlayerViewModel() : ViewModel() {

    val progressVM = myExoPlayer.getCurrentPositionAsFlow()

    val currentProgress: StateFlow<Long> = progressVM.progress

    val currentSong = myExoPlayer.currentlyPlayingSong

    val currentPlaylist = myExoPlayer.currentPlaylistTitle

    val isPlaying = myExoPlayer.isPlaying



    companion object{

        /**
         * Null playlist = plays songs in the default order of MediaStore
         *
         * */
        fun setSong(song:Song?, initialPosition:Long = 0L, startOnPrepared:Boolean, playlistTitle: String?= null){
            myExoPlayer.setSong(
                song = song,
                startOnPrepared = startOnPrepared,
                playlistTitle = playlistTitle,
                initialPosition = initialPosition
            )
        }

        fun seekTo(pos:Long,rawPosition:Boolean = true,onFinish:(()-> Unit)? = null){
            myExoPlayer.seekTo(pos, rawPosition = rawPosition)
            onFinish?.invoke()
        }

        fun play(){
            myExoPlayer.start()
        }

        fun pause(){
            myExoPlayer.pause()
        }

        fun toggle(){
            myExoPlayer.toggle()
        }

        fun rewind() = myExoPlayer.rewind()

        fun playNext(){
            myExoPlayer.playNextInPlaylist(allowLoop = true)
        }

        fun playPrevious(forcePrevious:Boolean = false){
            if(myExoPlayer.getCurrentPosition()<(10*1000) || forcePrevious)
                myExoPlayer.playPreviousInPlaylist(allowWrapAround = true)
            else
                myExoPlayer.rewind()
        }

        fun destroyPlayer(){
            myExoPlayer.destroy()
        }
    }

}