package MyViewModels

import androidx.compose.foundation.lazy.LazyListState
import androidx.lifecycle.ViewModel

class LazyListViewmodel(): ViewModel(){
    val listState = LazyListState()
}