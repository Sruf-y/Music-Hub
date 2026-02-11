package MyViewModels

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

class RefreshState(rawState: MutableState<Boolean> = mutableStateOf(false), var onRefresh:()-> Unit = {}) {

    var state by rawState
}

@Composable
fun rememberRefreshState(state:Boolean,onRefresh: () -> Unit = {}): RefreshState {
    return remember { RefreshState(mutableStateOf(state),onRefresh) }
}