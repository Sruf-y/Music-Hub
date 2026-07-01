package AppDatabase

import AppDatabase.PlaylistRelated.Playlist
import AppDatabase.SongRelated.Song
import ExoPlayer.myExoPlayer
import MyViewModels.SongsViewModel
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import android.util.Log
import android.util.Size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.net.toUri
import androidx.lifecycle.viewModelScope
import androidx.room.Database
import com.aacomposetest.Functions
import com.aacomposetest.Functions.Images
import com.aacomposetest.Log
import com.aacomposetest.Logs
import com.aacomposetest.MyApplication
import com.aacomposetest.concatenateWith
import com.aacomposetest.hasPermission
import com.aacomposetest.ui.theme.Theme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.io.File
import java.time.Duration
import java.time.LocalTime


object AndroidDatabase {
   
   private val SONGS_DIR = Song.SONGS_THUMBNAIL_DIRECTORY
   
   
   /**
    * At first use of the app: with pictures
    *
    * At start: without pictures to save on time
    *
    * On user manually requesting a refresh: With pictures. The user's requests are law
    * */
   suspend fun doSongsQuery(viewmodel: SongsViewModel?, context:Context, alsoPictures: Boolean = true,forceUpdate:Boolean = false,  onFinish: (suspend ()->Unit)? = null):Int =
      withContext(Dispatchers.IO) {
         
         //TODO handle permissions removal better
         if( false && !context.hasPermission(MyApplication.Permissions.READ_MEDIA_AUDIO)){
            onFinish?.invoke()
            finishRefresh()
            return@withContext 0
         }
         startRefresh()
         val contentResolver = context.contentResolver
         
         val FROM = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
         
         
         val queryStartTime: LocalTime = LocalTime.now()
         var counter = 0
         
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
            FROM, projection, selection, selectionArgs, null
         )
         
         val newExistings: ArrayList<Long> = ArrayList()
         
         
         
         cursor?.use { cursor ->
            while (cursor.moveToNext()) {
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
               val contentUri = ContentUris.withAppendedId(FROM, id)
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
               
               var thumbnailFile: File? = null
               var thumbFILE = File(File(context.filesDir, SONGS_DIR),contentUri.lastPathSegment.toString()+".jpeg")
               
               val sameSongIfExists = viewmodel?.getSongById(id)
               newExistings.add(id)
               
               if(sameSongIfExists?.lastModified != lastModified || !thumbFILE.exists() || forceUpdate) {
                  if (alsoPictures) {
                     
                     val thumbnailBitmap = try {
                        //decodeSampledBitmapFromUri(contentResolver,contentUri,400,400)
                        contentResolver.loadThumbnail(contentUri, Size(500, 500), null)
                     } catch (ex: Exception) {
                        null
                     }
                     
                     
                     // Save image
                     val folder = File(context.filesDir, SONGS_DIR)
                     if (thumbnailBitmap != null) {
                        thumbnailFile = Images.saveAsJpg(
                           folder,
                           contentUri.lastPathSegment.toString(),
                           thumbnailBitmap
                        )
                     }
                     
                  }
                  
                  
                  val song = Song(
                     id = id,
                     songUri = contentUri.toString(),
                     title = title,
                     artistName = author,
                     albumName = albumName,
                     relativePath = relativePath,
                     duration = duration,
                     thumbnailPath = thumbnailFile?.path?:"",
                     lastModified = lastModified,
                     mimeType = mimeType,
                     year = year,
                     size = size,
                     albumArtist = albumArtist?:"",
                     albumId = albumId
                  )
                  
                  
                  
                  
                  
                  
                  // Add to database or update
                  viewmodel?.upsert(song)
                  
                  counter++
               }
               
               
            }
            
            val databaseList = try {
               // Wait up to 10 seconds for the flow to emit
               withTimeout(10000) {
                  viewmodel?.getAllSongsFlow?.first {
                     it.isNotEmpty()
                  }
               }
            } catch (e: TimeoutCancellationException) {
               Log.w("SongQuery", "Timeout waiting for flow, using current value")
               viewmodel?.getAllSongsFlow?.value
            } catch (e: Exception) {
               Log.e("SongQuery", "Error getting flow", e)
               null
            }
            
            // Delete inexistent items from database
            
            databaseList?.let {databaseList->
               Log("New existings size: ${newExistings.size}. DatabaseList size: ${databaseList.size}")
               databaseList.forEach {
                  // if the newly written database doesn't contain an old item,
                  // consider the old item as inexistent in MediaStore
                  if(!newExistings.contains(it.id)){
                     Log("Deleting song ${it.title}")
                     // Deleting the song will also delete its thumbnail from memory.
                     viewmodel?.deleteSong(it.id,context)
                  }
               }
            }
            
            // Delete inexistent entry refferences from every playlist
            Playlist.getFolder(context).listFiles()?.forEach {
               val openedPlaylist: Playlist? = Playlist.getFromFile(it)
               
               openedPlaylist?.let { playlist->
                  // Walk the playlist
                  val newlist =  playlist.songsIdList.toMutableList().let {
                     it.toList().forEach { item ->
                        // Delete an item upon finding that it is not in newExistings
                        if (!newExistings.contains(item)) {
                           it.remove(element = item)
                        }
                     }
                     it
                  }
                  
                  playlist.songsIdList.clear()
                  playlist.songsIdList.addAll(newlist)
               }
            }
            
            Log.i(
               Logs.RUNNING_INFO.name,
               "(AndroidDataBase) Query took ${
                  Duration.between(queryStartTime, LocalTime.now()).toMillis()
               } miliseconds and had a lsit of size $counter"
            )
            
            
            
         }
         
         // If song was removed, remove the currently playing song too
         myExoPlayer.currentlyPlayingSong.getValue?.let {currenSong->
            if(!newExistings.contains(currenSong.id)){
               myExoPlayer.setSong(null,false)
            }
         }
         
         onFinish?.invoke()
         finishRefresh()
         
         return@withContext counter
      }
   
   var isRefreshingSongs by mutableStateOf(false)
      private set
   
   private fun startRefresh(){
      isRefreshingSongs = true
   }
   private fun finishRefresh(){
      isRefreshingSongs = false
   }
   
   
}