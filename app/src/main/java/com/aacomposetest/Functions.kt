package com.aacomposetest

import android.content.ContentResolver
import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.Color
import android.graphics.RenderNode
import android.net.Uri
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.util.Range
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewTreeObserver
import android.widget.EdgeEffect
import android.widget.TextView
import androidx.activity.SystemBarStyle
import androidx.annotation.ColorInt
import androidx.annotation.RequiresApi
import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.OverscrollEffect
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.neverEqualPolicy
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.NativeCanvas
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.draw
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.SuspendingPointerInputModifierNode
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.node.DelegatingNode
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import com.aacomposetest.Classes.AndroidEdgeEffectOverscrollEffect
import com.aacomposetest.Classes.EdgeEffectDefaults
import com.aacomposetest.ui.theme.MyColor
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
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.roundToInt
import kotlin.random.Random

object Functions {
   
   
   
   
   
   object Images{
      
      fun getBitmapFromDrawable(context:Context,drawableResId: Int): Bitmap {
         val drawable = ContextCompat.getDrawable(context, drawableResId)!!
         val bitmap = createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight)
         val canvas = android.graphics.Canvas(bitmap)
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
      
      
      fun saveAsJson(context: Context, filename:String, data:Any,parent:File=context.filesDir,onFinished:(suspend ()->Unit)? = null) {
         val _filename = filename.replace(".json","")
         
         CoroutineScope(Dispatchers.IO).launch {
            jsonMutex.withLock {
               if(!parent.exists()){
                  parent.mkdirs()
               }
               
               val json=Gson().toJson(data);
               val Filename= "$_filename.json"
               
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
         
         return loadFromJson(file)
      }
      
      suspend inline fun <reified T> loadFromJson(file: File?): T? {
         
         if(file!=null) {
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
         else {
            return null
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
   
   /**
    * Will fill the max size of the calculation given.
    * */
   fun View.fillMaxSize(
      parentWidth: () -> Int,
      parentHeight: () -> Int,
   ) {
      viewTreeObserver.addOnGlobalLayoutListener(
         object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
               viewTreeObserver.removeOnGlobalLayoutListener(this)
               
               this@fillMaxSize.minimumHeight = parentHeight.invoke()
               this@fillMaxSize.minimumWidth = parentWidth.invoke()
               
               requestLayout()
            }
         }
      )
   }
   /**
    * Will fill the max size of the calculation given.
    * */
   fun TextView.fillMaxSize(
      parentWidth: () -> Int,
      parentHeight: () -> Int,
   ) {
      viewTreeObserver.addOnGlobalLayoutListener(
         object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
               viewTreeObserver.removeOnGlobalLayoutListener(this)
               
               this@fillMaxSize.minimumHeight = parentHeight.invoke()
               this@fillMaxSize.minimumWidth = parentWidth.invoke()
               
               requestLayout()
            }
         }
      )
   }
   /**
    * Will TRY to fill the max size of the TextView's parent.
    *
    * Catches errors it causes itself.
    * */
   fun TextView.fillMaxSize() {
      viewTreeObserver.addOnGlobalLayoutListener(
         object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
               viewTreeObserver.removeOnGlobalLayoutListener(this)
               
               try {
                  this@fillMaxSize.minimumHeight = (parent as View).height
                  this@fillMaxSize.minimumWidth = (parent as View).width
                  requestLayout()
               }catch (ex: Exception){}
               
               
               
            }
         }
      )
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

/**
 * Maps a list after the order given by another list.
 *
 * @sample mapByOrderOfExample
 *
 * */
inline fun <T,K>List<T>.mapByOrderOf(orderList:List<K>, selector:(T)->K):List<T>{
   
   // Create a lookup map first
   val itemsMap = this.associateBy {
      selector(it)
   }
   
   // Then map in order
   val sortedItems = orderList.mapNotNull { id -> itemsMap[id] }
   return sortedItems
}
private fun mapByOrderOfExample() {
   class Item(val id: Int, val name: String)
   
   val myOrder = listOf(1, 5, 3, 2, 4)
   
   val myItems = listOf(
      Item(id = 1, name = "One"),
      Item(id = 2, name = "Two"),
      Item(id = 3, name = "Three"),
      Item(id = 4, name = "Four"),
      Item(id = 5, name = "Five")
   )
   
   val orderedItemsList = myItems.mapByOrderOf(
      orderList = myOrder,
      selector = { it.id }
   )
}

/**
 * Takes out the excepted items.
 *
 * @return the original list minus the list of exceptions.
 * */
fun <T>List<T>.except(vararg items:T): List<T>{
   return this.filter { it !in items }
}
/**
 * Takes out the excepted items.
 *
 * @return the original list minus the list of exceptions.
 * */
fun <T>List<T>.except( items:List<T>): List<T>{
   return this.filter { it !in items }
}

/**
 * Picks a random item in the list, except for a speciffic item
 * (usually used for picking a random item except the current one)
 *
 * @return null if the excepted item is the only one in the list or if the list is empty, a new item pick otherwise
 * */
fun <T> List<T>.getRandom(except:T? = null): T?{
   
   if(this.isEmpty()) return null
   
   if(this.size ==1 && indexOf(except) != -1) return null
   
   val exception = indexOf(except)
   
   var result = Random.nextInt(0,this.size)
   
   while (result == exception){
      result = Random.nextInt(0,this.size)
   }
   
   
   return this[result]
}

/**
 * Wrapper for Modifier.clickable() that doesn't add a ripple.
 * */
@Composable
fun Modifier.clickableNoRipple(enabled:Boolean = true, role: Role? = null, onClick:()-> Unit)
   = this.clickable(enabled = enabled,role = role,interactionSource = remember { MutableInteractionSource() }, indication = null,onClick = onClick)

/**
 * @return [T] at the index specified. Index is wrapped around if over the list's [size] or if it is negative
 * */
inline fun <T> List<T>.cyclicalGet(index:Int):T{
   
   val i = Math.floorMod(index,size)
   return this.get(i)
}


fun MutableState<Boolean>.invert(){
   this.value = !this.value
}


/**
 * Maps a number from a known range to another known range
 *
 * @return a [Float] result of placing the number on the same fill factor on the desired range as it was on the original range
 * */
fun Float.map(in_min:Float, in_max:Float, out_min:Float, out_max:Float):Float {
   return (this - in_min) * (out_max - out_min) / (in_max - in_min) + out_min
}

/**
 * Maps a number from a known range to another known range
 *
 * @return a [Float] result of placing the number on the same fill factor on the desired range as it was on the original range
 * */
fun Float.map(in_range: Range<Float>,out_range:Range<Float>) :Float {
   return (this - in_range.lower) * (out_range.upper - out_range.lower) / (in_range.upper - in_range.lower) + out_range.lower
}

/**
 * Converts an [Int] to [Dp]
 * */
fun Int.toDp(density: Density): Dp{
   return with(density) { this@toDp.toDp() }
}

/**
 * Converts a [Float] to [Dp]
 * */
fun Float.toDp(density: Density): Dp{
   return with(density) { this@toDp.toDp() }
}

/**
 * Converts a [Dp] to [Float]
 * */
fun Dp.toPx(density: Density):Float = with(density){
   this@toPx.toPx()
}


/**
 * Copies a list N times over.
 *
 * Behaves like "StringInstance".repeat(N)
 *
 * @return if count<=0 it returns an empty list
 * @return [this] list repeated [count] times.
 * */
inline fun <T>List<T>.repeat(count:Int):List<T>{
   
   if(count<=0){
      return emptyList()
   }
   
   return List<T>(this.size*count){index->
      this[index%this.size]
   }
}

/**
 * Asks the system if it is in dark mode currently.
 *
 * Note that the value returned is not stateful.
 *
 * @return [Boolean] true if the system is in dark mode, false otherwise.
 * */
fun Context.isDarkMode() = when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
   Configuration.UI_MODE_NIGHT_YES -> true
   else -> false
}
fun Context.buildSystemBarsStyle(lightMode:Int = MyColor.SemiTransparentLight.toArgb(),darkMode:Int = MyColor.SemiTransparentDark.toArgb()): SystemBarStyle{
   
   return if (isDarkMode()) {
      SystemBarStyle.dark(darkMode) // Dark mode = red
   } else {
      SystemBarStyle.light(lightMode, MyColor.Black.toArgb()) // Light mode
   }
}


fun Constraints.of(placeable: Placeable): Constraints{
   return this.copy(
      minWidth = placeable.width,
      maxWidth = placeable.width,
      minHeight = placeable.height,
      maxHeight = placeable.height
   )
}



/**
 * Append a callback to a callback
 * */
fun <T>((T)->Unit).append(block:(T)->Unit):(T)-> Unit{
   
   return {
      this.invoke(it)
      block.invoke(it)
   }
}

/**
 * Append a callback to a callback
 * */
fun (()->Unit).append(block:()->Unit):()-> Unit{
   
   return {
      this.invoke()
      block.invoke()
   }
}


fun Offset.roundToIntOffset(): IntOffset {
   return IntOffset(this.x.roundToInt(),this.y.roundToInt())
}



@Composable
fun rememberDefaultOverscrollEffect(): OverscrollEffect{
   val context = LocalContext.current
   val density = LocalDensity.current
   return remember {
      AndroidEdgeEffectOverscrollEffect(
         context, density,
         EdgeEffectDefaults.DefaultGlowColor,
         EdgeEffectDefaults.DefaultGlowPaddingValues
      )
   }
}




