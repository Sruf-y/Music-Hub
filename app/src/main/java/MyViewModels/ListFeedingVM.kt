package MyViewModels

import AppDatabase.MyDatabaseProvider
import AppDatabase.PlaylistRelated.Playlist
import AppDatabase.SongRelated.Song
import AppDatabase.SongRelated.SongRepository
import MyViewModels.FileWatcher.Companion.FileWatchListener
import android.content.Context
import android.os.FileObserver
import com.aacomposetest.Classes.AutoStateFlow
import com.aacomposetest.Classes.MyPrefs
import com.aacomposetest.Classes.Setting
import com.aacomposetest.Classes.getCursorValue
import com.aacomposetest.Log
import com.aacomposetest.MyApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


object ListFeedingVM {

    private val prefs by lazy {
        MyPrefs.getPrefs(MyApplication.instance, MyPrefs.PrefDirs.VisualData)
    }

    private val IOscope = CoroutineScope(Dispatchers.IO)

    // Using this in order to not create a SongViewModel for no reason
    private val dao = MyDatabaseProvider.getDatabase(MyApplication.instance).songDao()
    private val repo = SongRepository(dao)
    private val scope = CoroutineScope(Dispatchers.Default)




    private val playlistHolder = PlaylistLISTHolder(IOscope)
    val allPlaylists = playlistHolder.allPlaylists // pass refference to the stateflow

    


    init {
        
        // Add listener listening to any folder changes.
        MyApplication.playlistFolderWatcher.addOnChangeListener(
            FileWatchListener(
                key = "folderWatcher",
                eventFilter = {
                    it and (FileObserver.CLOSE_WRITE or
                       FileObserver.DELETE or
                       FileObserver.DELETE_SELF or
                       FileObserver.MOVE_SELF or
                       FileObserver.MOVE_SELF
                    )>0
                },
                onEvent = { event,filename ->
                    playlistHolder.updateList(MyApplication.instance)
                }
            )
        )
        playlistHolder.updateList(MyApplication.instance)
        
    }





    val allSongs: StateFlow<List<Song>> = repo.allsongsFlow.stateIn(
        scope,
        SharingStarted.Eagerly,
        emptyList()
    )



    // For when i implement settings. Right now these do nothing.
    private val allSettings: List<Setting<Any>>? = null
    private var sortedSettings: List<Setting<Any>>? = null

    enum class SongSortType{
        DateAdded,
        Title,
        Artist,
        Length
    }

    enum class SongSortDirection{
        Descending,
        Ascending
    }

    object SortedSongs {

        val songsScope: CoroutineScope = CoroutineScope(Dispatchers.Default)

        private val _isSearching = MutableStateFlow(false)
        val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()
        fun setSearching(value: Boolean){
            _isSearching.value = value
        }
        private val _searchWord = MutableStateFlow("")
        val searchWordFlow: StateFlow<String> = _searchWord.asStateFlow()
        fun setSearchWord(word:String){
            _searchWord.value = word
        }

        
        val sortTypeFlow = AutoStateFlow(
            prefs.getCursorValue(
                MyPrefs._prefs_globalSongsSortType,
                SongSortType.DateAdded.toString()
            )
        )
        fun setSortType(type:String){
            sortTypeFlow.setValue(type)
        }

        val sortDirectionFlow = AutoStateFlow(
            prefs.getCursorValue(
                MyPrefs._prefs_globalSongsSortDirection,
                SongSortDirection.Ascending.toString()
            )
        )
        fun setSortDirection(direction:String){
            sortDirectionFlow.setValue(direction)
        }

        val sortedSongsFlow: StateFlow<List<Song>> =
            combine(
                allSongs,
                sortTypeFlow.stateFlow,
                sortDirectionFlow.stateFlow
            ) { songs,sortType,sortDirection->
                when(SongSortType.valueOf(sortType)){
                    SongSortType.DateAdded -> songs.sortedBy { it.id }
                    SongSortType.Title -> songs.sortedBy { it.title }
                    SongSortType.Artist -> songs.sortedBy { it.artistName }
                    SongSortType.Length -> songs.sortedBy { it.duration }
                }.let { sortedList->
                    when(SongSortDirection.valueOf(sortDirection)){
                        SongSortDirection.Descending -> sortedList.reversed()
                        SongSortDirection.Ascending -> sortedList
                    }
                }
            }.stateIn(
                songsScope,
                SharingStarted.Eagerly,
                emptyList()
            )

        val sortedAndFilteredSongsFlow: StateFlow<List<Song>> =
            combine(
                sortedSongsFlow,
                searchWordFlow
            ) { songs, searchWord ->

                songs.filter { it->
                    it.title.contains(other = searchWord, ignoreCase = true) || searchWord.isBlank()
                }

            }.stateIn(
                songsScope,
                SharingStarted.Lazily,
                emptyList()
            )
    }

    object SortedPlaylists{


        // I have removed filtering for playlists because its simply annoying. I might re-add it later in some way.

        val filteredPlaylists: Flow<List<Playlist>> = allPlaylists

//        val filteredPlaylists: Flow<List<Playlist>> =
//            combine(
//                allPlaylists,
//                searchWordFlow
//            ) { playlists,searchWord->
//                playlists.filter { it->it.title.contains(other = searchWord, ignoreCase = true ) || searchWord.isBlank() }
//            }.stateIn(
//                playlistsVM.viewModelScope,
//                SharingStarted.Lazily,
//                emptyList()
//            )
    }



}

private class PlaylistLISTHolder(val IOscope: CoroutineScope){
    val _allPlaylists = MutableStateFlow<List<Playlist>>(emptyList())
    val allPlaylists: StateFlow<List<Playlist>> = _allPlaylists.asStateFlow()

    fun updateList(context: Context,onfinished: (() -> Unit)? = null) {


        val filesList = Playlist.getFolder(context).listFiles()

        val newlist: ArrayList<Playlist> = ArrayList()

        if (filesList != null) {

            IOscope.launch {

                filesList.sortedBy { it.nameWithoutExtension }.forEachIndexed { index, file ->

                    Playlist.getFromFile(file)?.let { loadedPlaylist ->
                        newlist.add(loadedPlaylist)
                    }

                }

                Log("New list is ${newlist.map{it.title}}")
                _allPlaylists.value = newlist
                
                onfinished?.invoke()
            }


        } else {
            _allPlaylists.value = newlist
            onfinished?.invoke()
        }
    }

}