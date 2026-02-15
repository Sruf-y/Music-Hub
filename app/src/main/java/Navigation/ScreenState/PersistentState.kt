package Navigation.ScreenState

import androidx.compose.runtime.State
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

object GlobalScreenState {
   private val states = mutableMapOf<String, Any>()
   
   @Suppress("UNCHECKED_CAST")
   fun <T:Any> getState(key: String, default: T): T {
      return (states[key] as? T) ?: default
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
 * Only to be used for values where multiple instances will never happen OR don't matter. For example the initial values for states like initialPage or initialOffset.
 *
 * Example:
 *
 *    private class InitialState(){
 *
 *       var initialPage by PersistentState("fullscreenViewmodel", { 0 })
 *
 *       val pages = listOf("Song","Lyrics")
 *    }
 *
 * */
class PersistentState<T : Any>(
   private val key: String,
   private val default:()-> T
): ReadWriteProperty<Any?,T>, State<T> {
   override var value: T
      get() = GlobalScreenState.getState(key, default.invoke())
      set(value) = GlobalScreenState.setState(key, value)
   
   override fun getValue(thisRef: Any?, property: KProperty<*>): T {
      return this.value
   }
   
   override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
      this.value = value
   }
}