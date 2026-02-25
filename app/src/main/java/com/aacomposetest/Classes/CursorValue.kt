package com.aacomposetest.Classes

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.aacomposetest.Classes.MyPrefs.open
import kotlin.reflect.KClass

data class CursorValue<T : Any>(
   /**
    * Name of the column to pull/put data.
    * */
   var name: String,
   /**
    * The class to try casting the data to/from
    * */
   var dataType: KClass<T>,
   /**
    * SharedPrefferences directory to save this to
    * */
   var dir: MyPrefs.PrefDirs
){
   
   fun get(context:Context,errorValue:T):T{
      return dir.open(context).getCursorValue(this,errorValue)
   }
   
   fun put(context: Context, value:T){
      dir.open(context).edit(commit = true) {
         putCursorValue(this@CursorValue,value)
      }
   }
   
   /**
    * !!! Doesn't re-open the sharedPrefferences file BUT also doesn't check for the file to be correct.
    * */
   fun get(sharedPreferences: SharedPreferences,errorValue:T):T{
      return sharedPreferences.getCursorValue(this,errorValue)
   }
   
   /**
    * !!! Doesn't re-open the sharedPrefferences file BUT also doesn't check for the file to be correct.
    * */
   fun put(sharedPreferences: SharedPreferences, value:T){
      
      sharedPreferences.edit(commit = true) {
         putCursorValue(this@CursorValue,value)
      }
   }
   
}


@Throws(IllegalArgumentException::class)
fun <T: Any> SharedPreferences.getCursorValue(cursorData: CursorValue<T>,errorValue:T):T{
   
   return when(cursorData.dataType){
      Int::class -> {
         getInt(cursorData.name, errorValue as Int)
      }
      Long::class -> {
         getLong(cursorData.name,errorValue as Long)
      }
      
      String::class -> {
         getString(cursorData.name, errorValue as String)
      }
      Float::class->{
         getFloat(cursorData.name, errorValue as Float)
      }
      Boolean::class->{
         getBoolean(cursorData.name, errorValue as Boolean)
      }
      else -> {
         throw IllegalArgumentException("Type not recognized or missmatched.")
      }
   } as T
}

@Throws(IllegalArgumentException::class)
fun <T : Any> SharedPreferences.Editor.putCursorValue(cursorData: CursorValue<T>,value:T){
   
   when(cursorData.dataType){
      Int::class -> {
         putInt(cursorData.name,value as Int)
      }
      Long::class -> {
         putLong(cursorData.name,value as Long)
      }
      String::class -> {
         putString(cursorData.name,value as String)
      }
      Float::class->{
         putFloat(cursorData.name,value as Float)
      }
      Boolean::class->{
         putBoolean(cursorData.name,value as Boolean)
      }
      else -> {
         throw IllegalArgumentException("Type not recognized or missmatched.")
      }
   }
}