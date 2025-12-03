package ui.components.BottomSheets

import AppDatabase.SongRelated.Song
import ExoPlayer.ExoPlayerViewModel
import ExoPlayer.myExoPlayer
import SrufComposeComponents.IconButton
import SrufComposeComponents.Text
import android.text.format.DateUtils
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.AppCompatImageView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.aacomposetest.Log
import com.aacomposetest.R
import com.aacomposetest.ui.theme.MyColor
import com.aacomposetest.ui.theme.Theme
import com.composables.core.BottomSheet
import com.composables.core.BottomSheetState
import com.composables.core.SheetDetent
import com.composables.core.rememberBottomSheetState
import ui.components.ProgressBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongPlayerBottomSheetFrame(
    modifier: Modifier,
    exoPlayerVM: ExoPlayerViewModel,
    peekHeight: Dp= 60.dp,
    expandedHeight: Dp= 250.dp,
    content: @Composable (Modifier) -> Unit = {}
) {

    Box(modifier = modifier
    ) {

        /**
         * Scaffold content
         * */

        Surface(
            modifier = Modifier
                .fillMaxSize()
        ) {

            val Peek = SheetDetent("Peek") { containerHeight, sheetHeight ->
                //containerHeight*0.2f

                peekHeight
            }
            val sheetstate = rememberBottomSheetState(
                initialDetent = Peek,
                detents = listOf(Peek, SheetDetent.FullyExpanded)
            )

            content.invoke(Modifier.padding(bottom = peekHeight))


            val shape = RoundedCornerShape(topStart = 15.dp, topEnd = 15.dp)
            SongPlayingBottomSheet(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(expandedHeight),
                sheetstate = sheetstate,
                backgroundColor = Theme.colorScheme().onBackground,
                shape = shape,
                expandedHeight = expandedHeight,
                peekHeight = peekHeight,
                exoPlayerVM = exoPlayerVM,

                )

        }


    }
}

@Composable
fun SongPlayingBottomSheet(
    sheetstate: BottomSheetState,
    backgroundColor:Color = Color.Unspecified,
    shape:Shape = RoundedCornerShape(topStart = 15.dp, topEnd = 15.dp),
    modifier:Modifier,
    expandedHeight: Dp= 250.dp,
    peekHeight:Dp= 60.dp,
    exoPlayerVM: ExoPlayerViewModel,
    centerPictureToMiddle:Boolean = false
){





    BottomSheet(state = sheetstate,
        backgroundColor = backgroundColor,
        shape = shape,
        modifier = modifier
    ) {

        Box(modifier = Modifier.fillMaxSize()) {

            val currentSong:Song? by exoPlayerVM.currentSong.collectAsState()
            val currentPosition by exoPlayerVM.currentProgress.collectAsState()
            val duration by remember(currentSong) { derivedStateOf { currentSong?.duration ?: 1 } }

            val isPlaying by exoPlayerVM.isPlaying.collectAsState()
            val sliderPositionCalculated by remember(duration,currentSong) {
                derivedStateOf {
                    currentPosition.toFloat() / (duration.toFloat())
                }
            }
            val sliderPosition = remember { mutableFloatStateOf(

                sliderPositionCalculated
            ) }



            val alpha =
                if(sheetstate.targetDetent == SheetDetent.FullyExpanded)
                    sheetstate.progress // Peek fades in
                else
                    1f - sheetstate.progress



            val difference by remember { mutableFloatStateOf((expandedHeight.value/2-peekHeight.value/(if(centerPictureToMiddle)2 else 1))) }
            // middle would be if both members of the equation were divided by 2, but the middle does create some flickers.


            val isUserDragging = remember { mutableStateOf(false) }

            // Keep slider following playback only if user is not dragging
            LaunchedEffect(currentPosition, isUserDragging) {
                if (!isUserDragging.value && (currentSong?.duration?:0) > 0) {
                    sliderPosition.floatValue = currentPosition.toFloat() / (currentSong?.duration?:0).toFloat()
                }
            }

            ScalingBackground(
                modifier = Modifier
                    .fillMaxWidth()
                    //.height(currentHeightPx.dp/3),
                    .fillMaxHeight()
                    .offset(y = ((1f - alpha) * (-difference)).dp),
                currentSong = currentSong,
                exoPlayerVM = exoPlayerVM
            )

            ExpandedPlayer(exoPlayerVM = exoPlayerVM,
                modifier = Modifier
                    .alpha(alpha),
                currentSong = currentSong,
                sliderPosition = sliderPosition,
                disabled = alpha<1f,
                isPlaying = isPlaying,
                isUserDragging = isUserDragging,
                duration = duration
            )

            PeekPlayer(
                modifier = Modifier
                    .height(peekHeight)
                    .fillMaxWidth()
                    .alpha(1f - alpha),
                sliderPosition = sliderPosition,
                isPlaying = isPlaying,
                exoPlayerVM = exoPlayerVM
            )
        }
    }
}

@Composable
private fun ExpandedPlayer(
    disabled:Boolean = false,
    modifier: Modifier,
    exoPlayerVM: ExoPlayerViewModel?= null,
    currentSong: Song?,
    isPlaying:Boolean,
    sliderPosition: MutableState<Float>,
    isUserDragging: MutableState<Boolean>,
    duration: Long
){

    val iconsSize = 100.dp

    val myTitle = currentSong?.title?:"<missing title>"




    Box(modifier = modifier.pointerInteropFilter { disabled }){
        Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = myTitle,modifier = Modifier.weight(1f), color = Color.White)
            Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.Center) {


                Image(
                    painter = painterResource(R.drawable.ic_previous),
                    contentDescription = null,
                    modifier = Modifier
                        .size(iconsSize)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() }, // to get rid of the default ripple effect
                            indication = null, onClick = {
                                exoPlayerVM?.playPrevious()
                            }
                        )
                )

                // PAUSE/PLAY BUTTON
                PlayPauseButton(
                    modifier = Modifier.size(iconsSize),
                    isPlaying = isPlaying,
                    exoPlayerVM = exoPlayerVM
                )

                Image(
                    painter = painterResource(R.drawable.ic_next),
                    contentDescription = null,
                    modifier = Modifier
                        .size(iconsSize)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() }, // to get rid of the default ripple effect
                            indication = null, onClick = {
                                exoPlayerVM?.playNext()
                            }
                        )
                )


            }

            SongSeekBar(modifier = Modifier.weight(1f),
                exoPlayerVM = exoPlayerVM,
                sliderPosition = sliderPosition,
                isUserDragging = isUserDragging,
                duration = duration
            )



        }
    }


}



@Composable
private fun PeekPlayer(
    modifier: Modifier,
    isPlaying: Boolean,
    exoPlayerVM: ExoPlayerViewModel?=null,
    sliderPosition: MutableState<Float>
){

    val barHeight = 6.dp
    val iconsSize = 40.dp



    Box(modifier = modifier.clipToBounds()){

        Column(modifier = Modifier.fillMaxSize()){

            Row(modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {


                PlayPauseButton(
                    modifier = Modifier
                        .size(iconsSize),
                    isPlaying = isPlaying,
                    exoPlayerVM = exoPlayerVM
                )

                IconButton(imageVector = Icons.Default.LocationOn,
                    modifier = Modifier.size(iconsSize),
                    onclick = {
                        // locate currently playing song by playlist. If curPlaylist == null, look in the first page of the pager, in global songs list
                    }
                )

            }


            SongProgressBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(barHeight),
                sliderPosition = sliderPosition,
                barBackground = MyColor.SemiTransparentBackground
            )
        }

    }


}

@Composable
private fun PlayPauseButton(modifier: Modifier,isPlaying:Boolean,exoPlayerVM: ExoPlayerViewModel?=null){


    AndroidView(
        factory = { context ->
            AppCompatImageView(context).apply {
                setImageResource(R.drawable.indian_music_button_toggle)
                isActivated = true
            }
        }, modifier = modifier
            .clickable(interactionSource = remember { MutableInteractionSource() }, // to get rid of the default ripple effect
                indication = null,
                onClick = {
                    exoPlayerVM?.toggle()
                }), update = { view ->
            // runs everytime isPlaying changes
            view.isActivated =
                isPlaying
        }
    )
}


@Composable
private fun ScalingBackground(
    modifier: Modifier = Modifier.fillMaxSize(),
    currentSong: Song?,
    exoPlayerVM: ExoPlayerViewModel?,
    @DrawableRes errorDrawable: Int? = R.drawable.exclamation_sign

){
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(currentSong?.thumbnailPath)
            .allowHardware(true)
            .memoryCacheKey(currentSong?.thumbnailPath)
            .placeholderMemoryCacheKey(currentSong?.thumbnailPath)
            .apply {
                if(errorDrawable!=null){
                    this.error(errorDrawable)
                }
            }
            .build(),
        contentDescription = "Song thumbnail scaled",
        modifier = modifier
            .blur((0.5f).dp),
        contentScale = ContentScale.Crop // CenterCrop
    )

    Box(modifier = modifier.background(color = Color(0x54000000)))
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SongSeekBar(
    modifier:Modifier,
    exoPlayerVM: ExoPlayerViewModel?= null,
    /**
     *Value is to be between 0f and 1f
     * */
    sliderPosition: MutableState<Float>,
    isUserDragging: MutableState<Boolean>,
    duration: Long
) {





    Row(
        modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {

        Text(text = DateUtils.formatElapsedTime(((sliderPosition.value*duration)/1000).toLong()), color = Color.White)

        Slider(modifier = Modifier
            .weight(1f)
            .padding(5.dp),
            value = sliderPosition.value,
            onValueChange = { newValue ->

                exoPlayerVM?.progressVM?.stopUpdates()
                isUserDragging.value = true

                sliderPosition.value = newValue
            },
            onValueChangeFinished = {

                val seekPosition = (sliderPosition.value * duration).toLong()

                isUserDragging.value = false


                exoPlayerVM?.seekTo(pos = seekPosition, onFinish = {
                    // value was up to 250ms too slow, due to progressVM post times
                    exoPlayerVM.progressVM.forceValue(seekPosition)
                    exoPlayerVM.progressVM.startUpdates()
                })

            },
            valueRange = 0f..1f,
            thumb = {
                Box(
                    Modifier
                        .size(20.dp)
                        .background(Color.White, CircleShape)
                )
            },
            track = {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(100.dp))
                        .background(Color.Gray)
                ){
                    Box(modifier = Modifier
                        .fillMaxWidth(sliderPosition.value)
                        .fillMaxHeight()
                        .background(color = Color.Blue))
                }
            }
        )

        Text(text = DateUtils.formatElapsedTime(duration/1000), color = Color.White)
    }


}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SongProgressBar(
    modifier: Modifier,
    sliderPosition: MutableState<Float>,
    barBackground: Color = Color.Gray,
    barProgress: Color = Color.Blue,
) {


    ProgressBar(
        modifier = modifier,
        value = sliderPosition.value,
        thumb = {},
        track = {
            Box(
                modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .background(barBackground)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(sliderPosition.value)
                        .fillMaxHeight()
                        .background(color = barProgress)
                )
            }
        }
    )


}