package MyViewModels

import AppDatabase.AndroidDatabase
import AppDatabase.MyDatabaseProvider
import AppDatabase.SongRelated.Song
import AppDatabase.SongRelated.SongRepository
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.util.Size
import androidx.annotation.RequiresOptIn
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.room.Delete
import com.aacomposetest.Functions.concatenateWith
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SongsViewModel(context: Context): ViewModel() {

    private val dao = MyDatabaseProvider.getDatabase(context).songDao()
    private val repo = SongRepository(dao)

    init {
        // i'll keep it like this because this way i can manually influence on-screen songs
        viewModelScope.launch {
            repo.allsongsFlow.collect{songs->
                _songsAsFlow.value = songs
            }
        }

    }


    private val _songsAsFlow = MutableStateFlow<List<Song>>(emptyList())
    val getAllSongsFlow = _songsAsFlow.asStateFlow()

    // Value used for playNextSong and playPreviousSong
    private val getSortedSongsStateFlow
        get() = ListFeedingVM.SortedSongs.sortedSongsFlow


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
        return repo.getSongById(id)
    }

    fun unsafeGetSongById(id: Long): Song?{
        return repo.unsafeGetSongById(id)
    }
    /**
     * TODO: Create functions also for singleResync and multipleResync
     * */
    suspend fun resyncALL_withMediaStore(context: Context, alsoPictures:Boolean, forceUpdate:Boolean, onFinish:( suspend ()->Unit)? = null):Int{

        return AndroidDatabase.doSongsQuery(
            viewmodel = this@SongsViewModel,
            context = context,
            alsoPictures = alsoPictures,
            forceUpdate = forceUpdate,
            onFinish = onFinish
        )
    }
    
    suspend fun getSongFromUri(uri: Uri, context:Context):Song?{
        
        return withContext(Dispatchers.IO) {
            val contentResolver = context.contentResolver
            
            
            val projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DATE_MODIFIED,
                MediaStore.Audio.Media.RELATIVE_PATH,
                MediaStore.Audio.Media.GENRE,
                
                MediaStore.Audio.Media.ALBUM_ARTIST,
                MediaStore.Audio.Media.SIZE,
                MediaStore.Audio.Media.MIME_TYPE,
                MediaStore.Audio.Media.YEAR,
                MediaStore.Audio.Media.ALBUM_ID
            )
            
            val selection = null //"${MediaStore.Audio.Media.RELATIVE_PATH} LIKE ?"
            val selectionArgs = null //arrayOf("%")
            
            
            val cursor = contentResolver.query(
                uri, projection, selection, selectionArgs, null
            )
            
           
           return@withContext cursor?.use { cursor ->
               if(cursor.moveToFirst()) {
                   val id =
                       cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID))
                   val display_name =
                       cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME))
                   val pieces = display_name.split(".")
                   var title = pieces.concatenateWith(".", listOf(pieces.size - 1))
                   
                   val duration =
                       cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION))
                   val author =
                       cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST))
                   val albumName =
                       cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM))
                   val contentUri = uri
                   val relativePath =
                       cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.RELATIVE_PATH))
                   val lastModified =
                       cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_MODIFIED))
                   val albumArtist =
                       cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ARTIST))
                   val size =
                       cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE))
                   val mimeType =
                       cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE))
                   val year =
                       cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR))
                   val albumId =
                       cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID))
                   
                   
                   
                   
                   
                   return@withContext  Song(
                       id = id,
                       songUri = contentUri.toString(),
                       title = title,
                       artistName = author,
                       albumName = albumName,
                       relativePath = relativePath,
                       duration = duration,
                       thumbnailPath = "",
                       lastModified = lastModified,
                       mimeType = mimeType,
                       year = year,
                       size = size,
                       albumArtist = albumArtist?:"",
                       albumId = albumId
                   )
                   
                   
               }
               else{
                   return@withContext null
               }
            
            }
            
        }
        
        
    }
    
    /**
     * @throws java.io.IOException if any issues happen when trying to load
     * */
    suspend fun getThumbnailForSong(uri:Uri,context: Context): Bitmap {
        return context.contentResolver.loadThumbnail(uri, Size(500, 500), null)
    }

}

class SongsViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SongsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SongsViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}