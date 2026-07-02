package Presentation.Navigation

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import com.musichub.toDp
import com.musichub.ui.theme.MyColor
import com.musichub.ui.theme.Theme

@Composable
fun DrawerStandardLayout(
    modifier: Modifier = Modifier
        .fillMaxHeight()
        .padding(5.dp),
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    items: LazyListScope.() -> Unit
) {
    val lazyListState = rememberLazyListState()
    
    // Get safe area insets
    val safeInsets = WindowInsets.safeDrawing
    val layoutDirection = LocalLayoutDirection.current
    val density = LocalDensity.current
    
    // Calculate padding values for the LazyColumn
    val contentPadding = PaddingValues(
        top = safeInsets.getTop( density = density ).toDp(density),
        bottom = safeInsets.getBottom( density = density ).toDp(density),
        start = safeInsets.getLeft( density = density, layoutDirection = layoutDirection).toDp(density)
    )
    
    Box(
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures()
            }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxHeight(),
            state = lazyListState,
            contentPadding = contentPadding,
            horizontalAlignment = horizontalAlignment
        ) {
            items.invoke(this)
        }
    }
}