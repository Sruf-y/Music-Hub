package AppDatabase.SongRelated

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SongRepository(private val dao: SongDao) {

    val allsongsFlow: Flow<List<Song>> = dao.getAllSongs()

    suspend fun addSong(song:Song) = dao.insert(song)

    suspend fun updateSong(song:Song) = dao.update(song)

    suspend fun upsert(song:Song) = dao.upsert(song)

    suspend fun getSongById(id:Long) = dao.getSongById(id)

    fun unsafeGetSongById(id:Long) = dao.unsafeGetSongById(id)

    suspend fun deleteSong(song:Song) = dao.delete(song)

    suspend fun deleteSongById(id:Long) = dao.deleteById(id)
}