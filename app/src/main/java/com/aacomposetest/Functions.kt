package com.aacomposetest

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.net.Uri
import android.os.FileObserver
import android.os.FileObserver.CLOSE_WRITE
import android.os.FileObserver.MOVE_SELF
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object Functions {

    fun List<String>.concatenateWith(divider:String ="", exceptForElements:List<Int>):String{
        val stringBuilder = StringBuilder(this.first())

        this.forEachIndexed { index,string->
            if(!exceptForElements.contains(index) && index!=0) {
                stringBuilder.append(divider)
                stringBuilder.append(string)
            }
        }
        return stringBuilder.toString()
    }



    object Images{

        fun getBitmapFromDrawable(context:Context,drawableResId: Int): Bitmap {
            val drawable = ContextCompat.getDrawable(context, drawableResId)!!
            val bitmap = createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight)
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            return bitmap
        }


        suspend fun saveAsJpg(directory:File , filename: String, bitmap: Bitmap?): File? =
            withContext(Dispatchers.IO)
            {

                // Early return if bitmap is null
                if (bitmap == null) {
                    Log.e(Logs.DATA_ERROR.name, "Cannot save null bitmap")
                    return@withContext null
                }

                var target_dir: File = directory


                if (!target_dir.exists()) {
                    target_dir.mkdirs()
                }


                val fullFilename = filename.replace(".jpg", "") + ".jpg"
                val file = File(target_dir, fullFilename) // Use proper context path
                val newbitmap = bitmap
                return@withContext try {

                    // Use auto-closing stream
                    FileOutputStream(file).use { outputStream ->


                        if (!newbitmap.compress(Bitmap.CompressFormat.JPEG, 92, outputStream)) {
                            throw IOException("Failed to compress bitmap")
                        }
                    }

                    return@withContext file
                } catch (e: IOException) {
                    // Clean up if something went wrong
                    if (file.exists()) {
                        file.delete()
                        return@withContext null
                    }
                    Log.e("PhotoSave", "Error saving photo", e)
                    return@withContext null
                }
            }





        /**
         * Example of usage: val bitmap = decodeSampledBitmapFromUri(contentResolver, contentUri, 1000, 1000)
         * */
        fun decodeSampledBitmapFromUri(contentResolver: ContentResolver, uri: Uri, reqWidth: Int, reqHeight: Int): Bitmap? {
            return try {
                // Step 1: Decode with inJustDecodeBounds=true to get image dimensions
                val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }

                contentResolver.openInputStream(uri)?.use { inputStream ->
                    BitmapFactory.decodeStream(inputStream, null, options)
                }

                // Step 2: Calculate inSampleSize
                options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
                options.inJustDecodeBounds = false

                // Step 3: Decode bitmap with inSampleSize set
                contentResolver.openInputStream(uri)?.use { inputStream ->
                    BitmapFactory.decodeStream(inputStream, null, options)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

        private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
            val height = options.outHeight
            val width = options.outWidth
            var inSampleSize = 1

            if (height > reqHeight || width > reqWidth) {
                val halfHeight = height / 2
                val halfWidth = width / 2

                while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                    inSampleSize *= 2
                }
            }
            return inSampleSize
        }

    }

//    fun map(x:Float, in_min:Float, in_max:Float, out_min:Float, out_max:Float):Float {
//        return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
//    }
    fun Float.map(in_min:Float, in_max:Float, out_min:Float, out_max:Float):Float {
        return (this - in_min) * (out_max - out_min) / (in_max - in_min) + out_min
    }

    object JsonOp{
        private val jsonMutex: Mutex = Mutex()

        private val eventMask = (CLOSE_WRITE or MOVE_SELF)

        class JsonFileObserver(file: File, val onFileChanged:()->Unit):FileObserver(file,eventMask){

            override fun onEvent(event: Int, path: String?) {
                if((event and eventMask) ==0){
                    onFileChanged.invoke()
                }


            }
        }

        /**
         * To be used with a state value. Example
         *
         *      val mystring by remember { mutableStateOf("my string") }
         *      playlist.watchFile(
         *          {
         *              mystring = file.readText()
         *          }
         *      )
         *
         * To display the text from a file at all times dinamicly.
         *
         * */
        @Composable
        fun watchFile(fileState:File?, onFileChange:()-> Unit){


            fileState?.path?.let {
                DisposableEffect(fileState.path) {
                    val observer = JsonFileObserver(
                        file = fileState,
                        onFileChanged = onFileChange
                    )

                    observer.startWatching()

                    onDispose { observer.stopWatching() }
                }
            }
        }

        fun saveAsJson(context: Context, filename:String, data:Any,parent:File=context.filesDir,onFinished:(suspend ()->Unit)? = null) {

            CoroutineScope(Dispatchers.IO).launch {
                jsonMutex.withLock {
                    if(!parent.exists()){
                        parent.mkdirs()
                    }

                    val json=Gson().toJson(data);
                    val Filename= "$filename.json"

                    File(parent,Filename).writeText(json)

                    withContext(Dispatchers.Main) {
                        onFinished?.invoke()
                    }

                }
            }
        }






        suspend inline fun <reified T> loadFromJson(
            context: Context,
            filename: String,
            parent: File = context.filesDir
        ): T? {
            var Filename = filename

            if (!filename.endsWith(".json"))
                Filename = "$filename.json"

            val file = File(parent, Filename)


            return withContext(Dispatchers.IO) {
                if (file.exists()) {
                    val loadedFile = file.readText()
                    val type = object : TypeToken<T>() {}.type
                    val readData: T = Gson().fromJson(loadedFile, type)
                    return@withContext readData
                }
                return@withContext null
            }
        }

        inline fun <reified T> syncLoadFromJson(
            context: Context,
            filename: String,
            parent: File = context.filesDir
        ): T? {
            var Filename = filename

            if (!filename.endsWith(".json"))
                Filename = "$filename.json"

            val file = File(parent, Filename)



            if (file.exists()) {
                val loadedFile = file.readText()
                val type = object : TypeToken<T>() {}.type
                val readData: T = Gson().fromJson(loadedFile, type)
                return readData
            }
            return null

        }

        suspend inline fun <reified T> loadFromJson(file: File?): T? {

            return withContext(Dispatchers.IO) {
                file?.let {
                    if (file.exists()) {
                        val loadedFile = file.readText()
                        val type = object : TypeToken<T>() {}.type
                        val readData: T = Gson().fromJson(loadedFile, type)
                        return@withContext readData
                    }
                    return@withContext null
                }
                return@withContext null

            }
        }

        inline fun <reified T> syncLoadFromJson(file: File?): T? {


            file?.let {
                if (file.exists()) {
                    val loadedFile = file.readText()
                    val type = object : TypeToken<T>() {}.type
                    val readData: T = Gson().fromJson(loadedFile, type)
                    return readData
                }
            }
            return null


        }
    }

    class FolderWatcher(
        private val folder: File,
        private val onChange: () -> Unit
    )
    {
        private var observer: FileObserver? = null

        fun startWatching() {
            if (observer != null) return

            observer = object : FileObserver(folder, CREATE or DELETE or MODIFY or MOVED_TO or MOVED_FROM) {
                override fun onEvent(event: Int, path: String?) {
                    if (event and (CREATE or DELETE or MODIFY or MOVED_TO or MOVED_FROM) != 0) {
                        onChange()
                    }
                }
            }
            observer?.startWatching()
        }

        fun stopWatching() {
            observer?.stopWatching()
            observer = null
        }
    }

    /**
     * Renames the file and accounts for the extension.
     *
     * Do not add the extension to the file's name in the [string] parameter
     * */
    fun File.rename(string:String):File?{
        if(this.exists()){
            val filepath = this.toPath()

            val newExtension = if(this.extension.isNotEmpty())".${this.extension}" else ""


            val newfile = File(this.parent,string+newExtension)

            this.renameTo(newfile)

            return newfile
        }
        else {
            Log.e("ERROR","File does not exist")
            return null
        }
    }
}

