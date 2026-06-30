package ExoPlayer

import AppDatabase.SongRelated.Song
import Settings.MySettings
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.StateFlow

class ExoPlayerViewModel() : ViewModel() {

    companion object{

        /**
         * Null playlist = plays songs in the default order of MediaStore
         *
         * */
        fun setSong(song:Song?, initialPosition:Long = 0L, startOnPrepared:Boolean = true, playlistTitle: String?= null){
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

        fun play() = myExoPlayer.start()


        fun pause() = myExoPlayer.pause()


        fun toggle() = myExoPlayer.toggle()


        fun rewind() = myExoPlayer.rewind()

        fun playNext(){
            myExoPlayer.playNextInPlaylist()
        }


        fun playPrevious(allowLoopAround:Boolean = MySettings.settings?.playerAllowLoopAround?.stateFlow?.value == true, forcePrevious:Boolean = false, onBackTreshold: Long = 5 *1000){
            if(myExoPlayer.getCurrentPosition()<(onBackTreshold) || forcePrevious)
                myExoPlayer.playPreviousInPlaylist(allowWrapAround = allowLoopAround)
            else
                myExoPlayer.rewind()
        }

        fun destroyPlayer(){
            myExoPlayer.destroy()
        }
    }

}