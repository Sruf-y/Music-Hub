package TestLayouts


import AppDatabase.SongRelated.Song
import SrufComposeComponents.Text
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.SplineBasedFloatDecayAnimationSpec
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.DecayAnimation
import androidx.compose.animation.core.DecayAnimationSpec
import androidx.compose.animation.core.FloatExponentialDecaySpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDecay
import androidx.compose.animation.core.spring
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.draggable2D
import androidx.compose.foundation.gestures.rememberDraggable2DState
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.gestures.stopScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.PointerInputEventHandler
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.ViewModel
import com.aacomposetest.ui.theme.ComposeTestTheme
import com.aacomposetest.ui.theme.MyColor
import com.composables.core.androidx.compose.foundation.gestures.draggable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ui.components.SongItem
import kotlin.compareTo
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.times


class TestActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()


        val a = List(100) { index -> Song.emptySong(id = index.toLong(),title = "Song ${index}") }
        setContent {
            ComposeTestTheme {




                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .safeContentPadding()
                ) {
                    val lazyListState:LazyListState? = null

                    var flingJob by remember{mutableStateOf<Job?>(null)}

                    val scope = rememberCoroutineScope()

                    var appbarRect by remember { mutableStateOf(Rect(0f,0f,0f,0f)) }
                    var tabrowRect by remember { mutableStateOf(Rect(0f,0f,0f,0f)) }

                    // Calculate offset for 1st item
                    var appbarOffset by remember {
                        mutableFloatStateOf(0f)
                    }

                    // Auto calculate offset for 2nd item
                    val tabrowOffset by remember {
                        derivedStateOf {

                            appbarOffset+appbarRect.height
                        }

                    }

                    // Auto calculate offset for 3rd item
                    val totalHeaderHeight by remember {
                        derivedStateOf {
                            (tabrowOffset+tabrowRect.height).coerceAtLeast(0f)
                        }
                    }

                    // Nested connection, nu am reusit sa inteleg calculele pentru "fling" inca: 24/10/2025. Deci am lasat doar cu drag
                    val nestedScrollConnection = remember {
                        object : NestedScrollConnection {
                            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {

                                flingJob?.cancel()

                                val previousOffset = totalHeaderHeight
                                appbarOffset = (appbarOffset+available.y).coerceIn(-appbarRect.height,0f)

                                return Offset(0f,totalHeaderHeight-previousOffset)
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .nestedScroll(nestedScrollConnection)
                    ) {


                        var velocityY by remember { mutableStateOf(0f) }
                        var lastPosition by remember { mutableStateOf(0f) }
                        var lastTime by remember { mutableStateOf(0L) }


                        val appbarDragState = rememberDraggable2DState {
                            appbarOffset = (appbarOffset + it.y).coerceIn(
                            -appbarRect.height,
                            0f
                            )


                            val currentTime = System.currentTimeMillis()
                            val timeDiff = (currentTime - lastTime).coerceAtLeast(1L)


                            // calculate velocity
                            velocityY = ((appbarOffset - lastPosition) / timeDiff) * 1000f


                            lastPosition = appbarOffset
                            lastTime = currentTime
                        }

                        /**
                         * Lets non-fling items (Items unlike column/lazycolumn, Box for example) fling.
                         * To be used with NestedScrollConnection.
                         * */
                        fun Modifier.AttachFling(): Modifier
                            {
                                return this.draggable2D(
                                    appbarDragState,
                                    onDragStarted = {
                                        flingJob?.cancel()
                                        scope.launch {
                                            lazyListState?.stopScroll()
                                        }
                                        lastPosition = appbarOffset
                                        lastTime = System.currentTimeMillis()
                                    },
                                    onDragStopped = {
                                        flingJob?.cancel()
                                        flingJob = scope.launch {
                                            val boundedPos = (appbarOffset).coerceIn(
                                                -appbarRect.height,
                                                0f
                                            )
                                            var currentY = boundedPos
                                            var velY = velocityY

                                            val friction = 0.995f

                                            // Animate until velocity is negligible
                                            while (abs(velY) > 1f) {
                                                // Update position based on velocity
                                                currentY = (currentY + velY * 0.016f).coerceIn(
                                                    -appbarRect.height,
                                                    0f
                                                )

                                                // Apply friction (decay)
                                                velY *= friction

//                                                // Handle passing fling off to the list
//                                                if(currentY!=boundedPos){
//
//                                                }

                                                // Update UI
                                                appbarOffset = (currentY).coerceIn(
                                                    -appbarRect.height,
                                                    0f
                                                )

                                                delay(16L) // ~60fps
                                            }
                                        }
                                    }
                                )
                            }


                        // App bar - scrolls out completely
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp) // Reduced height for better demonstration
                                .zIndex(1f)
                                .offset(y = with(LocalDensity.current) { appbarOffset.toDp() })
                                .background(MyColor.Green)

                                .onGloballyPositioned {
                                    appbarRect = it.boundsInParent()
                                }
                                .AttachFling()




                        ){
                            Text("Appbar", modifier = Modifier.align(Alignment.Center))
                        }

                        // Tab row - becomes sticky after app bar scrolls out
                        if(false)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .zIndex(2f) // Higher zIndex to stay above content
                                .offset(y = with(LocalDensity.current) { tabrowOffset.toDp() })
                                .background(MyColor.SeparatorGray)
                                .onGloballyPositioned {
                                    tabrowRect = it.boundsInParent()
                                }
                                .AttachFling()


                        ) {
                            // Your tab content here
                            Text("Tab Row", modifier = Modifier.align(Alignment.Center))
                        }

                        // LazyColumn with padding to account for headers
                        LazyColumn(state = lazyListState?:rememberLazyListState(),
                            modifier = Modifier
                                .fillMaxSize()
                                .offset(y = with(LocalDensity.current) { totalHeaderHeight.toDp() })
                                .background(MyColor.Blue),
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {

                            items(items = a, key = {it.id}) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(80.dp)
                                        .background(MyColor.Pink40)
                                ){
                                    Text(text = it.title, Modifier.align(Alignment.CenterStart))
                                }
                            }

                        }
                    }
                }




            }

        }
    }


}