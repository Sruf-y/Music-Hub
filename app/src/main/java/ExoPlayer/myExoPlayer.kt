package ExoPlayer

import AppDatabase.PlaylistRelated.Playlist
import AppDatabase.SongRelated.Song
import MyViewModels.DataObserverViewModel
import MyViewModels.ListFeedingVM
import MyViewModels.PlaylistWatcher
import MyViewModels.SongsViewModel
import Settings.PlayerSettings
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.media3.common.AudioAttributes
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer as External_ExoPlayer
import com.aacomposetest.Classes.MyPrefs
import com.aacomposetest.Classes.MyPrefs.getCursorValue
import com.aacomposetest.Classes.MyPrefs.putCursorValue
import com.aacomposetest.Functions.getRandom
import com.aacomposetest.Log
import com.aacomposetest.Logs
import com.aacomposetest.MyApplication
import com.aacomposetest.MyApplication.Companion.JsonFormat
import com.aacomposetest.tryTo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

object myExoPlayer {

    private val _isInitialized = MutableStateFlow<Boolean>(value = false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    private var playScope = CoroutineScope(Dispatchers.Main)

    private val prefs: SharedPreferences? = MyPrefs.getPrefs(MyApplication.instance)

    val playerSettings: PlayerSettings =
        PlayerSettings(shuffle = false)
    



    // Handles song position saving.
    private val progressVM = DataObserverViewModel<Long>(
        delayMillis = 250,
        initialValue = prefs?.getCursorValue(MyPrefs._prefs_LastSongPosition, 0) ?: 0,
        observingFunction = {
            val currentPos = getCurrentPosition()
            val duration = currentlyPlayingSong.value?.duration ?: 0


            val progress = if (duration > 0) {
                currentPos
            } else {
                0
            }
            
            progress
        },
        onValueChange = {
            
            prefs?.edit(){
                Log.i(Logs.SHARED_PREFFERENCES.name,"PUT current song position")
                putCursorValue(MyPrefs._prefs_LastSongPosition,it)
            }
        }
    )


    /**
     * If when automatically playing, the songs can loop around or not.
     * */
    var isLoopAllowed:Boolean = false

    private var songsVM : SongsViewModel? = null

    private var exoPlayer: External_ExoPlayer? = null
    var isPrepared = false


    private val _currentSong = MutableStateFlow<Song?>(value = null)
    val currentlyPlayingSong: StateFlow<Song?> = _currentSong.asStateFlow()

    private val allSongs = ListFeedingVM.SortedSongs.sortedSongsFlow

    val curPlaylistVM = PlaylistWatcher(
       fileWatcher = MyApplication.playlistFolderWatcher
    )


    fun setCurrentPlaylist(title:String?,context: Context = MyApplication.instance){
        curPlaylistVM.changePlaylist(playlistTitle = title,context = context)
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
                                Player.STATE_READY -> {
                                    isPrepared = true
                                }
                                Player.STATE_ENDED -> {
                                     playNextInPlaylist(allowLoop = isLoopAllowed)
                                }
                                Player.STATE_IDLE -> {
                                    isPrepared = false
                                }
                                Player.STATE_BUFFERING -> {
                                
                                }
                            }
                        }

                        override fun onIsPlayingChanged(isPlaying: Boolean) {
                            _isPlaying.value = isPlaying

                            if(isPlaying){
                                Log("Started yodates")
                                progressVM.startUpdates()
                            }else{
                                progressVM.forceValue(exoPlayer!!.currentPosition)
                                progressVM.stopUpdates()
                            }
                        }

                        override fun onPositionDiscontinuity(
                            oldPosition: Player.PositionInfo,
                            newPosition: Player.PositionInfo,
                            reason: Int
                        ) {
                            super.onPositionDiscontinuity(oldPosition, newPosition, reason)
                            progressVM.forceValue(newPosition.positionMs)
                        }
                    })
                    
                    // Logging for audio states
                    if(false)
                    addListener(object : Player.Listener {
                        override fun onPlaybackStateChanged(state: Int) {
                            when (state) {
                                Player.STATE_READY -> {
                                    com.aacomposetest.Log("Ready")
                                }
                                
                                Player.STATE_ENDED -> {
                                    com.aacomposetest.Log("Ended")
                                    playNextInPlaylist(allowLoop = isLoopAllowed)
                                }
                                
                                Player.STATE_IDLE -> {
                                    com.aacomposetest.Log("Idle")
                                }
                                
                                Player.STATE_BUFFERING -> {
                                    com.aacomposetest.Log("Buffering")
                                }
                            }
                        }
                        
                        
                    }
                    )
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

    fun rewind(autoStart:Boolean = true) {
        currentlyPlayingSong.value?.let {
            seekTo(0)
            if(autoStart){
                start()
            }
            
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
                    _currentSong.tryEmit(song)
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


        setCurrentPlaylist(playlistTitle)

        privSetSong(song = song, startOnPrepared = startOnPrepared)
        seekTo(initialPosition)

        // Update song in database if its not null
        song?.let {
            songsVM?.updateSong(it)
        }



        prefs?.edit(){
            putCursorValue(MyPrefs._prefs_LastSongId,currentlyPlayingSong.value?.id?:-1)

            putCursorValue(MyPrefs._prefs_LastPlaylistTitle,playlistTitle?:"")
        }
    }
    
    
    private fun playActionLogError(){
        tryTo {
            MyApplication.crashLogger.writeTextToFIle(
                text = """
                        During Play action, current song was not found in current playlist:
                        Current Song:
                        ${
                    if(currentlyPlayingSong.value!=null){
                        JsonFormat.encodeToString(Song.serializer(),currentlyPlayingSong.value!!)
                    }else{
                        "NULL"
                    }
                    
                }
                        
                        Current Playlist:
                        ${
                    if(curPlaylistVM.playlist.value!=null){
                        JsonFormat.encodeToString(Playlist.serializer(),curPlaylistVM.playlist.value!!)
                    }
                    else "NULL"
                }
                        
                        Song exists in playlist:
                        ${curPlaylistVM.playlist.value?.songsIdList?.contains(currentlyPlayingSong.value?.id)}
                    """.trimIndent(),
                filename = "PlayerIssue",
                context = MyApplication.instance
            )
        }
    }
    
    /**
     * Plays previous song in playlist.
     *
     * [allowWrapAround] - If function is allowed to wrap around to the last song in the playlist
     * */
    fun playPreviousInPlaylist(
        allowWrapAround: Boolean = false,
        onError: () -> Unit = {
            Toast.makeText(MyApplication.instance, "CurSong not found", Toast.LENGTH_SHORT).show()
            
            playActionLogError()
        }
    ) {

        currentlyPlayingSong.value?.let {currentSong->

            val curList = curPlaylistVM.playlist.value?.songsIdList?:allSongs.value.map { it.id }

            val curIndex = curList.indexOf(currentSong.id)





            if(curIndex == -1){
                onError.invoke()
            }

            else if(playerSettings.isShuffling.value){
                playRandom()
            }

            /**
             * If is at first index and is allowed to loop around to the last.
             * */
            else if(curIndex == 0 && allowWrapAround){
                playScope.launch {

                    val song = try {
                        songsVM?.getSongById(curList[curList.lastIndex])
                    }catch (ex: Exception){
                        null
                    }

                    setSong(
                        song = song,
                        playlistTitle =  curPlaylistVM.playlist.value?.title
                    )
                }
            }
            /**
             * General behaviour.
             * */
            else if(curIndex <= curList.size && curIndex>0 ){
                playScope.launch {

                    val song = try {

                        songsVM?.getSongById(curList[curIndex-1])
                    }catch (ex: Exception){
                        null
                    }

                    setSong(
                        song = song,
                        playlistTitle =  curPlaylistVM.playlist.value?.title
                    )
                }
            }
        }


    }

    private fun playRandom() {

        currentlyPlayingSong.value?.let { currentSong ->

            val curList = curPlaylistVM.playlist.value?.songsIdList ?: allSongs.value.map { it.id }

            val randomId = curList.getRandom(currentlyPlayingSong.value?.id)

            playScope.launch {

                if (randomId != null) {

                    val song = try {
                        songsVM?.getSongById(randomId)
                    } catch (ex: Exception) {
                        null
                    }

                    setSong(
                        song = song,
                        playlistTitle = curPlaylistVM.playlist.value?.title
                    )

                } else {
                    playFirst(curList)
                }
            }
        }

    }

    /**
     * Doesn't get the list by itself because it's meant to be used by functions that already get the list.
     * */
    private fun playFirst(curList:List<Long>){
        playScope.launch {



            val song = try {
                songsVM?.getSongById(curList[0])
            } catch (ex: Exception) {
                null
            }


            setSong(
                song = song,
                playlistTitle = curPlaylistVM.playlist.value?.title
            )
        }
    }

    /**
     * Plays the next song in the current playlist.
     *
     * [allowLoop] - If function is allowed to loop back to the first son in the playlist.
     * */
    fun playNextInPlaylist(
        allowLoop: Boolean = true,
        onError: () -> Unit = {
            Toast.makeText(MyApplication.instance, "CurSong not found", Toast.LENGTH_SHORT).show()
            playActionLogError()
        },
    ) {

        currentlyPlayingSong.value?.let {currentSong->

            val curList = curPlaylistVM.playlist.value?.songsIdList?:allSongs.value.map { it.id }

            val curIndex = curList.indexOf(currentSong.id)


            if(curIndex == -1) {
                onError.invoke()
            }
            // if shuffling
            else if(playerSettings.isShuffling.value){
                playRandom()
            }
            // if allowloop
            else if(curIndex == curList.lastIndex && allowLoop){
                playFirst(curList)
            }
            // if at the end of a playlist and auto-loop is DISABLED
            else if(curIndex == curList.lastIndex && !allowLoop){
                // NO OP
            }
            // normal, in-playlist behaviour. Excludes last item as it is handled above
            else if( curIndex < curList.lastIndex && curIndex>=0 ){
                playScope.launch {
                    val song = try {
                        songsVM?.getSongById(curList[curIndex+1])
                    }catch (ex: Exception){
                        null
                    }
                    setSong(
                        song = song,
                        playlistTitle =  curPlaylistVM.playlist.value?.title
                    )
                }
            }
            
        }
    }



    /**
     * Starts/Resumes playback.
     * */
    fun start(){
        if(exoPlayer!= null && !isPlaying.value) {
            
            if (exoPlayer!!.currentPosition >= currentlyPlayingSong.value!!.duration) {
                rewind()
            } else {
                exoPlayer?.play()
            }
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
    fun currentPositionVM() = progressVM


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
        setCurrentPlaylist(null)


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
            putCursorValue(MyPrefs._prefs_LastPlaylistTitle,curPlaylistVM.playlist.value?.title?:"")
        }
    }
}