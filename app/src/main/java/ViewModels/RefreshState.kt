package ViewModels

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

class RefreshState(rawState: MutableState<Boolean> = mutableStateOf(false),
                   onRefresh:()-> Unit = {}
) {
    var onRefresh = onRefresh
        private set
    /**
     * When refresh has been triggered by the user this will fire. It is reccomanded to set rawState's value to false afterwards.
     * */
    fun setOnRefreshListener(action:()-> Unit){
        onRefresh = action
    }
    var state by rawState
}

@Composable
fun rememberRefreshState(state:Boolean,onRefresh: () -> Unit = {}): RefreshState {
    return remember { RefreshState(mutableStateOf(state),onRefresh) }
}