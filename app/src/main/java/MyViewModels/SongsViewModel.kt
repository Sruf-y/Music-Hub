package MyViewModels

import AppDatabase.AndroidDatabase
import AppDatabase.MyDatabaseProvider
import AppDatabase.SongRelated.Song
import AppDatabase.SongRelated.SongRepository
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aacomposetest.Classes.MyPrefs
import com.aacomposetest.Classes.MyPrefs.getCursorValue
import com.aacomposetest.MyApplication
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.getValue

class SongsViewModel(context: Context): ViewModel() {

    private val dao = MyDatabaseProvider.getDatabase(context).songDao()
    private val repo = SongRepository(dao,viewModelScope)








    val getAllSongsFlow: Flow<List<Song>> = repo.allsongsFlow.map { list -> list.map { it.copy() } }

    val getSortedSongsStateFlow: Flow<List<Song>> =
        combine(
            getAllSongsFlow,
            sortTypeFlow,
            sortDirectionFlow
        ) { songs, type, direction ->
            when (type) {
                sortTypeOptions[0] -> songs.sortedBy { it.id }
                sortTypeOptions[1] -> songs.sortedBy { it.title }
                sortTypeOptions[2] -> songs.sortedBy { it.artistName }
                sortTypeOptions[3] -> songs.sortedBy { it.duration }
                else -> songs.sortedBy { it.id }
            }.let {sortedList->
                if (direction == sortDirectionOptions[0]) sortedList.reversed() else sortedList
            }

        }.stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            emptyList()
        )



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
        return withContext(viewModelScope.coroutineContext) {
            repo.getSongById(id)
        }
    }

    suspend fun resyncWithMediastore(context: Context, alsoPictures:Boolean, forceUpdate:Boolean, onFinish:( suspend ()->Unit)? = null):Int{

        return AndroidDatabase.doSongsQuery(
            viewmodel = this@SongsViewModel,
            context = context,
            alsoPictures = alsoPictures,
            forceUpdate = forceUpdate,
            onFinish = onFinish
        )

    }

    companion object{
        private val prefs by lazy {
            MyApplication.instance.applicationContext.getSharedPreferences(MyPrefs.PrefDirs.GenericData, Context.MODE_PRIVATE)
        }

        val sortTypeOptions = listOf<String>("DateAdded","Title","Artist","Length")
        val sortDirectionOptions = listOf<String>("Descending","Ascending")


        private val _sortTypeFlow = MutableStateFlow(prefs.getCursorValue(MyPrefs._prefs_globalSongsSortType,sortTypeOptions.first()))
        val sortTypeFlow: StateFlow<String> = _sortTypeFlow.asStateFlow()
        fun setSortType(type:String){
            _sortTypeFlow.value = type

        }

        private val _sortDirectionFlow = MutableStateFlow(
            prefs.getCursorValue(MyPrefs._prefs_globalSongsSortDirection, sortDirectionOptions.first())
        )
        val sortDirectionFlow: StateFlow<String> = _sortDirectionFlow.asStateFlow()
        fun setSortDirection(direction:String){
            _sortDirectionFlow.value = direction
        }
    }


}