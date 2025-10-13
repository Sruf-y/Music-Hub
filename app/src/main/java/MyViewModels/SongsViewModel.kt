package MyViewModels

import AppDatabase.AndroidDatabase
import AppDatabase.MyDatabaseProvider
import AppDatabase.SongRelated.Song
import AppDatabase.SongRelated.SongRepository
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SongsViewModel(context: Context): ViewModel() {

    private val dao = MyDatabaseProvider.getDatabase(context).songDao()
    private val repo = SongRepository(dao)


    fun getAllSongsFlow(): Flow<List<Song>> = repo.allsongs.map { list ->
        list.map { it.copy() } // ensures new object references
    }

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

    suspend fun getNextSong(song: Song): Song?{
        val snapshot = getAllSongsFlow().firstOrNull()

        snapshot?.let {
            val curIndex = it.indexOf(song)
            if(it.contains(song)){
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

    suspend fun getNextSongById(id:Long): Song?{
        val snapshot = getAllSongsFlow().firstOrNull()

        snapshot?.let {
            val mappedSnapshot = snapshot.map{it->it.id}
            val curIndex = mappedSnapshot.indexOf(id)
            if(mappedSnapshot.contains(id) ){
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

    suspend fun getPreviousSong(song:Song):Song?{
        val snapshot = getAllSongsFlow().firstOrNull()

        snapshot?.let {
            val curIndex = it.indexOf(song)
            if(it.contains(song)){

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

    suspend fun getPreviousSongById(id:Long):Song?{
        val snapshot = getAllSongsFlow().firstOrNull()

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

    suspend fun resyncWithMediastore(context: Context, alsoPictures:Boolean, onFinish:( suspend ()->Unit)? = null):Int{

        return AndroidDatabase.doSongsQuery(
            viewmodel = this@SongsViewModel,
            context = context,
            alsoPictures = alsoPictures,
            onFinish = onFinish
        )

    }




}