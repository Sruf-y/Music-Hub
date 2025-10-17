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

    object SortedSongs {
        private val _searchWord = MutableStateFlow("")
        val searchWordFlow: StateFlow<String> = _searchWord.asStateFlow()
        fun setSearchWord(word:String){
            _searchWord.value = word
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

        val sortedAndFilteredSongsFlow: Flow<List<Song>> =
            combine(
                allSongs,
                sortTypeFlow,
                sortDirectionFlow,
                searchWordFlow
            ) { songs, type, direction,searchWord ->
                when (type) {
                    sortTypeOptions[0] -> songs.sortedBy { it.id }
                    sortTypeOptions[1] -> songs.sortedBy { it.title }
                    sortTypeOptions[2] -> songs.sortedBy { it.artistName }
                    sortTypeOptions[3] -> songs.sortedBy { it.duration }
                    else -> songs.sortedBy { it.id }
                }.let {sortedList->
                    (if (direction == sortDirectionOptions[0]) sortedList.reversed() else sortedList)
                        .filter { it->it.title.contains(searchWord) || searchWord.isBlank() }


                }
            }.stateIn(
                songsVM.viewModelScope,
                SharingStarted.Eagerly,
                emptyList()
            )
    }

    object SortedPlaylists{
        private val _searchWord = MutableStateFlow("")
        val searchWordFlow: StateFlow<String> = _searchWord.asStateFlow()
        fun setSearchWord(word:String){
            _searchWord.value = word
        }

        val filteredPlaylists: Flow<List<Playlist>> =
            combine(
                allPlaylists,
                searchWordFlow
            ) { playlists,searchWord->
                playlists.filter { it->it.title.contains(searchWord) || searchWord.isBlank() }
            }.stateIn(
                playlistsVM.viewModelScope,
                SharingStarted.Eagerly,
                emptyList()
            )
    }

    object ViewingPlaylist{
        private val _searchWord = MutableStateFlow("")
        val searchWordFlow: StateFlow<String> = _searchWord.asStateFlow()
        fun setSearchWord(word:String){
            _searchWord.value = word
        }
    }




}