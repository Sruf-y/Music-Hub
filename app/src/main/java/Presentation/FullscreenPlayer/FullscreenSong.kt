package Presentation.FullscreenPlayer

import AppDatabase.SongRelated.Song
import ExoPlayer.myExoPlayer
import androidx.annotation.DrawableRes
import androidx.annotation.FloatRange
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.aacomposetest.Classes.MyIcons
import com.aacomposetest.R
import com.aacomposetest.ui.theme.MyColor
import com.aacomposetest.ui.theme.SrufTheme
import ui.components.BottomSheets.PlayPauseButton

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
fun FullscreenSong(modifier: Modifier = Modifier) {

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
    if(song == null)
        AsyncImage(model = ImageRequest.Builder(LocalContext.current)
            .data(R.drawable.exclamation_sign)
            .allowHardware(true)
            .build(),
            contentDescription = null,
            modifier = modifier,
            contentScale = ContentScale.Crop // CenterCrop
        )
    else
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
    Box(modifier = modifier){

    }
}

@Composable
private fun _ButtonsSpace(song:Song?,modifier: Modifier = Modifier.fillMaxSize()){

    val iconSize = remember { 90.dp }

    val isPlaying by myExoPlayer.isPlaying.collectAsState()

    Box(modifier = Modifier){

        Row(
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {

            PlayPauseButton(modifier = Modifier.size(iconSize).background(MyColor.Black),isPlaying = isPlaying)
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