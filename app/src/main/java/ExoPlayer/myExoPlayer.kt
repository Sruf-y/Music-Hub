package ExoPlayer

import AppDatabase.PlaylistRelated.Playlist
import AppDatabase.SongRelated.Song
import ExoPlayer.myExoPlayer.prefs
import MyViewModels.DataObserverViewModel
import MyViewModels.SongsViewModel
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.util.Log
import androidx.annotation.OptIn
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.lifecycle.viewModelScope
import androidx.media3.common.AudioAttributes
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer as External_ExoPlayer
import com.aacomposetest.Classes.MyPrefs
import com.aacomposetest.Classes.MyPrefs.getCursorValue
import com.aacomposetest.Classes.MyPrefs.putCursorValue
import com.aacomposetest.Logs
import com.aacomposetest.MyApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object myExoPlayer {

    private val _isInitialized = MutableStateFlow<Boolean>(value = false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    private var ioScope = CoroutineScope(Dispatchers.IO)
    private var progressJob: Job? = null

    private val prefs: SharedPreferences? = MyApplication.instance.getSharedPreferences(MyPrefs.PrefDirs.GenericData,MODE_PRIVATE)


    private val progressVM = DataObserverViewModel<Long>(
        delayMillis = 250,
        initialValue = prefs?.getCursorValue(MyPrefs._prefs_LastSongPosition, 0) ?: 0,
        observingFunction = {
            val currentPos = myExoPlayer.getCurrentPosition()
            val duration = myExoPlayer.currentlyPlayingSong.value?.duration ?: 0

            val progress = if (duration > 0) {
                currentPos
            } else {
                0
            }

            progress
        }
    )


    var isLoopAllowed:Boolean = false

    private var songsVM : SongsViewModel? = null

    private var exoPlayer: External_ExoPlayer? = null
    var isPrepared = false


    private val _currentSong = MutableStateFlow<Song?>(value = null)
    val currentlyPlayingSong: StateFlow<Song?> = _currentSong.asStateFlow()


    private val _currentPlaylistTitle = MutableStateFlow<String?>(value = null)
    val currentPlaylistTitle: StateFlow<String?> = _currentPlaylistTitle.asStateFlow()
    fun setCurrentPlaylist(title:String?){
        _currentPlaylistTitle.value = title
    }

    // Player state tracking
    private val _isPlaying = MutableStateFlow<Boolean>(value = false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    @OptIn(UnstableApi::class)
    fun initializePlayer(context: Context, onInilialized:(()-> Unit)? = null) {

        if(songsVM == null){
            songsVM = SongsViewModel(context)
        }

        val songViewModel = SongsViewModel(context)

        if (exoPlayer == null || exoPlayer!!.isReleased) {

            Log.i(Logs.EXOPLAYER.name,"Attempting to CREATE exoplayer from NULL.")

            exoPlayer = External_ExoPlayer.Builder(context)
                .setAudioAttributes(
                    AudioAttributes.DEFAULT,
                    true // Handle audio focus
                )
                .setHandleAudioBecomingNoisy(true) // Pause when headphones unplugged
                .build()
                .apply {
                    addListener(object : Player.Listener {
                        override fun onPlaybackStateChanged(state: Int) {
                            when (state) {
                                Player.STATE_READY -> isPrepared = true
                                Player.STATE_ENDED -> {
                                     playNextInPlaylist(allowLoop = isLoopAllowed)
                                }

                                Player.STATE_IDLE -> isPrepared = false
                                Player.STATE_BUFFERING -> {

                                }
                            }
                        }

                        override fun onIsPlayingChanged(isPlaying: Boolean) {
                            _isPlaying.value = isPlaying

                            if(isPlaying){
                                progressVM.startUpdates()
                            }
                        }
                    })
                }



            CoroutineScope(Dispatchers.IO).launch{

                prefs?.apply {
                    val songId = getCursorValue(MyPrefs._prefs_LastSongId,-1)
                    val song = songViewModel.getSongById(songId)

                    val songPosition = getCursorValue(MyPrefs._prefs_LastSongPosition,0)

                    val playlistTitle: String? = getCursorValue(MyPrefs._prefs_LastPlaylistTitle,"")

                    val playlist = Playlist.getPlaylist(context,playlistTitle?:"")

                    progressVM.forceValue(songPosition)

                    _currentSong.value = song

                    // Start updating the shared prefferences

                    progressJob?.cancel()
                    progressJob = ioScope.launch {

                        progressVM.progress
                            .collect{position->

                                //Log.i(Logs.EXOPLAYER.name,"Logged songPos ${position}")

                                prefs.edit(){

                                    putCursorValue(MyPrefs._prefs_LastSongPosition,position)
                                }
                            }

                    }

                    withContext(Dispatchers.Main) {
                        setSong(song = song, initialPosition = songPosition, startOnPrepared = false,playlistTitle = playlist?.title )
                        _isInitialized.value = true
                    }

                }
            }






            Log.i(Logs.EXOPLAYER.name,"ExoPlayer initialized succesuflly.")
            onInilialized?.invoke()


        }




    }

    fun rewind() {
        currentlyPlayingSong.value?.let {
            seekTo(0)
        }
    }

    fun seekTo(pos: Long, rawPosition: Boolean = true) {
        exoPlayer?.let { player ->
            val positionMs =
                if (rawPosition) pos else (pos / 100) * player.duration.coerceAtLeast(1)

            val seekedPosition = positionMs.coerceIn(0, currentlyPlayingSong.value?.duration)

            player.seekTo(seekedPosition)


            // If paused and seek goes to end, clear _isPlaying for clarity
            if (!player.isPlaying && positionMs >= player.duration) {
                _isPlaying.value = false
            }


        }
    }

    /**
     * DOES NOT INITIALIZE PLAYER ANYMORE!!!
     * */
    private fun privSetSong(song: Song?, startOnPrepared:Boolean=true,throwErrorOnError: Boolean = false) {

        val lastSong = currentlyPlayingSong

        exoPlayer?.let { player ->
            try {
                song?.let {
                    _currentSong.value = song
                    val mediaItem = MediaItem.Builder()
                        .setUri(song.songUri.toUri())
                        .setMediaMetadata(
                            MediaMetadata.Builder()
                                .setTitle(song.title)
                                .setArtist(song.artistName)
                                .build()
                        )
                        .setTag(song) // Store original Song object
                        .build()

                    player.setMediaItem(mediaItem)
                    player.prepare()

                    // Update song stats
                    song.apply {
                        timesPlayed++
                    }





                    if(startOnPrepared)
                        start()


                }





            } catch (ex: Exception) {
                Log.e(Logs.DATA_ERROR.toString(), "Error setting song", ex)
                if(!throwErrorOnError) {
                    lastSong.value?.let {
                        privSetSong(
                            song = it,
                            startOnPrepared = startOnPrepared,
                            throwErrorOnError = true
                        )
                    }
                }else{
                    throw ex
                }
            }
        }
    }




    /**
     * To be used inside composables to set the song's playlist along with the song
     *
     * */
    fun setSong(song: Song?, startOnPrepared:Boolean=true, playlistTitle: String? =  null, initialPosition: Long = 0L){



        _currentPlaylistTitle.value = playlistTitle

        privSetSong(song = song, startOnPrepared = startOnPrepared)
        seekTo(initialPosition)

        // Update song in database if its not null
        song?.let {
            songsVM?.updateSong(it)
        }


        prefs?.edit(){
            putCursorValue(MyPrefs._prefs_LastSongId,currentlyPlayingSong.value?.id?:-1)

            putCursorValue(MyPrefs._prefs_LastPlaylistTitle,currentPlaylistTitle.value?:"")
        }
    }



    /**
     * Plays previous song in playlist.
     *
     * [allowWrapAround] - If function is allowed to wrap around to the last song in the playlist
     * */
    fun playPreviousInPlaylist(allowWrapAround:Boolean = false){
        CoroutineScope(Dispatchers.Main).launch {
            val playlist = Playlist.getPlaylist(MyApplication.instance,currentPlaylistTitle.value?:"")

            if(playlist!=null) {
                playlist.songsIdList.let { songsList ->

                    currentlyPlayingSong.value?.let { currentSong ->
                        val currentId = currentSong.id

                        songsVM?.viewModelScope?.launch {

                            val previousSongUnchecked = playlist.getPreviousId(currentId)

                            // if allowLoop, then make the next song first if its null. Otheriwse let it be null if its last
                            // song and as such, nothing will play
                            val previousSongId =try {
                                if (allowWrapAround) previousSongUnchecked ?: songsList.last()
                                else previousSongUnchecked
                            }catch (ex: NoSuchElementException){
                                null
                            }


                            previousSongId?.let {
                                privSetSong(
                                    song = songsVM!!.getSongById(it), startOnPrepared = true
                                )
                            }
                        }
                    }
                }
            }else{
                currentlyPlayingSong.value?.let {currentSong->
                    val currentId = currentSong.id

                    songsVM?.viewModelScope?.launch {

                        val previousSongUnchecked = songsVM!!.getPreviousSongById(currentId)

                        // if allowLoop, then make the next song first if its null. Otheriwse let it be null if its last
                        // song and as such, nothing will play
                        val previousSong =try {
                            if (allowWrapAround) previousSongUnchecked ?: songsVM!!.getAllSongsFlow.firstOrNull()?.last()
                            else previousSongUnchecked
                        }catch (ex: NoSuchElementException){
                            null
                        }

                        // only play previous if previous song id is not null
                        previousSong?.let {
                            privSetSong(
                                song = it, startOnPrepared = true
                            )
                        }

                    }
                }
            }

        }


    }


    /**
     * Plays the next song in the current playlist.
     *
     * [allowLoop] - If function is allowed to loop back to the first son in the playlist.
     * */
    fun playNextInPlaylist(allowLoop:Boolean = false){
        CoroutineScope(Dispatchers.Main).launch {
            val playlist =
                Playlist.getPlaylist(MyApplication.instance, currentPlaylistTitle.value ?: "")

            if(playlist!=null && playlist.songsIdList.contains(currentlyPlayingSong.value?.id)==true) {
                playlist.songsIdList.let { songsList->

                    currentlyPlayingSong.value?.let {currentSong->
                        val currentId = currentSong.id

                        songsVM?.viewModelScope?.launch {

                            val nextSongIdUnchecked = playlist.getNextId(currentId)

                            // if allowLoop, then make the next song first if its null. Otheriwse let it be null if its last
                            // song and as such, nothing will play
                            val nextSongId =try {
                                if (allowLoop) nextSongIdUnchecked ?: songsList.first()
                                else nextSongIdUnchecked
                            }catch (ex: NoSuchElementException){
                                null
                            }

                            // only play next if next song id is not null
                            nextSongId?.let {
                                privSetSong(
                                    song = songsVM!!.getSongById(it), startOnPrepared = true
                                )
                            }

                        }
                    }
                }
            }
            else{
                currentlyPlayingSong.value?.let {currentSong->
                    val currentId = currentSong.id

                    songsVM?.viewModelScope?.launch {

                        val nextSongIdUnchecked = songsVM!!.getNextSongById(currentId)

                        // if allowLoop, then make the next song first if its null. Otheriwse let it be null if its last
                        // song and as such, nothing will play
                        val nextSong = try {
                            if (allowLoop) nextSongIdUnchecked ?: songsVM!!.getAllSongsFlow
                                .first().first()
                            else nextSongIdUnchecked
                        }catch (ex: NoSuchElementException){
                            null
                        }

                        // only play next if next song id is not null
                        nextSong?.let {
                            privSetSong(
                                song = it, startOnPrepared = true
                            )
                        }

                    }
                }
            }


        }



    }


    /**
     * Starts/Resumes playback.
     * */
    fun start(){
        if(exoPlayer!= null && !isPlaying.value) {
            exoPlayer?.play()
        }
    }


    /**
     * Pauses playback.
     * */
    fun pause(){
        if(exoPlayer!= null && isPlaying.value) {
            exoPlayer?.pause()
        }
    }


    /**
     * Safe toggle between pausing and resuming playback.
     * */
    fun toggle(){

        exoPlayer?.let {
            if( isPlaying.value){
                pause()
            }else{
                start()
            }
        }
    }


    fun getCurrentPosition() = exoPlayer?.currentPosition?:0
    fun getCurrentPositionAsFlow() = progressVM


    /**
     * Destroys the media playback.
     *
     * Exoplayer is set to null.
     * It will need to be reinitialized after calling this function.
     * */
    fun destroy(){

        Log.i(Logs.EXOPLAYER.name,"Attempting to DESTROY exoplayer.")

        progressVM.stopUpdates()
        onDestroy_SaveLogic()

        _currentSong.value = null
        _currentPlaylistTitle.value = null

        try{
            progressJob?.cancel("Player destroyed.")

        }catch (ex: Exception){
            ex.printStackTrace()
        }
        _isInitialized.value = false

        try{
            exoPlayer?.stop()

        }catch (ex: Exception){
            ex.printStackTrace()
        }

        exoPlayer?.release()
    }

    fun release(){
        exoPlayer?.release()
    }

    fun onDestroy_SaveLogic(){
        prefs?.edit {
            putCursorValue(MyPrefs._prefs_LastSongPosition,getCurrentPosition())
            putCursorValue(MyPrefs._prefs_LastSongId,currentlyPlayingSong.value?.id?:-1)
            putCursorValue(MyPrefs._prefs_LastPlaylistTitle,currentPlaylistTitle.value?:"")
        }
    }
}