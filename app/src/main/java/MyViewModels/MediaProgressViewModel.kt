package MyViewModels

import ExoPlayer.myExoPlayer
import android.os.Handler
import android.os.Looper
import androidx.annotation.OptIn
import androidx.compose.runtime.asLongState
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.media3.common.util.UnstableApi
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


class MediaProgressViewModel() : ViewModel() {


    private var delayMillis:Long = 250

    /**
     * Updating this value requires a stop and start of the updates, otherwise it will not take effect
     *
     * Before starting again it is recomanded to manually set the value with forceValue in order to avoid remnant value flickers
     * */
    fun setDelay(milliseconds:Long){
        delayMillis = milliseconds
    }

    var onValueChange:(newValue: Long)->Unit = {}

    private val _progress = MutableStateFlow<Long>(0)
    val progress: StateFlow<Long> = _progress.asStateFlow()

    private val handler = Handler(Looper.getMainLooper())
    private val updateRunnable = object : Runnable {
        override fun run() {
            updateProgress()
            handler.postDelayed(this, delayMillis) // Update every 250ms
        }
    }

    @OptIn(UnstableApi::class)
    private fun updateProgress() {
        val currentPos = myExoPlayer.getCurrentPosition()
        val duration = myExoPlayer.currentlyPlayingSong.value?.duration ?: 0

        val progress = if (duration > 0) {
            currentPos
        } else {
            0
        }

        _progress.value = progress

        onValueChange.invoke(_progress.value)
    }

    fun forceValue(value:Long){
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