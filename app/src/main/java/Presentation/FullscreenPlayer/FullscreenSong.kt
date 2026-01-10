package Presentation.FullscreenPlayer

import AppDatabase.SongRelated.Song
import ExoPlayer.myExoPlayer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment

@Composable
fun FullscreenSong(modifier: Modifier = Modifier) {

    val currentSong: Song? by myExoPlayer.currentlyPlayingSong.collectAsState()

    Column(modifier = Modifier, verticalArrangement = Arrangement.SpaceAround) {
        _ImageSpace(currentSong)

        _TextSpace(currentSong)

        _ButtonsSpace(currentSong)
    }

}

@Composable
private fun _ImageSpace(song:Song?){

}

@Composable
private fun _TextSpace(song:Song?){

}

@Composable
private fun _ButtonsSpace(song:Song?){

    Box(modifier = Modifier){

        Row(
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {

        }
    }
}