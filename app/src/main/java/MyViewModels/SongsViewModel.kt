package MyViewModels

import AppDatabase.AndroidDatabase
import AppDatabase.MyDatabaseProvider
import AppDatabase.SongRelated.Song
import AppDatabase.SongRelated.SongRepository
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class SongsViewModel(context: Context): ViewModel() {

    private val dao = MyDatabaseProvider.getDatabase(context).songDao()
    private val repo = SongRepository(dao)

    init {
        viewModelScope.launch {
            repo.allsongsFlow.collect{songs->
                _songsAsFlow.value = songs
            }
        }

    }


    private val _songsAsFlow = MutableStateFlow<List<Song>>(emptyList())
    val getAllSongsFlow = _songsAsFlow.asStateFlow()

    // Value used for playNextSong and playPreviousSong
    private val getSortedSongsStateFlow
        get() = ListFeedingVM.SortedSongs.sortedSongsFlow


    fun insertSong(song: Song){
        viewModelScope.launch {
            repo.addSong(song)
        }
    }

    fun upsert(song: Song){
        viewModelScope.launch {
                repo.upsert(song)
        }
    }

    suspend fun getNextSongById(currentId:Long, default:Boolean = false): Song?{

        val snapshot = if(default)getAllSongsFlow.firstOrNull() else getSortedSongsStateFlow.firstOrNull()


        snapshot?.let {
            val mappedSnapshot = snapshot.map{it->it.id}
            val curIndex = mappedSnapshot.indexOf(currentId)
            if(mappedSnapshot.contains(currentId) ){
                // return the SONG, not the song's ID
                return try {
                    it.get(curIndex + 1)
                } catch (ex: IndexOutOfBoundsException) {
                    null
                }
            }else{
                return null
            }
        }

        return null
    }

    suspend fun getPreviousSongById(id:Long,default:Boolean = false):Song?{
        val snapshot = if(default)getAllSongsFlow.firstOrNull() else getSortedSongsStateFlow.firstOrNull()

        snapshot?.let {
            val mappedSnapshot = snapshot.map{it->it.id}
            val curIndex = mappedSnapshot.indexOf(id)
            if(mappedSnapshot.contains(id)){
                // return the SONG, not the song's ID
                return try {
                    it.get(curIndex - 1)
                } catch (ex: IndexOutOfBoundsException) {
                    null
                }
            }else{
                return null
            }
        }

        return null
    }

    fun deleteSong(id: Long,context: Context){
        viewModelScope.launch {

            // First delete the thumbnail if it exists

            repo.getSongById(id)?.tryGetThumbnail(context)

            repo.deleteSongById(id)
        }
    }

    fun deleteSong(song: Song, context: Context){
        viewModelScope.launch {

            // First delete the thumbnail if it exists

            repo.getSongById(song.id)?.tryGetThumbnail(context)

            repo.deleteSong(song)
        }
    }


    fun updateSong(song: Song){
        viewModelScope.launch {
            repo.updateSong(song)
        }
    }

    suspend fun getSongById(id: Long): Song?{
        return repo.getSongById(id)
    }

    fun unsafeGetSongById(id: Long): Song?{
        return repo.unsafeGetSongById(id)
    }
    /**
     * TODO: Create functions also for singleResync and multipleResync
     * */
    suspend fun resyncALL_withMediaStore(context: Context, alsoPictures:Boolean, forceUpdate:Boolean, onFinish:( suspend ()->Unit)? = null):Int{

        return AndroidDatabase.doSongsQuery(
            viewmodel = this@SongsViewModel,
            context = context,
            alsoPictures = alsoPictures,
            forceUpdate = forceUpdate,
            onFinish = onFinish
        )

    }

}

class SongsViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SongsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SongsViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}