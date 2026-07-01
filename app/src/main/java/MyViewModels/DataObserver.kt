package MyViewModels

import androidx.annotation.OptIn
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * **Note:**
 *  [isStateful] does not work with current implementation and will always be coutned as true. So to avoid unessesary
 *  checks, it is better to leave it as false as that allows only the State check to happen and this class' check to be ignored.
 *
 * @param delayMillis delay between reads
 * @param onValueChangeDelayMillis delay between writtings to the value variable
 * @param observingFunction function that is ran at an interval of [delayMillis] and written at
 * an interval of [onValueChangeDelayMillis] and writes the output of the function to the value variable
 * @param initialValue the first value emitted. This is required since this class uses stateFlow as its base
 * @param isStateful if the [observingFunction] should write to the stateFlow even if the last value was the same as the current value.
 *
 *
 * */
@kotlin.OptIn(FlowPreview::class)
class DataObserver<T>(
   val observingFunction:()->T,
   val initialValue:T,
   var delayMillis: ()->Long = {250},
   var onValueChangeDelayMillis:()->Long = {500},
   var isStateful: Boolean = false,
   var onValueChange:(T)-> Unit = {
   
   }
): ViewModel()
{
   private var valueChangeJob:Job? = null
   
   private val _value = MutableStateFlow<T>(initialValue)
   val value: StateFlow<T> = _value.asStateFlow()
   
   private fun handleBeingStateful(newValue:T,onPerformChange:()-> Unit){
      if(isStateful){
         if(_value.value != newValue){
            onPerformChange.invoke()
         }
      }else{
         onPerformChange.invoke()
      }
   }
   
   var updaterJob:Job? = null
   private fun startUpdater(){
      if(updaterJob == null){
         updaterJob = viewModelScope.launch {
            while(true){
               updateValue()
               delay(delayMillis.invoke())
            }
            
         }
      }
   }
   private fun cancelUpdater() {
      try {
         updaterJob?.cancel()
      } catch (e: Exception) { }
      updaterJob = null
   }
   
   
   @OptIn(UnstableApi::class)
   private fun updateValue() {
      
      // Doesn't update if the same value is tried to be emitted
      handleBeingStateful(observingFunction.invoke()){
         _value.value = observingFunction.invoke()
      }
      
   }
   
   fun forceValue(value:T){
      _value.tryEmit(value)
   }
   
   fun startUpdates() {
      startUpdater()
   }
   
   fun stopUpdates() {
      cancelUpdater()
   }
   
   init {
      // Job that reacts to the core udpater's value updates, thus should not be interrupted and only canceled at onCleared()
      valueChangeJob = viewModelScope.launch {
         // Use debounce
         var lastValue = value.value
         // Run in a loop with a DISTINCT check then debounce
         while (true){
            if(lastValue!=value.value) {
               onValueChange.invoke(value.value)
               lastValue = value.value
            }
            delay(onValueChangeDelayMillis.invoke())
         }
         
      }
      
   }
   
   override fun onCleared() {
      try {
         valueChangeJob?.cancel()
      }catch (ex: Exception){}
      valueChangeJob = null
      
      cancelUpdater()
      
      super.onCleared()
   }
}

