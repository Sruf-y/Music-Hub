package MyViewModels

import AppDatabase.MyDatabaseProvider
import AppDatabase.PlaylistRelated.Playlist
import AppDatabase.SongRelated.Song
import AppDatabase.SongRelated.SongRepository
import ExoPlayer.myExoPlayer
import android.content.Context
import android.os.FileObserver.CLOSE_WRITE
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.aacomposetest.Classes.MyPrefs
import com.aacomposetest.Classes.MyPrefs.getCursorValue
import com.aacomposetest.Classes.Setting
import com.aacomposetest.Functions.FileWatcher
import com.aacomposetest.Functions.FileWatcher.Companion.FileWatchListener
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
        MyPrefs.getPrefs(MyApplication.instance)
    }

    private val IOscope = CoroutineScope(Dispatchers.IO)

    // Using this in order to not create a SongViewModel for no reason
    private val dao = MyDatabaseProvider.getDatabase(MyApplication.instance).songDao()
    private val repo = SongRepository(dao)
    private val scope = CoroutineScope(Dispatchers.Default)




    private val playlistHolder = PlaylistHolder(IOscope)
    val allPlaylists = playlistHolder.allPlaylists // pass refference to the stateflow


    val playlistFolderWatcher:FileWatcher = FileWatcher(
    Playlist.getFolder(MyApplication.instance)
    )





    init {


        // watch the folder itself
        playlistFolderWatcher.addOnChangeListener(
            FileWatchListener(
                "folderWatcher",
                { event,path ->
                    playlistHolder.updateList(MyApplication.instance)
                }
            )
        )
        playlistHolder.updateList(MyApplication.instance)

        playlistFolderWatcher.startWatching()
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

        private val _sortTypeFlow = MutableStateFlow(
            prefs.getCursorValue(
                MyPrefs._prefs_globalSongsSortType,
            SongSortType.DateAdded.toString()
            )
        )
        val sortTypeFlow: StateFlow<String> = _sortTypeFlow.asStateFlow()
        fun setSortType(type:String){
            _sortTypeFlow.value = type

        }

        private val _sortDirectionFlow = MutableStateFlow(
            prefs.getCursorValue(
                MyPrefs._prefs_globalSongsSortDirection,
                SongSortDirection.Descending.toString()
            )
        )
        val sortDirectionFlow: StateFlow<String> = _sortDirectionFlow.asStateFlow()
        fun setSortDirection(direction:String){
            _sortDirectionFlow.value = direction
        }

        val sortedSongsFlow: StateFlow<List<Song>> =
            combine(
                allSongs,
                sortTypeFlow,
                sortDirectionFlow
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
                SharingStarted.Lazily,
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

    object ViewingPlaylist{

        val viewPlaylistScope: CoroutineScope = CoroutineScope(Dispatchers.Default)

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


        var curPlaylistTitle:MutableState<String?> = mutableStateOf(null)
            private  set

        val _curPlaylist = MutableStateFlow<Playlist?>(null)
        val curPlaylist = _curPlaylist.asStateFlow()

        val curPlaylistSongs: Flow<List<Song>> = combine(
            allSongs,
            curPlaylist
        ) { songs, playlist ->
            val songsById = songs.associateBy { it.id }

            playlist?.songsIdList?.mapNotNull { id ->
                songsById[id]
            } ?: emptyList()
        }.stateIn(
            viewPlaylistScope,
            SharingStarted.Eagerly,
            emptyList()
        )

        val playlistChangeCallback = FileWatchListener(
            "currentlyViewedPlaylsit",
            { event, name ->

                val myName = curPlaylist.value?.getFileIfExists(MyApplication.instance)?.name


                if(name == myName && event == CLOSE_WRITE){

                    _setCurPlaylist(MyApplication.instance,curPlaylist.value?.title)
                }
            }
        )

        private fun _setCurPlaylist(context:Context,title:String?){

            val playlist = Playlist.getPlaylist(context,title)
            curPlaylistTitle.value = title
            _curPlaylist.update { playlist }

        }

        fun enterCurPlaylist(context: Context,title: String?){
            _setCurPlaylist(context,title)
            playlistFolderWatcher.addOnChangeListener(playlistChangeCallback)
        }

        fun delCurPlaylist(){
            curPlaylistTitle.value = null
            _curPlaylist.update { null }
        }
        val filteredSongsList: StateFlow<List<Song>> =
            combine(
                allSongs,
                curPlaylistSongs,
                searchWordFlow
            ) { songs,cursongs,searchWord->
                cursongs.filter{ it->it.title.contains(other = searchWord, ignoreCase = true ) || searchWord.isBlank() }
            }.stateIn(
                viewPlaylistScope,
                SharingStarted.Eagerly,
                emptyList()
            )

        private val _sortTypeFlow = MutableStateFlow(
            prefs.getCursorValue(
                MyPrefs._prefs_playlistSongsSortType,
                SongSortType.DateAdded.name
            )
        )
        val sortTypeFlow: StateFlow<String> = _sortTypeFlow.asStateFlow()
        fun setSortType(type:String){
            _sortTypeFlow.value = type

        }

        private val _sortDirectionFlow = MutableStateFlow(
            prefs.getCursorValue(
                MyPrefs._prefs_playlistSongsSortDirection,
                SongSortDirection.Descending.name
            )
        )
        val sortDirectionFlow: StateFlow<String> = _sortDirectionFlow.asStateFlow()
        fun setSortDirection(direction:String){
            _sortDirectionFlow.value = direction
        }

        val sortedSongsFlow:Flow<List<Song>> =
            combine(
                curPlaylistSongs,
                sortTypeFlow,
                sortDirectionFlow
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
                viewPlaylistScope,
                SharingStarted.Lazily,
                emptyList()
            )

        val sortedAndFilteredSongsFlow: Flow<List<Song>> =
            combine(
                sortedSongsFlow,
                searchWordFlow
            ) { songs, searchWord ->

                songs.filter { it->
                    it.title.contains(other = searchWord, ignoreCase = true) || searchWord.isBlank()
                }

            }.stateIn(
                viewPlaylistScope,
                SharingStarted.Lazily,
                emptyList()
            )
    }

}

private class PlaylistHolder(val IOscope: CoroutineScope){
    val _allPlaylists = MutableStateFlow<List<Playlist>>(emptyList())
    val allPlaylists: StateFlow<List<Playlist>> = _allPlaylists.asStateFlow()

    fun updateList(context: Context,onfinished: (() -> Unit)? = null) {


        val filesList = Playlist.getFolder(context).listFiles()

        val newlist: ArrayList<Playlist> = ArrayList()

        if (filesList != null) {

            IOscope.launch {

                filesList.forEachIndexed { index, file ->

                    Playlist.getFromFile(file)?.let { loadedPlaylist ->
                        newlist.add(loadedPlaylist)
                    }

                }


                _allPlaylists.update { old->

                    newlist
                }
                onfinished?.invoke()
            }


        } else {
            _allPlaylists.update { old-> newlist  }
            onfinished?.invoke()
        }
    }

}