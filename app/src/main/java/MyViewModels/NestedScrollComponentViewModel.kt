package MyViewModels

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.lifecycle.ViewModel

class NestedScrollComponentViewModel: ViewModel() {
    var velocityY: MutableState<Float> = mutableFloatStateOf(0f)
    var lastPosition: MutableState<Float> = mutableFloatStateOf(0f)
    var lastTime: MutableState<Long> = mutableLongStateOf(0L)
}