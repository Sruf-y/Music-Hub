package AppDatabase.SongRelated

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SongRepository(private val dao: SongDao,coroutineScope: CoroutineScope) {

//    private val _allSongsStateFlow = MutableStateFlow<List<Song>>(emptyList<Song>())
//    val allSongsStateFlow: StateFlow<List<Song>> = _allSongsStateFlow

    init {
//        coroutineScope.launch {
//            dao.getAllSongs().collect {songs->
//                _allSongsStateFlow.value = songs
//            }
//        }
    }

    val allsongsFlow: Flow<List<Song>> = dao.getAllSongs()

    suspend fun addSong(song:Song) = dao.insert(song)

    suspend fun updateSong(song:Song) = dao.update(song)

    suspend fun upsert(song:Song) = dao.upsert(song)

    suspend fun getSongById(id:Long) = dao.getSongById(id)

    suspend fun deleteSong(song:Song) = dao.delete(song)

    suspend fun deleteSongById(id:Long) = dao.deleteById(id)
}