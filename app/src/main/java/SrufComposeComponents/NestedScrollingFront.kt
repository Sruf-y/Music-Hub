package SrufComposeComponents

import Navigation.ScreenState.PersistentState
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.tooling.ComposeStackTraceMode
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.media3.ui.compose.state.rememberPresentationState
import coil.request.Disposable
import com.aacomposetest.Classes.CursorValue
import com.aacomposetest.Classes.MyPrefs.open
import com.aacomposetest.Log
import com.aacomposetest.tryTo
import com.aacomposetest.tryValue
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlin.math.roundToInt

@Composable
fun NestedScrollingFront(
    appbar: @Composable () -> Unit,
    tabRow: @Composable () -> Unit,
    pager: @Composable () -> Unit,
    cursorValue: CursorValue<Float>? = null,
) {
    
    val contextRef = LocalContext.current
    
    var appbarHeight by remember { mutableFloatStateOf(0f) }
    var tabrowHeight by remember { mutableFloatStateOf(0f) }
    
    // Dictates the on-screen offset of everything.
    var appbarOffset by remember {
        mutableFloatStateOf(
                if (cursorValue != null) {
                    tryValue({
                        0f
                    }
                    ) {
                        cursorValue.get(context = contextRef, errorValue = 0f)
                    }
                } else {
                    0f
                }
        )
    }
    
    // Handles saving the cursorValue
    DisposableEffect(cursorValue) {
        
        onDispose {
            cursorValue?.run {
                
                val prefs = dir.open(contextRef)
                
                put(prefs,appbarOffset)
            }
        }
    }
    
    
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                val delta = available.y

                val newAppBarOffset = appbarOffset + delta
                val previousAppBarOffset = appbarOffset

                appbarOffset = newAppBarOffset.coerceIn(-appbarHeight, 0f)

                val consumedDelta = appbarOffset - previousAppBarOffset


                return Offset(0f, consumedDelta)
            }

        }
    }




    Box(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(nestedScrollConnection)
    ) {
        val appbarScrollState = rememberScrollState()

        // App bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .offset {
                    IntOffset(0, appbarOffset.roundToInt())
                }
                .onGloballyPositioned {
                    appbarHeight = it.size.height.toFloat()
                }
                .verticalScroll(state = appbarScrollState)
        ) {
            appbar.invoke()
        }

        // Tab row
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .offset {
                    IntOffset(0, (appbarHeight + appbarOffset).roundToInt())
                }
                .onGloballyPositioned {
                    tabrowHeight = it.size.height.toFloat()
                }
                .verticalScroll(state = appbarScrollState)
        ){
            tabRow.invoke()
        }



        val localDensity = LocalDensity.current
        // HorizontalPager
        Box(modifier = Modifier
            .fillMaxSize()
            .offset {
                IntOffset(
                    0,
                    (appbarHeight + appbarOffset + tabrowHeight).roundToInt()
                )
            }
            .padding(bottom = with(localDensity) { tabrowHeight.toDp() })
        ){
            pager.invoke()
        }

    }
}
