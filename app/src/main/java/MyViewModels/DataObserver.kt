package MyViewModels

import androidx.annotation.OptIn
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import com.aacomposetest.Log
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


@kotlin.OptIn(FlowPreview::class)
class DataObserverViewModel<T>(
   val observingFunction:()->T,
   val initialValue:T,
   var delayMillis: Long = 250,
   var onValueChangeDelayMillis:Long = 500,
   var onValueChange:(T)-> Unit = {
   
   }
): ViewModel()
{
   
   private var valueChangeJob:Job? = null
   
   private val _progress = MutableStateFlow<T>(initialValue)
   val progress: StateFlow<T> = _progress.asStateFlow()
   
   var updaterJob:Job? = null
   private fun startUpdater(){
      if(updaterJob == null){
         updaterJob = viewModelScope.launch {
            updateProgress()
            delay(delayMillis)
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
   private fun updateProgress() {
      _progress.value = observingFunction.invoke()
   }
   
   fun forceValue(value:T){
      _progress.tryEmit(value)
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
         var lastValue = progress.value
         // Run in a loop with a DISTINCT check then debounce
         while (true){
            if(lastValue!=progress.value) {
               onValueChange.invoke(progress.value)
               lastValue = progress.value
            }
            delay(onValueChangeDelayMillis)
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

