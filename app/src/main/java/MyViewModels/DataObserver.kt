package MyViewModels

import ExoPlayer.myExoPlayer
import android.os.Handler
import android.os.Looper
import androidx.annotation.OptIn
import androidx.lifecycle.ViewModel
import androidx.media3.common.util.UnstableApi
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDateTime

class DataObserverViewModel<T>(
    val observingFunction:()->T,
    val initialValue:T,
    var delayMillis: Long = 250,
    var onValueChange:(T)-> Unit = {}
): ViewModel() {


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

        onValueChange.invoke(_progress.value)
    }

    fun forceValue(value:T){
        _progress.value = value
    }

    fun startUpdates() {
        handler.post(updateRunnable)
    }

    fun stopUpdates() {
        handler.removeCallbacks(updateRunnable)
    }

    override fun onCleared() {
        stopUpdates()
        super.onCleared()
    }
}