package MyViewModels

import android.content.Context
import android.os.FileObserver
import android.os.FileObserver.CLOSE_WRITE
import android.os.FileObserver.CREATE
import android.os.FileObserver.DELETE
import android.os.FileObserver.DELETE_SELF
import android.os.FileObserver.MODIFY
import android.os.FileObserver.MOVED_FROM
import android.os.FileObserver.MOVED_TO
import android.os.FileObserver.MOVE_SELF
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musichub.Logs
import kotlinx.coroutines.launch
import java.io.File

class FileWatcher(
   private val dir:File,
   onChangeListeners:List<FileWatchListener> = emptyList()
): ViewModel()
{
   private val onChangeListenerList: ArrayList<FileWatchListener> = ArrayList()
   
   init {
      
      if(instances.map { it.second }.contains(dir.path)){
         throw DuplicateWatchingException()
      }
      
      onChangeListeners.forEach {
         addOnChangeListener(it)
      }
   }
   
   fun addOnChangeListener(listener: FileWatchListener): Boolean {
      if(onChangeListenerList.map { it.key }.contains(listener.key)){
         Log.w(Logs.DATA_ERROR.name,"Attempted to add duplicate listeners with the same key!")
         return false
      }else{
         return onChangeListenerList.add(listener)
      }
   }
   
   fun removeOnChangeListener(key: Any): Boolean {
      return onChangeListenerList.removeIf { it.key == key }
   }
   
   
   private var observer: FileObserver? = null
   
   fun startWatching() {
      if (observer != null) return
      
      observer = FileObserverImpl(dir){event,fileName->
            /**
             * For playlist "Favorites", fileName will look like "Favorites.json"
             * */
            
            val eventType = when (event) {
               CREATE -> "CREATE"
               DELETE -> "DELETE"
               MODIFY -> "MODIFY"
               MOVED_FROM -> "MOVED_FROM"
               MOVED_TO -> "MOVED_TO"
               CLOSE_WRITE -> "CLOSE_WRITE"
               DELETE_SELF -> "DELETE_SELF"
               MOVE_SELF -> "MOVE_SELF"
               else -> "UNKNOWN($event)"
            }
         com.musichub.Log("Event type : [ ${eventType} ]",Logs.FILE_IO)
         viewModelScope.launch {
            onChangeListenerList.toList().forEach {
               //filter the event
               if(it.fileNameFilter(fileName) && it.eventFilter(event)){
                  it.onEvent.invoke(event,fileName)
               }
               
            }
         }
         
            
         }
      
      
      observer?.startWatching()
      
   }
   
   
   
   fun stopWatching() {
      observer?.stopWatching()
   }
   
   override fun onCleared() {
      super.onCleared()
      
      stopWatching()
      observer = null
   }
   
   companion object{
      private val instances:List<Pair<Int, String>> = emptyList()
      internal class DuplicateWatchingException(): Exception("This folder is already being watched! Two instances watching the same folder will butt heads!")
      
      /**
       * @param onEvent gives the FileObserver event ID and the file name as inputs for the lambda. The filename for "Favorites" will be "Favorites.json"
       * */
      data class FileWatchListener(
         val key: Any,
         val fileNameFilter: (filenameval :String?)->Boolean = {true},
         val eventFilter:(event:Int)->Boolean = {true},
         val onEvent:(event: Int, fileName: String?)-> Unit
      )
      
   }
}

private class  FileObserverImpl(
   dir:File,
   val onEvent: (Int,String?)->Unit = {_,_->}
):FileObserver(
   dir,
   CREATE or DELETE_SELF or DELETE or MODIFY or MOVED_TO or MOVED_FROM or CLOSE_WRITE or MOVE_SELF
) {
   override fun onEvent(event: Int, path: String?) = onEvent.invoke(event,path)
}