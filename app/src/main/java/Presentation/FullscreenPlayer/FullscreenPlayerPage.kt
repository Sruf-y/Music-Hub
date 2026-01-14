package Presentation.FullscreenPlayer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.aacomposetest.ui.theme.MyColor

@Composable
fun FullscreenPlayerPage(modifier: Modifier = Modifier,initialPage:Int = 0){

    val pages = remember{ listOf("Song","Lyrics") }

    val pagerState = rememberPagerState(initialPage = initialPage.coerceIn(0,pages.size)){ pages.size }

    Box(modifier = modifier.background(MyColor.SeparatorGray)){
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) {
            when(it){
                0->{// Fullscreen player
                    FullscreenSong(modifier = Modifier.fillMaxSize())
                }
                1->{// Lyrics page
                    LyricsPage(modifier = Modifier.fillMaxSize())
                }
            }
        }
    }


}