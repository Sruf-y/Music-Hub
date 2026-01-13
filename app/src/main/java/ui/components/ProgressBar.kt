package ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp

@Composable
private fun defaultTrack(value:Float){
    Box(modifier = Modifier
        .fillMaxSize()
        .background(Color.Gray)){

        Box(modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(value)
            .background(Color.Blue))
    }
}

@Composable
private fun defaultThumb(){
    Box(modifier = Modifier
        .fillMaxHeight()
        .width(5.dp)
        .background(Color.White)
    )
}

@Composable
fun ProgressBar(
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .height(20.dp),
    value:Float,
    maxVal:Float = 1f,
    track:@Composable ()-> Unit = { defaultTrack(value/maxVal) },
    thumb:@Composable ()->Unit = { defaultThumb() },
){
    var boxWidth by remember { mutableStateOf(0) }

    Box(modifier = modifier.onSizeChanged{
        boxWidth = it.width
    }){

        Box(modifier = Modifier.fillMaxSize().align(alignment = Alignment.Center)){
            track()
        }


        Box(modifier = Modifier.wrapContentSize().offset{
            IntOffset (((value/maxVal) * boxWidth).toInt(),0)
        }){
            thumb()
        }
    }


}
