package AppDatabase.PlaylistRelated

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.musichub.Functions
import com.musichub.Functions.rename
import com.musichub.Log
import com.musichub.Logs
import com.musichub.MyApplication
import java.io.File
import com.musichub.R
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure
import kotlinx.serialization.serializer
import kotlin.random.Random
import kotlin.random.nextInt
import kotlin.random.nextLong

/**
 * Mutable AND user governed playlist item
 *
 * Songs can be moved around, added and removed at will.
 *
 * */
@Serializable()
class Playlist constructor(
    val title: String
)
{
    

    /**
     * List of song IDs
     * */
    var songsIdList: ArrayList<Long> = ArrayList()

    var thumbNail: Int = Defaults.Thumbnail()
        get() = R.drawable.ic_launcher_foreground
        private set

    fun setThumbnail(context: Context, @DrawableRes id: Int){
        val _thumbnailString = context.resources.getResourceEntryName(id)
        
        thumbNail = context.resources.getIdentifier(_thumbnailString, "drawable", context.packageName)
    }



    fun save(context: Context):File{

        return internalSaveJustData(title,context)
    }
    
    private fun internalSaveJustData(filename:String,context: Context): File {
        return Functions.JsonOp.saveAsJson(
            context = context,
            filename = filename,
            data = this.toIntermediary(),
            parent = getFolder(context),
        )
    }

    fun delete(context: Context): Boolean{
        return getFileIfExists(context)?.delete() == true
    }
    
    fun rename(name:String,context: Context,overwrite: Boolean = false): File? {
        return getFileIfExists(context)?.rename(name,overwrite)
    }


    fun getFileIfExists(context: Context): File?{
        val file = File(getFolder(context), "$title.json")

        file.exists().let {
            return if(it){
                file
            }else{
                null
            }
        }
    }

    fun hasNext(curentId:Long):Boolean{
        val curIndex = songsIdList.indexOf(curentId)

        try{
            songsIdList.get(curIndex+1)
            return true
        }catch (ex: IndexOutOfBoundsException){
            return false
        }

    }



    fun getNextId(curentId: Long):Long?{
        if(hasNext(curentId)) {
            val curIndex = songsIdList.indexOf(curentId)
            return songsIdList.get(curIndex+1)
        }
        else{
            return null
        }
    }

    fun hasPrevious(curentId:Long): Boolean{
        val curIndex = songsIdList.indexOf(curentId)

        try{
            songsIdList.get(curIndex-1)
            return true
        }catch (ex: IndexOutOfBoundsException){
            return false
        }
    }

    fun getPreviousId(curentId:Long):Long?{



        if(hasPrevious(curentId)) {
            val curIndex = songsIdList.indexOf(curentId)
            return songsIdList.get(curIndex-1)
        }
        else{
            return null
        }
    }
    
    fun toIntermediary(): IntermediaryPlaylist{
        return IntermediaryPlaylist(songsIdList)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Playlist

        if (thumbNail != other.thumbNail) return false
        if (title != other.title) return false
        if (songsIdList != other.songsIdList) return false

        return true
    }

    override fun hashCode(): Int {
        var result = thumbNail
        result = 31 * result + title.hashCode()
        result = 31 * result + songsIdList.hashCode()
        return result
    }


    fun copy(title:String = this.title, idList: ArrayList<Long> = this.songsIdList, thumbnail:Int = this.thumbNail): Playlist{
        val aux = this.copy(title = title)
        aux.songsIdList = idList
        aux.thumbNail = thumbnail

        return aux
    }

    companion object {
        
        object Defaults{
            private val _thumbnailString = MyApplication.instance.resources.getResourceEntryName(R.drawable.ic_launcher_foreground)
            val Thumbnail
                get() = {
                    MyApplication.instance.resources.getIdentifier(_thumbnailString, "drawable", MyApplication.instance.packageName)
                }
        }

        val PLAYLISTS_DIR = "Playlists"
        val SHUFFLE_DIR = "Shuffled_Playlist"

        fun getFolder(context: Context): File {
            val folder = File(context.filesDir, PLAYLISTS_DIR)
            if(!folder.exists()){
                folder.mkdir()
            }
            return folder
        }

        fun getShuffleFLoder(context:Context):File{
            val folder = File(context.filesDir, SHUFFLE_DIR)

            if(!folder.exists()){
                folder.mkdir()
            }
            return folder
        }

        fun getShufflePlaylistFile(context:Context):File{

            val folder = getShuffleFLoder(context)

            val file = File(folder,"Shuffle.json")
            if(!file.exists()){
                file.createNewFile()
            }
            return file
        }

        fun getPlaylist(context: Context, title:String?): Playlist?{

            return if(title!=null) {
                (Functions.JsonOp.syncLoadFromJson(context, title, getFolder(context)) as? IntermediaryPlaylist)?.toPlaylist(title)
            } else null
        }
        
        fun getFileIfExists(context: Context,title:String): File?{
            val file = File(getFolder(context), "$title.json")
            
            file.exists().let {
                return if(it){
                    file
                }else{
                    null
                }
            }
        }

        suspend fun getFromFile(file: File): Playlist?{
            val result: IntermediaryPlaylist? = Functions.JsonOp.loadFromJson(file = file)

            return result?.toPlaylist(file.nameWithoutExtension)
        }

    }
}

@Serializable
data class IntermediaryPlaylist(
    val songsIdList: ArrayList<Long>
){
    fun toPlaylist(title:String): Playlist {
        return Playlist(title).let {
            it.songsIdList = this.songsIdList
            //it.setThumbnail() = this.thumbNail // TODO make actual thumbnail recovering
            it
        }
    }
}