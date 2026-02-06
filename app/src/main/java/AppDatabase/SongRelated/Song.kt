package AppDatabase.SongRelated

import MyViewModels.SongsViewModel
import android.content.Context
import android.net.Uri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import java.io.File
@Serializable
@Entity(tableName = "songs")
data class Song (
    @PrimaryKey(autoGenerate = false)
    val id:Long,

    @ColumnInfo(name = "songUri")
    var songUri: String,
    @ColumnInfo(name = "title")
    var title:String = "<unknown>",
    @ColumnInfo(name = "artistName")
    var artistName: String = "<unknown>",
    @ColumnInfo(name = "albumName")
    var albumName:String,
    @ColumnInfo(name = "relativePath")
    var relativePath:String,
    @ColumnInfo(name = "duration")
    var duration:Long,
    @ColumnInfo(name = "thumbnailPath")
    var thumbnailPath:String,
    @ColumnInfo(name = "lastModified")
    var lastModified:Long,

    @ColumnInfo(name = "timesPlayed")
    var timesPlayed:Long = 0,

    @ColumnInfo(name = "album_artist")
    var albumArtist:String,
    @ColumnInfo(name = "size")
    var size:Long,
    @ColumnInfo(name = "mime_type")
    var mimeType:String,
    @ColumnInfo(name = "year")
    var year: Int?,
    @ColumnInfo(name = "album_id")
    var albumId:Long?


){


    /**
     * Returns the thumbnail file if it exists, else returns null
     * */
    fun tryGetThumbnail(context: Context): File?{

        val folder = File(context.filesDir,SONGS_THUMBNAIL_DIRECTORY)

        val myfile =  File(folder, id.toString()+".jpg")

        myfile.let {
            return if(it.exists())
                it
            else
                null
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Song

        if (id != other.id) return false
        if (songUri != other.songUri) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + songUri.hashCode()
        return result
    }


    companion object{
        val SONGS_THUMBNAIL_DIRECTORY = "Song Thumbnails"

        val defaultTitle = "<unknown>"

        fun emptySong(id: Long = -1,title:String =defaultTitle):Song{
            return Song(
                id = id,
                songUri = "",
                title = title,
                artistName = "<unknown artist>",
                albumName = "<unknown album>",
                relativePath = "<no path>",
                duration = 0,
                thumbnailPath = "<no thumbnail>",
                timesPlayed = 0,
                lastModified = 0,
                albumArtist = "<unknown albumArtist>",
                size = 0L,
                year = 0,
                mimeType = "<unknown mimeType>",
                albumId = null
            )
        }
    }


}