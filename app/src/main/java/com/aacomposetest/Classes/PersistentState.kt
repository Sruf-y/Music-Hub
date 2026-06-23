package com.aacomposetest.Classes

import androidx.compose.runtime.State
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

object GlobalPersistentStateManager {
   private val states = mutableMapOf<String, Any>()
   
   @Suppress("UNCHECKED_CAST")
   fun <T:Any> getState(key: String, default: T): T {
      return states.getOrPut(key) { default } as T
   }
   
   fun <T:Any> setState(key: String, value: T) {
      states[key] = value
   }
   
   fun clearState(key: String) {
      states.remove(key)
   }
   
   fun clearStates(vararg key:String){
      key.forEach {
         states.remove(it)
      }
   }
   
   fun clearAll() {
      states.clear()
   }
}


/**
 *
 * A type of object persistence done through a map, allowing items to persist only as long as the proccess is alive.
 * Items are deleted at proccess death.
 *
 *
 * Only to be used for values where multiple instances will never happen OR don't matter. For example the initial values for states like initialPage or initialOffset.
 *
 * Example:
 *
 *    private class InitialState(){
 *
 *       var initialPage by PersistentState("initialPageKey", { 0 })
 *
 *       val pages = listOf("Song","Lyrics")
 *    }
 *
 * Then
 *
 *    @Composable
 *    fun Example(){
 *       val a by remember{ InitialState() }
 *    }
 * */
class PersistentState<T : Any>(
   private val key: String,
   private val defaultFactory:()-> T
): ReadWriteProperty<Any?,T>, State<T> {
   override var value: T
      get() = GlobalPersistentStateManager.getState(key, defaultFactory.invoke())
      set(value) = GlobalPersistentStateManager.setState(key, value)
   
   override fun getValue(thisRef: Any?, property: KProperty<*>): T {
      return this.value
   }
   
   override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
      this.value = value
   }
   
   object GlobalStates{
      // make the string harder to collide at least for myself
      @JvmField
      val playerBottomSheetIsExpandedByDefault ="isPlayerBottomSheetExpanded#_ID_01"
      @JvmField
      val fullScreenState = "fullScreenState#_ID_01"
      @JvmField
      val mainPagerSavedPage = "mainPagerInitialPage#_ID_01"
      @JvmField
      val lastAccessedPlaylist = "lastAccessedPlaylistName#_ID_01"
      @JvmField
      val lastAccessedPlaylistIndex = "page_view_playlist_index#_ID_01"
      @JvmField
      val lastAccessedPlaylistOffset = "page_view_playlist_offset#_ID_01"
   }
}