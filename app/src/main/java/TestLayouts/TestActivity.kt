package TestLayouts


import AppDatabase.SongRelated.Song
import SrufComposeComponents.Text
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.spring
import androidx.compose.animation.splineBasedDecay
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.draggable2D
import androidx.compose.foundation.gestures.rememberDraggable2DState
import androidx.compose.foundation.gestures.rememberScrollable2DState
import androidx.compose.foundation.gestures.rememberScrollableState
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
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.aacomposetest.ui.theme.ComposeTestTheme
import com.aacomposetest.ui.theme.MyColor
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt


class TestActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()


        val songsList =
            List(100) { index -> Song.emptySong(id = index.toLong(), title = "Song ${index}") }
        setContent {
            ComposeTestTheme {

                val scope = rememberCoroutineScope()

                if (false)
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .safeContentPadding()
                    ) {
                        val lazyListState: LazyListState? = null

                        var flingJob by remember { mutableStateOf<Job?>(null) }

                        val scope = rememberCoroutineScope()

                        var appbarRect by remember { mutableStateOf(Rect(0f, 0f, 0f, 0f)) }
                        var tabrowRect by remember { mutableStateOf(Rect(0f, 0f, 0f, 0f)) }

                        // Calculate offset for 1st item
                        var appbarOffset by remember {
                            mutableFloatStateOf(0f)
                        }

                        // Auto calculate offset for 2nd item
                        val tabrowOffset by remember {
                            derivedStateOf {

                                appbarOffset + appbarRect.height
                            }

                        }

                        // Auto calculate offset for 3rd item
                        val totalHeaderHeight by remember {
                            derivedStateOf {
                                (tabrowOffset + tabrowRect.height).coerceAtLeast(0f)
                            }
                        }

                        // Nested connection, nu am reusit sa inteleg calculele pentru "fling" inca: 24/10/2025. Deci am lasat doar cu drag
                        val nestedScrollConnection = remember {
                            object : NestedScrollConnection {
                                override fun onPreScroll(
                                    available: Offset,
                                    source: NestedScrollSource
                                ): Offset {

                                    flingJob?.cancel()

                                    val previousOffset = totalHeaderHeight
                                    appbarOffset = (appbarOffset + available.y).coerceIn(
                                        -appbarRect.height,
                                        0f
                                    )

                                    return Offset(0f, totalHeaderHeight - previousOffset)
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
                            fun Modifier.AttachFling(): Modifier {
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


                            ) {
                                Text("Appbar", modifier = Modifier.align(Alignment.Center))
                            }

                            // Tab row - becomes sticky after app bar scrolls out
                            if (false)
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
                            if (false)
                                LazyColumn(
                                    state = lazyListState ?: rememberLazyListState(),
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .offset(y = with(LocalDensity.current) { totalHeaderHeight.toDp() })
                                        .background(MyColor.Blue),
                                    contentPadding = PaddingValues(bottom = 16.dp)
                                ) {

                                    items(items = songsList, key = { it.id }) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(80.dp)
                                                .background(MyColor.Pink40)
                                        ) {
                                            Text(
                                                text = it.title,
                                                Modifier.align(Alignment.CenterStart)
                                            )
                                        }
                                    }

                                }
                        }
                    }


                var pagerState = rememberPagerState(
                    initialPage = 0,
                    pageCount = { 3 }
                )

                var appbarHeight by remember { mutableStateOf(0f) }
                var appbarOffset by remember { mutableFloatStateOf(0f) }
                var tabrowHeight by remember { mutableFloatStateOf(0f) }

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

                            Log.i("WTF", "Delta $delta  |  appbaroffset $appbarOffset")

                            val consumedDelta = appbarOffset - previousAppBarOffset


                            return Offset(0f, consumedDelta)
                        }




                    }
                }

                Surface(modifier = Modifier
                    .fillMaxSize()
                    .safeDrawingPadding()
                ){
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .nestedScroll(nestedScrollConnection)
                    ) {
                        // App bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .offset {
                                    IntOffset(0, appbarOffset.roundToInt())
                                }
                                .height(200.dp)
                                .background(MyColor.Green)
                                .onGloballyPositioned {
                                    appbarHeight = it.size.height.toFloat()
                                }

                        ) {
                            Text("Appbar", modifier = Modifier.align(Alignment.Center))
                        }

                        // Tab row
                        TabRow(
                            selectedTabIndex = pagerState.currentPage,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                                .offset {
                                    IntOffset(0, (appbarHeight + appbarOffset).roundToInt())
                                }
                                .background(Color.Gray)
                                .onGloballyPositioned {
                                    tabrowHeight = it.size.height.toFloat()
                                }
                        ) {
                            listOf("Tab 1", "Tab 2", "Tab 3").forEachIndexed { index, title ->
                                Tab(
                                    selected = pagerState.currentPage == index,
                                    onClick = {
                                        scope.launch {
                                            pagerState.animateScrollToPage(index)
                                        }
                                    },
                                    text = { Text(title) }
                                )
                            }
                        }


                        val localDensity = LocalDensity.current
                        // HorizontalPager
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier
                                .fillMaxSize()
                                .offset {
                                    IntOffset(
                                        0,
                                        (appbarHeight + appbarOffset + tabrowHeight).roundToInt()
                                    )
                                }
                                .padding(bottom = with(localDensity) { tabrowHeight.toDp() })
                        ) { page ->
                            when (page) {
                                0 -> MyPagerPage(songsList)
                                1 -> MySecondPage(songsList)
                                2 -> MyThirdPage()
                            }
                        }
                    }
                }
            }
        }
    }
}



@Composable
fun MyPagerPage(a:List<Song>){
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MyColor.SelectedGold),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {

        items(items = a, key = { it.id }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)

            ) {
                Text(text = it.title, Modifier.align(Alignment.CenterStart).background(MyColor.Pink40))
            }
        }

    }
}

@Composable
fun MySecondPage(a:List<Song>){
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MyColor.Blue),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {

        items(items = a, key = { it.id }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .background(MyColor.Pink40)
            ) {
                Text(text = it.title, Modifier.align(Alignment.CenterStart), color = Color.Red)
            }
        }

    }
}

@Composable
fun MyThirdPage(){
    val scrollState = rememberScrollState()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Yellow)
            .verticalScroll(scrollState) // Add scroll capability
    ) {
        // Your content
        Text("Third Page", modifier = Modifier.align(Alignment.Center))
    }
}


@Composable
fun StickyHeaderPager(
    appBar: @Composable () -> Unit,
    tabRow: @Composable () -> Unit,
    pager: @Composable (parentRect:Rect) -> Unit,
    modifier: Modifier = Modifier
) {

    var parentRect by remember { mutableStateOf<Rect>(Rect(0f,0f,0f,0f)) }

    val appBarHeight = remember { mutableStateOf(0) }
    val tabRowHeight = remember { mutableStateOf(0) }
    var scrollOffset by remember { mutableStateOf(0f) }
    val totalHeaderHeight by remember {
        derivedStateOf {
            appBarHeight.value + tabRowHeight.value
        }
    }

    val nestedScrollConnection = remember(totalHeaderHeight) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y

                // finger goes down, content goes down - we want to SHOW headers
                if (delta > 0) {
                    if (scrollOffset < 0) {
                        val consume = minOf(delta, -scrollOffset)
                        scrollOffset += consume
                        return Offset(0f, consume)
                    }
                }
                // finger goes up, content goes up - we want to HIDE headers
                else if (delta < 0) {
                    if (scrollOffset > -totalHeaderHeight) {
                        val consume = maxOf(delta, -(totalHeaderHeight + scrollOffset))
                        scrollOffset += consume
                        return Offset(0f, consume)
                    }
                }
                return Offset.Zero
            }
        }
    }

    val localDensity = LocalDensity.current


    Box(
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned {
                parentRect = it.boundsInParent()
            }
            .nestedScroll(nestedScrollConnection)
    ) {
        // Headers that scroll away
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = with(localDensity) { (scrollOffset).toDp() })
        ) {
            // AppBar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .onSizeChanged { appBarHeight.value = it.height }
            ) {
                appBar()
            }

            // TabRow (will become sticky)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .onSizeChanged { tabRowHeight.value = it.height }
            ) {
                tabRow()
            }
        }




        // Pager content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = with(localDensity) { (totalHeaderHeight + scrollOffset).toDp() })
        ) {
            pager(parentRect)
        }
    }
}





@Composable
fun ExampleUsage(pagerState: PagerState,items:List<Song>) {

    val localDensity = LocalDensity.current
    StickyHeaderPager(
        appBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color.Blue)
            ) {
                Text(
                    "App Bar",
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.White,
                    fontSize = 24.sp
                )
            }
        },
        tabRow = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .background(Color.Gray)
            ) {
                TabRow(selectedTabIndex = pagerState.currentPage, modifier = Modifier.fillMaxSize()) {
                    listOf("Tab 1", "Tab 2", "Tab 3").forEachIndexed { index, title ->
                        Tab(
                            selected = pagerState.currentPage == index,
                            onClick = { /* Handle tab click */ },
                            text = { Text(title) }
                        )
                    }
                }
            }
        },
        pager = {parentRect->
            HorizontalPager(
                state = pagerState,
                modifier =
                    Modifier
                        .height(with(localDensity) { parentRect.height.toDp() })
                        .width(with(localDensity) { parentRect.width.toDp() })
            ) { page ->
                when(page){
                    0->{
                        MyPagerPage(items)
                    }
                    1->{
                        MySecondPage(items)
                    }
                    2->{
                        MyThirdPage()
                    }
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}




