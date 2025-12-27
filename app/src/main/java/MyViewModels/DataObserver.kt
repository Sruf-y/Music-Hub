package MyViewModels

import ExoPlayer.myExoPlayer
import android.os.Handler
import android.os.Looper
import androidx.annotation.OptIn
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import com.aacomposetest.Log
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import java.time.LocalDateTime


@kotlin.OptIn(FlowPreview::class)
class DataObserverViewModel<T>(
    val observingFunction:()->T,
    val initialValue:T,
    var delayMillis: Long = 250,
    var valueChangeDebounce:Long = 500,
    var onValueChange:(T)-> Unit = {

    }
): ViewModel() {

    var trackJob:Job? = null

    private val _progress = MutableStateFlow<T>(initialValue)
    val progress: StateFlow<T> = _progress.asStateFlow()

    private val handler = Handler(Looper.getMainLooper())
    private val updateRunnable = object : Runnable {
        override fun run() {
            updateProgress()
            handler.postDelayed(this, delayMillis) // Update every 250ms
        }
    }

    @OptIn(UnstableApi::class)
    private fun updateProgress() {

        _progress.value = observingFunction.invoke()


    }

    fun forceValue(value:T){
        _progress.tryEmit(value)
    }

    fun startUpdates() {

        handler.post(updateRunnable)
    }

    fun stopUpdates() {
        handler.removeCallbacks(updateRunnable)
    }

    init {
        trackJob = viewModelScope.launch {
            progress
                .debounce { valueChangeDebounce }
                .collect {
                    onValueChange.invoke(it)
                }
        }

    }

    override fun onCleared() {
        try {
            trackJob?.cancel()
        }catch (ex: Exception){}
        stopUpdates()
        super.onCleared()
    }
}