package com.aacomposetest.Classes

import Settings.SettingsRepository
import android.preference.PreferenceFragment
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.aacomposetest.Log
import com.aacomposetest.MyApplication
import com.aacomposetest.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


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
      .distinctUntilChanged()
   
   /**
    * Emits whenever [writeValue] is called but also when a disk write finishes.
    * */
   val stateFlow:StateFlow<T> = immediateFlow.stateFlow
   
   init {
      scope.launch(Dispatchers.Default) {
         
         val initial = writtenFlow.firstOrNull()
         if(initial!=null){
            immediateFlow.setValue(initial)
         }
         
         var firstWrite = true
         
         immediateFlow.stateFlow
            /**
             * Debounce writes to safe write cycles
             * */
            .debounce { writeDebounce }
            .collect { newImmediateValue ->
               
               if(!firstWrite){
                  if (newImmediateValue != initial) {
                     datastoreDir.edit {
                        it[key] = newImmediateValue
                     }
                  }
               } else {
                  firstWrite = false
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
      Boolean::class->{
         booleanPreferencesKey(value) as Preferences.Key<T>
      }
      else->{
         throw WrongTypeException("This function cannot return this type [${T::class.simpleName}]")
      }
   }
   
}