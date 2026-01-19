package Presentation.FullscreenPlayer

import AppDatabase.SongRelated.Song
import ExoPlayer.ExoPlayerViewModel
import ExoPlayer.myExoPlayer
import androidx.annotation.DrawableRes
import androidx.annotation.FloatRange
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.aacomposetest.Functions.clickableNoRipple
import com.aacomposetest.R
import com.aacomposetest.ui.theme.MyColor
import com.aacomposetest.ui.theme.SrufTheme
import ui.components.BottomSheets.PlayPauseButton
import ui.components.BottomSheets.ResponsiveSlider
import ui.components.LoopingText

@Preview
@Composable
private fun Preview(){


    SrufTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            content(null)
        }
    }


}
@Composable
internal fun FullscreenSong(modifier: Modifier = Modifier) {

    val currentSong: Song? by myExoPlayer.currentlyPlayingSong.collectAsState()


    content(currentSong,modifier)

}

@Composable
private fun content(currentSong: Song?,modifier:Modifier = Modifier){
    Column(modifier = modifier, verticalArrangement = Arrangement.SpaceAround) {

        ScreenSpace(verticalPercent = 0.5f){
            _ImageSpace(
                currentSong,
                Modifier.fillMaxSize()
            )
        }

        ScreenSpace(verticalPercent = 0.15f){
            _TextSpace(currentSong,modifier = Modifier
                .fillMaxSize()
                .background(MyColor.Red))
        }

        ScreenSpace(verticalPercent = 0.35f,modifier = Modifier
            .fillMaxSize()
            .background(MyColor.Blue)){
            _ButtonsSpace(currentSong)
        }




    }
}

@Composable
private fun _ImageSpace(song:Song?, modifier: Modifier = Modifier.fillMaxSize()){

    Box(modifier = modifier, contentAlignment = Alignment.Center){

        SongImage(song = song,modifier = Modifier
            .fillMaxSize(0.7f)
            .aspectRatio(1f)
        )
    }
}

@Composable
private fun SongImage(
    song:Song?,
    modifier:Modifier = Modifier.fillMaxSize(),
    @DrawableRes errorDrawable: Int? = R.drawable.exclamation_sign
){
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(song?.thumbnailPath)
            .allowHardware(true)
            .memoryCacheKey(song?.thumbnailPath)
            .placeholderMemoryCacheKey(song?.thumbnailPath)
            .let {
                if (errorDrawable != null) {
                    it.error(errorDrawable)
                } else it
            }.build(),
        contentDescription = null,
        modifier = modifier,
        contentScale = ContentScale.Crop // CenterCrop
    )

}

@Composable
private fun _TextSpace(song:Song?,modifier: Modifier = Modifier.fillMaxSize()){
    Box(modifier = modifier) {
        LoopingText(
            text = song?.title ?: Song.defaultTitle,
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun _ButtonsSpace(song:Song?,modifier: Modifier = Modifier.fillMaxSize()){

    val playIconSize = remember { 90.dp }

    val sideIconsSize = remember { 70.dp }
    
    val isPlaying by myExoPlayer.isPlaying.collectAsState()

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ){

        ResponsiveSlider()

        // TODO: Other buttons like shuffle, favorite etc. instead of this spacer
        Spacer(modifier = Modifier.weight(1f))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 40.dp)
        ) {

            Image(
                painter = painterResource(R.drawable.ic_previous),
                contentDescription = null,
                modifier = Modifier
                    .size(sideIconsSize)
                    .clickableNoRipple {
                        ExoPlayerViewModel.playPrevious(true)
                    }
            )

            PlayPauseButton(modifier = Modifier
                .size(playIconSize),
                isPlaying = isPlaying
            )

            Image(
                painter = painterResource(R.drawable.ic_next),
                contentDescription = null,
                modifier = Modifier
                    .size(sideIconsSize)
                    .clickableNoRipple {
                        ExoPlayerViewModel.playNext(true)
                    }
            )

        }
    }
}

@Composable
private fun ScreenSpace(
    @FloatRange(from = 0.0, to = 1.0) verticalPercent:Float = 1f,
    @FloatRange(from = 0.0, to = 1.0) horizontalPercent:Float = 1f,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.()-> Unit
){
    Box(
        modifier = modifier
            .fillMaxWidth(horizontalPercent)
            .fillMaxHeight(verticalPercent),
        content = content
    )
}