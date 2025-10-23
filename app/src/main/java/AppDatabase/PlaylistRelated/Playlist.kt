package AppDatabase.PlaylistRelated

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.aacomposetest.Functions
import com.aacomposetest.MyApplication
import java.io.File
import com.aacomposetest.R

/**
 * Mutable AND user governed playlist item
 *
 * Songs can be moved around, added and removed at will.
 *
 * */
data class Playlist(var title:String) {


    var thumbNail: Int = Defaults.Thumbnail()
        get() = R.drawable.ic_launcher_foreground
        private set

    fun setThumbnail(context: Context, @DrawableRes id: Int){
        val _thumbnailString = context.resources.getResourceEntryName(id)

        thumbNail = context.resources.getIdentifier(_thumbnailString, "drawable", context.packageName)
    }

    // list of song IDs
    var songsIdList: ArrayList<Long> = ArrayList()

    fun save(context: Context,onFinished: (suspend () -> Unit)? = null){

        Functions.JsonOp.saveAsJson(
            context = context,
            filename = title,
            data = this,
            parent = getFolder(context),
            onFinished = onFinished
        )
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






    companion object {

        object Defaults{
            private val _thumbnailString = MyApplication.instance.resources.getResourceEntryName(R.drawable.ic_launcher_foreground)
            val Thumbnail
                get() = {
                    MyApplication.instance.resources.getIdentifier(_thumbnailString, "drawable", MyApplication.instance.packageName)
                }
        }

        val PLAYLISTS_DIR = "Playlists"

        fun getFolder(context: Context): File {
            val folder = File(context.filesDir, PLAYLISTS_DIR)
            if(!folder.exists()){
                folder.mkdir()
            }
            return folder
        }

        fun getPlaylist(context: Context, title:String?): Playlist?{


            return if(title!=null)Functions.JsonOp.syncLoadFromJson(context,title,getFolder(context)) else null
        }

        suspend fun getFromFile(file: File): Playlist?{
            val result: Playlist? = Functions.JsonOp.loadFromJson(file = file)

            return result
        }

    }
}