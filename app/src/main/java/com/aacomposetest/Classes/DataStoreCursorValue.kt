package com.aacomposetest.Classes

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Returns a [PersistentState] pointer to the requested cursor value
 * */
inline fun <reified T : Any> accessCursorValue(
   defaultValue: T,
   key: String,
   scope: CoroutineScope,
   datastoreDir: DataStore<Preferences>,
   noinline writeCondition:(T)-> Boolean = {true},
   writeDebounce:Long = 500L
): PersistentState<DataStoreCursorValue<T>> {
   return PersistentState(key){
      DataStoreCursorValue(
         defaultValue = defaultValue,
         key = genericKey<T>(key),
         writeCondition = writeCondition,
         datastoreDir = datastoreDir,
         writeDebounce = writeDebounce,
         scope = scope
      )
   }
}






class DataStoreCursorValue<T : Any>(
   defaultValue:T,
   val key: Preferences.Key<T>,
   val datastoreDir:DataStore<Preferences>,
   var writeCondition:(T)-> Boolean = {true},
   val writeDebounce:Long = 500L,
   private val scope: CoroutineScope
){
   private val immediateFlow = AutoStateFlow<T>(defaultValue)
   
   private val writtenFlow = datastoreDir.data
      .map { it[key]?:defaultValue }
   
   /**
    * Emits whenever [writeValue] is called but also when a disk write finishes.
    * */
   val stateFlow:StateFlow<T> = combine(
      immediateFlow.stateFlow,
      writtenFlow
   ) { immediate,written->
      immediate
   }.stateIn(
      scope = scope,
      started = SharingStarted.WhileSubscribed(5000L),
      initialValue = defaultValue
   )
   
   init {
      // FLOW - Collect immediate value and write it to disk
      scope.launch(Dispatchers.Default) {
         immediateFlow.stateFlow
            // Wait 500ms to before writing data to file in case a new value is about to be written
            // in quick succestion. In that case don't write and start waiting again but on behalf of the next value
            .debounce{ writeDebounce }
            .collect {newImmediateValue->
               datastoreDir.edit {
                  
                  //TODO: STATEFUL - Implement here if wanted/needed in the future.
                  // note to self: by stateful i meant "create an 'oldValue' and compare against it if another variable, "equalityCheck" is true. Current behaviour in that
                  // instance is when that variable would be false.
                  // I don't see a reason for that tho
                  
                  it[key] = newImmediateValue
               }
            }
      }
   }
   
   /**
    * Sets the value that is read by the stateFlow. The value will be written to disk after the debounced time [writeDebounce]
    * */
   fun setValue(value:T){
      if(writeCondition.invoke(value)){
         immediateFlow.setValue(value)
      }
      
   }
   
   /**
    * FORCES a near-instant write, bypassing safety measures.
    * */
   fun emergencyForceWrite(value:T){
      immediateFlow.setValue(value)
      
      scope.launch {
         datastoreDir.edit {
            it[key] = value
         }
      }
   }
   
}

/**
 * Signifies the wrong type being given to a [genericKey] function call.
 * */
class WrongTypeException(message:String): Exception(message)

/**
 * Wrapper for DataStore's functions for key generation.
 * */
inline fun <reified T : Any>genericKey(value: String):Preferences.Key<T>{
   
   return when(T::class){
      Int::class->{
         intPreferencesKey(value) as Preferences.Key<T>
      }
      Long::class->{
         longPreferencesKey(value) as Preferences.Key<T>
      }
      Float::class->{
         floatPreferencesKey(value) as Preferences.Key<T>
      }
      Double::class->{
         doublePreferencesKey(value) as Preferences.Key<T>
      }
      String::class->{
         stringPreferencesKey(value) as Preferences.Key<T>
      }
      else->{
         throw WrongTypeException("This function cannot return this type [${T::class.simpleName}]")
      }
   }
   
}
