package Presentation.FullscreenPlayer

import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

@Composable
fun FullscreenPlayerPage(modifier: Modifier = Modifier,initialPage:Int = 0){

    val pages = remember{ listOf("Song","Lyrics") }

    val pagerState = rememberPagerState(initialPage = initialPage.coerceIn(0,pages.size)){ pages.size }

    HorizontalPager(
        state = pagerState,
        modifier = modifier
    ) {
        when(it){
            0->{// Fullscreen player
                // TODO FullscreenSong()
            }
            1->{// Lyrics page
                // TODO LyricsPage()
            }
        }
    }

}