package MyViewModels

import AppDatabase.PlaylistRelated.Playlist
import AppDatabase.SongRelated.Song
import android.content.Context
import androidx.lifecycle.viewModelScope
import com.aacomposetest.Classes.MyPrefs
import com.aacomposetest.Classes.MyPrefs.getCursorValue
import com.aacomposetest.Classes.Setting
import com.aacomposetest.MyApplication
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update


object ListFeedingVM {

    private val prefs by lazy {
        MyApplication.instance.applicationContext.getSharedPreferences(MyPrefs.PrefDirs.GenericData, Context.MODE_PRIVATE)
    }

    private val songsVM = SongsViewModel(MyApplication.instance)
    private val playlistsVM = PlaylistViewModel(MyApplication.instance)


    val allSongs: Flow<List<Song>> = songsVM.getAllSongsFlow

    val allPlaylists: StateFlow<List<Playlist>> = playlistsVM.allPlaylists




    init {
        playlistsVM.updateList(MyApplication.instance)
        playlistsVM.startWatchingFolder()
    }



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

        val sortedSongsFlow:Flow<List<Song>> =
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
                songsVM.viewModelScope,
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
                songsVM.viewModelScope,
                SharingStarted.Lazily,
                emptyList()
            )
    }

    object SortedPlaylists{


        // I have removed filtering for playlists because its simply annoying. I might re-add it later in some way.
        val filteredPlaylists: Flow<List<Playlist>>
            get() = allPlaylists

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


        var curPlaylistTitle:String? = null
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
            playlistsVM.viewModelScope,
            SharingStarted.Eagerly,
            emptyList()
        )


        fun setCurPlaylist(context:Context,title:String?){
            val playlist = Playlist.getPlaylist(context,title)
            curPlaylistTitle = title
            _curPlaylist.update { playlist }

        }
        val filteredSongsList: StateFlow<List<Song>> =
            combine(
                allSongs,
                curPlaylistSongs,
                searchWordFlow
            ) { songs,cursongs,searchWord->
                cursongs.filter{ it->it.title.contains(other = searchWord, ignoreCase = true ) || searchWord.isBlank() }
            }.stateIn(
                playlistsVM.viewModelScope,
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
                songsVM.viewModelScope,
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
                songsVM.viewModelScope,
                SharingStarted.Lazily,
                emptyList()
            )
    }

}