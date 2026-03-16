package Presentation.FullscreenPlayer

import android.content.res.Configuration
import com.aacomposetest.Classes.PersistentState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import com.aacomposetest.Log
import com.aacomposetest.ui.theme.MyColor
import ui.components.BottomSheets.renderDefaultBackground

@Composable
fun FullscreenPlayerPage(modifier: Modifier = Modifier,initialPage:Int? = null){
    
    val viewmodel: InitialState = remember {
        InitialState().apply {
            if (initialPage != null) {
                this.initialPage = initialPage
            }
        }
    }
    

    val pagerState = rememberPagerState(initialPage = viewmodel.initialPage.coerceIn(0,viewmodel.pages.size)){ viewmodel.pages.size }
    
    
    LaunchedEffect(pagerState.currentPage) {
        viewmodel.initialPage = pagerState.currentPage
    }
    
    Box(modifier = modifier.background(MyColor.SeparatorGray)){
        renderDefaultBackground(Modifier.fillMaxSize())
        
        
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) {
            when (it) {
                0 -> {// Fullscreen player
                    FullscreenSong(modifier = Modifier.fillMaxSize())
                }
                
                1 -> {// Lyrics page
                    LyricsPage(modifier = Modifier.fillMaxSize())
                }
            }
        }
        
    }
}


private class InitialState(){
    
    var initialPage by PersistentState("fullscreenViewmodel", { 0 })
    
    val pages = listOf("Song","Lyrics")
}



