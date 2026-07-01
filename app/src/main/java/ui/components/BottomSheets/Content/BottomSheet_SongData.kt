package ui.components.BottomSheets.Content

import AppDatabase.SongRelated.Song
import SrufComposeComponents.Text
import android.content.ContentResolver
import android.content.ContentUris
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BrushPainter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.aacomposetest.MyApplication
import com.aacomposetest.toDp
import com.aacomposetest.toDpSize
import com.aacomposetest.ui.theme.MyColor
import com.aacomposetest.ui.theme.Theme
import com.composables.core.HorizontalSeparator
import java.io.File
import java.time.LocalDateTime
import java.util.Date

@Composable
fun BottomSheet_SongData(song:Song?){
   val data = remember {
      queryMediaStoreFile(
         uri = song?.songUri?.toUri(),
         contentResolver = MyApplication.instance.contentResolver,
      )
   }
   var intsize by remember { mutableStateOf(IntSize(0,0)) }
   
   Box(Modifier.wrapContentSize().onGloballyPositioned{
      intsize = it.size
   }){
      
      val density = LocalDensity.current
      
      Theme.RenderDefaultBackground(modifier = Modifier.size(intsize.toDpSize(density)))
      
      Column(
         Modifier
            .fillMaxWidth()
            .padding(15.dp)
            
      ) {
         
         Text("Information", fontSize = 24.sp, modifier = Modifier
            .fillMaxWidth()
            .background(Theme.HintColor()),
            fontWeight = FontWeight.Bold
         )
         data?.apply {
            DataItem("ID", id.toString());                                          HorizontalSeparator(Theme.TextColor())
            DataItem("Display Name", displayName);                                  HorizontalSeparator(Theme.TextColor())
            DataItem("Path", path ?: "N/A");                                        HorizontalSeparator(Theme.TextColor())
            DataItem("Size", getFormattedSize());                                   HorizontalSeparator(Theme.TextColor())
            DataItem("MIME Type", mimeType ?: "Unknown");                           HorizontalSeparator(MyColor.TextColor())
            DataItem("Date Added", Date(dateAdded*1000).toString());          HorizontalSeparator(MyColor.TextColor())
            DataItem("Date Modified", Date(dateModified*1000).toString());    HorizontalSeparator(MyColor.TextColor())
            DataItem("URI", uri.toString());                                        HorizontalSeparator(MyColor.TextColor())
            DataItem("Times played",song!!.timesPlayed.toString())
         }
         
         
         
      }
   }
   
}

@Composable
private fun DataItem(text:String,value:String){
   Row(
      modifier = Modifier
         .fillMaxWidth()
         .heightIn(45.dp)
         .background(Theme.SemiTransparent()),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
   ){
      Text(text, modifier = Modifier.weight(1f))
      
      Text(value, fontSize = 14.sp, modifier = Modifier.weight(1f))
   }
}

data class MediaStoreFileInfo(
   val id: Long,
   val displayName: String,
   val path: String?,
   val size: Long,
   val mimeType: String?,
   val dateAdded: Long,
   val dateModified: Long,
   val uri: Uri,
) {
   fun getFormattedSize(): String = when {
      size < 1024 -> "$size B"
      size < 1024 * 1024 -> String.format(Locale.current.platformLocale,"%.2f KB", size / 1024.0)
      size < 1024 * 1024 * 1024 -> String.format(Locale.current.platformLocale,"%.2f MB", size / (1024.0 * 1024.0))
      else -> String.format(Locale.current.platformLocale,"%.2f GB", size / (1024.0 * 1024.0 * 1024.0))
   }
}

/**
 * Query a file from MediaStore using its URI
 */
fun queryMediaStoreFile(uri: Uri?, contentResolver: ContentResolver): MediaStoreFileInfo? {
   var cursor: Cursor? = null
   
   return try {
      if(uri == null){
         return null
      }
      // For content URIs from MediaStore
      if (uri.scheme == "content" && uri.authority == "media") {
         val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.SIZE,
            MediaStore.Files.FileColumns.MIME_TYPE,
            MediaStore.Files.FileColumns.DATE_ADDED,
            MediaStore.Files.FileColumns.DATE_MODIFIED,
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns.RELATIVE_PATH
         )
         
         cursor = contentResolver.query(
            uri,
            projection,
            null,
            null,
            null
         )
         
         cursor?.use { c ->
            if (c.moveToFirst()) {
               val id = c.getLong(c.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID))
               val displayName = c.getString(c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)) ?: "unknown"
               val size = c.getLong(c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE))
               val mimeType = c.getString(c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE))
               val dateAdded = c.getLong(c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_ADDED))
               val dateModified = c.getLong(c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_MODIFIED))
               val path = c.getString(c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA))
               val relativePath = c.getString(c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.RELATIVE_PATH))
               
               return MediaStoreFileInfo(
                  id = id,
                  displayName = displayName,
                  size = size,
                  mimeType = mimeType,
                  dateAdded = dateAdded,
                  dateModified = dateModified,
                  path = path ?: relativePath,
                  uri = uri
               )
            }
         }
      }
      return null
      
   } catch (e: Exception) {
      e.printStackTrace()
      return null
   } finally {
      cursor?.close()
      
   }
}