package AppDatabase.SongRelated

import kotlinx.coroutines.flow.Flow

class SongRepository(private val dao: SongDao) {
    val allsongs: Flow<List<Song>> = dao.getAllSongs()

    suspend fun addSong(song:Song) = dao.insert(song)

    suspend fun updateSong(song:Song) = dao.update(song)

    suspend fun upsert(song:Song) = dao.upsert(song)

    suspend fun getSongById(id:Long) = dao.getSongById(id)

    suspend fun deleteSong(song:Song) = dao.delete(song)

    suspend fun deleteSongById(id:Long) = dao.deleteById(id)
}