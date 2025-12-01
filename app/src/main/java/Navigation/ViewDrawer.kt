package Navigation

import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.remember

/**
 * Animation state enum for [ViewDrawerState]
 * */
enum class DrawerSheetState{
    Idle,
    Dragging,
    Settling
}

/**
 * Open/Closed state enum for [ViewDrawerState]
 * */
enum class ViewDrawerValue{
    Open,
    Closed
}

/**
 * State class for [ViewDrawer]. Can be used to programatically track the drawer's states and to open or close it.
 *
 * @param initialValue Initial value the drawer will take upon rendering.
 * */
class ViewDrawerState(initialValue: ViewDrawerValue){

    private val scope = CoroutineScope(Dispatchers.Main)

    private var animJob: Job? = null

    private var drawerView: MutableState<DrawerLayout?> = mutableStateOf(null)

    private fun launchAnimation(block:suspend ()-> Unit){
        animJob?.cancel()

        animJob = scope.launch {
            block.invoke()
        }
    }

    /**
     * Opens the drawer
     * */
    fun open(gravity: Int = GravityCompat.START,animate:Boolean = true) {
        drawerView.value?.openDrawer(gravity,animate)
    }

    /**
     * Closes the drawer
     * */
    fun close(gravity: Int = GravityCompat.START,animate:Boolean = true) {
        drawerView.value?.closeDrawer(gravity,animate)
    }

    /**
     * Curent open/closed value of the drawer.
     * */
    var currentValue by mutableStateOf(initialValue)
        private set

    /**
     * Drawer's current offset. Is Float.NAN before being initialized OR if the drawer's content is empty.
     * Otherwise it ranges between 0F at CLOSED and 1F at OPEN
     * */
    var currentOffset by mutableFloatStateOf(0f)
        private set
    /**
     * States can only ever be:
     * [DrawerLayout.STATE_IDLE], [DrawerLayout.STATE_DRAGGING] or [DrawerLayout.STATE_SETTLING]
     * */
    var currentState by mutableStateOf(DrawerSheetState.Idle)
        private set

    /**
     * Sets the private refference for the drawer in order for the state to function at all.
     * */
    fun setDrawer(view: DrawerLayout?){
        drawerView.value = view

        setupDrawerListener()
    }

    private val drawerListener = object : DrawerLayout.DrawerListener {
        override fun onDrawerSlide(drawerView: View, slideOffset: Float) {

            currentOffset = slideOffset
        }

        override fun onDrawerOpened(drawerView: View) {
            currentValue = ViewDrawerValue.Open
        }

        override fun onDrawerClosed(drawerView: View) {
            currentValue = ViewDrawerValue.Closed
        }

        override fun onDrawerStateChanged(newState: Int) {
            // Called when the drawer state changes
            // newState can be: STATE_IDLE, STATE_DRAGGING, STATE_SETTLING
            currentState = DrawerSheetState.entries[newState]

        }
    }

    /**
     * Sets up the listener for the drawer.
     * */
    private fun setupDrawerListener() {
        drawerView.value?.addDrawerListener(drawerListener)
    }

    /**
     * Sets the drawer to open or closed as the last state dictates.
     * */
    fun initDrawer(){
        when(currentValue){
            ViewDrawerValue.Open -> {
                currentOffset = 1f
                open()
            }
            ViewDrawerValue.Closed -> {
                currentOffset = 0f
                close()
            }
        }
    }

    companion object{
        fun Saver() =
            androidx.compose.runtime.saveable.Saver<ViewDrawerState, ViewDrawerValue>(
                save = {
                    it.currentValue
                },
                restore = {
                    ViewDrawerState(it)
                }
            )
    }
}

/**
 * Remember function to obtain a saveable instance of [ViewDrawerState]
 *
 * @return [ViewDrawerState]
 * */
@Composable
fun rememberViewDrawerState(initialValue: ViewDrawerValue): ViewDrawerState{
    return rememberSaveable(saver = ViewDrawerState.Saver()) {
        ViewDrawerState(initialValue)
    }
}


/**
 * Drawer composable designed to work as the default Android framework dictates.
 *
 * ISSUE:
 *
 * [Column] does not work inside the drawer content. But [LazyColumn] does work. The same rule applies for Rows and Grids.
 *
 * @sample Basic_ViewDrawer_Sample
 * @param state The state used to observe the drawer
 * @param mainContent Main content of the layout
 * @param drawerContent Content of the drawer layout
 * @param scrimColor Color of the Scrim drawn between [drawerContent] and [mainContent]
 *
 * */
@Composable
fun ViewDrawer(
    modifier: Modifier = Modifier.fillMaxSize(),
    state: ViewDrawerState = ViewDrawerState(ViewDrawerValue.Closed),
    mainContent: @Composable () -> Unit = {},
    drawerContent: @Composable () -> Unit = {},
    @ColorInt scrimColor: Int = Color.Black.copy(alpha = 0.5f).toArgb(),
) {

    val mainFrameId = remember { View.generateViewId() }
    val composeMainId = remember { View.generateViewId() }
    val composeDrawerId = rememberSaveable { View.generateViewId() }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->

            DrawerLayout(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )

                // Create main content FrameLayout
                val mainFrame = FrameLayout(ctx).apply {
                    id = mainFrameId
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }

                // Create main ComposeView - fill parent
                val composeMain = ComposeView(ctx).apply {
                    id = composeMainId
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                }

                // Create drawer ComposeView with specific width
                val composeDrawer = ComposeView(ctx).apply {
                    id = composeDrawerId
                    layoutParams = DrawerLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    ).apply {
                        gravity = GravityCompat.START
                    }
                }

                // Build hierarchy
                mainFrame.addView(composeMain)
                addView(mainFrame)
                addView(composeDrawer)

                setScrimColor(scrimColor)
            }
        },
        update = { view ->



            state.setDrawer(view)

            state.initDrawer()

            // Set content to the ComposeViews
            val composeMain = view.findViewById<ComposeView>(composeMainId)
            val composeDrawer = view.findViewById<ComposeView>(composeDrawerId)

            composeMain.setContent {
                mainContent.invoke()
            }

            composeDrawer.setContent {
                drawerContent.invoke()
            }
        }
    )
}






@Composable
internal fun Basic_ViewDrawer_Sample(){
    Surface(modifier = Modifier
        .fillMaxSize()) {

        val state = rememberViewDrawerState(ViewDrawerValue.Closed)

        ViewDrawer(
            state = state,
            mainContent = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFB8FFB8))
                        .safeDrawingPadding()
                ) {

                    LazyRow(modifier = Modifier.align(Alignment.CenterStart)) {
                        items(count = 100) {
                            Button(
                                modifier = Modifier
                                    .width(150.dp)
                                    .height(50.dp),
                                onClick = {}
                            ) {
                                Text("Content $it")
                            }
                        }
                    }
                }

            },
            drawerContent = {
                Box(modifier = Modifier
                    .fillMaxHeight()
                    .width(200.dp)
                    .background(Color(0x9F6A6A6A))
                    .safeDrawingPadding()
                ){
                    Text("Hello from drawer!", color = Color.White)
                }
            })
    }
}