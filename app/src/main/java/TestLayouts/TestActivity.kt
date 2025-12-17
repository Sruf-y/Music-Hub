package TestLayouts


import AppDatabase.SongRelated.Song
import SrufComposeComponents.Experimental.ColorPicker
import SrufComposeComponents.Text
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.aacomposetest.ui.theme.ComposeTestTheme
import com.aacomposetest.ui.theme.MyColor
import kotlinx.coroutines.launch
import SrufComposeComponents.NestedScrollingFront
import android.view.View
import android.widget.FrameLayout
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Surface
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.viewinterop.AndroidView
import androidx.drawerlayout.widget.DrawerLayout


class TestActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()



        setContent {
            ComposeTestTheme {

                Surface(modifier = Modifier
                    .fillMaxSize()
                    .safeDrawingPadding(), color = MyColor.SemiTransparentBackground) {
                    ColorPicker()
                }

                //NestedScrollTest()
            }
        }

    }
}


@Composable
private fun NestedScrollTest(){
    val songsList =
        List(100) { index -> Song.emptySong(id = index.toLong(), title = "Song ${index}") }

    val pagerState = rememberPagerState( initialPage = 0){3}
    NestedScrollingFront(
        appbar = {
            Text("Hello appbar",modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(MyColor.Green)
            )
        },
        tabRow = {
            val scope = rememberCoroutineScope()
            TabRow(
                selectedTabIndex = pagerState.currentPage,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
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
        },
        pager = {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> MyPagerPage(songsList)
                    1 -> MySecondPage(songsList)
                    2 -> MyThirdPage()
                }
            }
        }
    )
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
                Text(text = it.title, Modifier
                    .align(Alignment.CenterStart)
                    .background(MyColor.Pink40))
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












