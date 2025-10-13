package AppDatabase.SongRelated


import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(song: Song)

    @Update
    suspend fun update(song: Song)

    @Delete
    suspend fun delete(song: Song)

    @Query("DELETE FROM songs WHERE ID = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM songs")
    fun getAllSongs(): Flow<List<Song>>

    @Query("SELECT * FROM songs WHERE id = :id")
    suspend fun getSongById(id:Long): Song?

    suspend fun upsert(song: Song) {
        val existing = getSongById(song.id)
        if (existing == null) insert(song)
        else update(song.copy(
            // merge or override only the fields you want
            title = song.title,
            songUri = song.songUri,
            artistName = song.artistName,
            duration = song.duration,
            relativePath = song.relativePath,
            albumName = song.albumName
        ))
    }



}