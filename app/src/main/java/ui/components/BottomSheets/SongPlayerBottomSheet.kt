package ui.components.BottomSheets

import AppDatabase.SongRelated.Song
import ExoPlayer.ExoPlayerViewModel
import ExoPlayer.myExoPlayer
import SrufComposeComponents.IconButton
import SrufComposeComponents.Text
import android.text.format.DateUtils
import android.view.View
import androidx.annotation.DrawableRes
import androidx.annotation.FloatRange
import androidx.appcompat.widget.AppCompatImageView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableFloatState
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.aacomposetest.Functions.clickableNoRipple
import com.aacomposetest.Functions.invert
import com.aacomposetest.Log
import com.aacomposetest.R
import com.aacomposetest.ui.theme.SrufTheme
import com.aacomposetest.ui.theme.MyColor
import com.aacomposetest.ui.theme.Theme
import com.composables.core.BottomSheet
import com.composables.core.BottomSheetState
import com.composables.core.SheetDetent
import kotlinx.coroutines.delay
import ui.components.LoopingText


@Composable
fun PlayPauseButton(
    modifier: Modifier = Modifier,
    isPlaying: Boolean,
    enabled: Boolean = true,
) {


    AndroidView(
        factory = { context ->
            AppCompatImageView(context).apply {
                setImageResource(R.drawable.indian_music_button_toggle)
                isActivated = true
                isContextClickable = false
                isClickable = false
                isEnabled = false
            }
                  },
        modifier = modifier
            .run{
                if(enabled){
                    clickable(
                        interactionSource = remember { MutableInteractionSource() }, // to get rid of the default ripple effect
                        indication = null,
                        onClick = {
                            ExoPlayerViewModel.toggle()
                        }
                    )
                }else{
                    this
                }
            },
        update = { view ->
            // runs everytime a parameter of this composable changes.
            view.isActivated = isPlaying



            // I haven't found any other way
            view.visibility = if(enabled) View.VISIBLE else View.GONE

        }
    )
}


@Composable
fun ScalingBackground(
    modifier: Modifier = Modifier.fillMaxSize(),
    song: Song?,
    @DrawableRes errorDrawable: Int? = R.drawable.exclamation_sign,
    defaultBackground:Color = Color.Black
){



    Box(modifier = modifier){

        // Default background
        Box(modifier = Modifier
            .fillMaxSize()
            .background(defaultBackground))

        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(song?.thumbnailPath)
                .allowHardware(true)
                .memoryCacheKey(song?.thumbnailPath)
                .placeholderMemoryCacheKey(song?.thumbnailPath)
                .let {
                    if(errorDrawable!=null){
                        it.error(errorDrawable)
                    }
                    else
                        it
                }
                .build(),
            contentDescription = "Song thumbnail scaled",
            modifier = Modifier
                .fillMaxSize()
                .blur((0.5f).dp),
            contentScale = ContentScale.Crop // CenterCrop
        )

        // Darkening effect over the picture
        Box(modifier = Modifier
            .fillMaxSize()
            .background(color = Color(0x54000000)))
    }


}


@Composable
private fun ButtonsRow(
    modifier: Modifier = Modifier.fillMaxWidth(),
    isPlaying: Boolean,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Center,
    iconSize: Dp = 90.dp,
    isEnabled: Boolean = true,
) {

    Row(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement
    ) {

        Image(
            painter = painterResource(R.drawable.ic_previous),
            contentDescription = null,
            modifier = Modifier
                .size(iconSize)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() }, // to get rid of the default ripple effect
                    indication = null, onClick = {
                        if (isEnabled) {
                            ExoPlayerViewModel.playPrevious(true)
                        }
                    })
        )


        PlayPauseButton(modifier = Modifier.size(iconSize),isPlaying = isPlaying,enabled = isEnabled)

        Image(
            painter = painterResource(R.drawable.ic_next),
            contentDescription = null,
            modifier = Modifier
                .size(iconSize)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() }, // to get rid of the default ripple effect
                    indication = null, onClick = {
                        if (isEnabled) {
                            ExoPlayerViewModel.playNext(true)
                        }
                    })
        )
    }
}


@Composable
private fun RespProgressBar(
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .height(10.dp),
    @FloatRange(0.0,1.0) value: Float,
    shape: Shape = RectangleShape,
    filledCollor: Color = MyColor.Green,
    backgroundColor: Color = MyColor.SeparatorGray,
) {

    Box(
        modifier = modifier
            .clip(shape)
            .background(backgroundColor)

    ){
        Box(modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(value)
            .background(filledCollor))
    }
}

@Composable
private fun MinimizedPlayer(buttonsEnabled: Boolean){
    
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResponsivePlayerBS(
    modifier:Modifier = Modifier,
    state: BottomSheetState,
    shape:Shape = RoundedCornerShape(topStart = 15.dp, topEnd = 15.dp),
    expandedHeight: Dp= 250.dp,
    peekHeight:Dp= 60.dp,
    currentSong: Song?,
    centerPictureToMiddle:Boolean = false,
    swapControls:Boolean = false,
    gotoSong: () -> Unit = {

    },
    onShuffle:()->Unit = {
        myExoPlayer.playerSettings.isShuffling.invert()
    },
    onBackgroundClick:()->Unit = {}
) {

    val isPlaying by myExoPlayer.isPlaying.collectAsState(false)


    // middle would be if both members of the equation were divided by 2, but the middle does create some flickers.
    /**
     * Difference at which point the image's center sits at either (down->up) 75% if not centered, or 50% if centered.
     * */
    val difference by remember { mutableFloatStateOf((expandedHeight.value/2-peekHeight.value/(if(centerPictureToMiddle)2 else 1))) }

    /**
     * State progress as a float 0~1 value between the lowest point and the highest point.
     * */
    val progressScalar by remember {
        derivedStateOf {

            if (state.targetDetent == SheetDetent.FullyExpanded)
                state.progress // Peek fades in
            else
                1f - state.progress
        }
    }

    val invertedProgressScalar by remember {
        derivedStateOf {
            1f - progressScalar
        }
    }



    /**
     * Final offset calculation for the background
     * */
    val backgroundYoffset by remember {
        derivedStateOf {
            (1f - progressScalar) * (-difference)
        }
    }


    BottomSheet(
        modifier = modifier
            .height(expandedHeight),
        state = state,
        shape = shape
    ) {

        ScalingBackground(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = backgroundYoffset.dp),
            song = currentSong,
            defaultBackground = Theme.colorScheme().onBackground
        )

        val range by remember(currentSong) {
            derivedStateOf {

                (0f..(currentSong?.duration?.toFloat()?:1f)).apply {
                    Log(this.endInclusive.toString())
                }
            }
        }

        val sliderPosition = remember { mutableFloatStateOf(0f) }
        val isUserDragging = remember { mutableStateOf(false) }
        val curPos by myExoPlayer.getCurrentPositionAsFlow().progress.collectAsState()

        var userDragConcluded by remember { mutableStateOf(false) }

        val realSliderPosition by remember {
            derivedStateOf {
                if(!isUserDragging.value && userDragConcluded == false){
                    curPos.toFloat()
                }else{
                    sliderPosition.floatValue
                }
            }
        }

        LaunchedEffect(userDragConcluded) {
            if(userDragConcluded){
                delay(200)
                userDragConcluded = false
            }
        }


        // Expanded

        val isExpandedEnabled by remember {
            derivedStateOf {
                progressScalar != 0f
            }
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .alpha(progressScalar),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally,
            reverseLayout = swapControls
        ) {
            val isShuffling by myExoPlayer.playerSettings.isShuffling
            // Text area
            if (!swapControls) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(0.9f),
                        contentAlignment = Alignment.Center
                    ) {
                        LoopingText(currentSong?.title ?: "<unknown>")

                        IconButton(
                            imageVector = if(isShuffling) Icons.Default.Settings else Icons.Default.Close,
                            modifier = Modifier
                                .size(60.dp)
                                .align(Alignment.TopEnd),
                            iconSize = Size(0.7f,0.7f),
                            iconTint = MyColor.White,
                            enabled = isExpandedEnabled,
                            onclick = {
                                onShuffle.invoke()
                            }
                        )
                    }
                }
            }



            item {
               ResponsiveSlider(sliderPosition,isExpandedEnabled)

            }

            item {
                ButtonsRow(
                    iconSize = 75.dp,
                    isPlaying = isPlaying,
                    modifier = Modifier.shadow(
                        5.dp,
                        RoundedCornerShape(100),
                        ambientColor = Color(0x25000000),
                        spotColor = MyColor.SemiTransparentBackground
                    ),
                    isEnabled = isExpandedEnabled
                )
            }

            if (swapControls) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(0.9f),
                        contentAlignment = Alignment.Center
                    ) {
                        LoopingText(currentSong?.title ?: "<unknown>")

                        IconButton(
                            imageVector = if(isShuffling) Icons.Default.Settings else Icons.Default.Close,
                            modifier = Modifier
                                .size(60.dp)
                                .align(Alignment.TopEnd),
                            iconSize = Size(0.7f,0.7f),
                            iconTint = MyColor.White,
                            enabled = isExpandedEnabled,
                            onclick = {
                                onShuffle.invoke()
                            }
                        )
                    }
                }

            }


        }


        // Minimized
        val minimizedIconSize = remember { 40.dp }
        val isMinimizedEnabled by remember {
            derivedStateOf {
                progressScalar != 1f
            }
        }
        Box(
            Modifier
                .fillMaxWidth()
                .height(peekHeight)
                .alpha(invertedProgressScalar)
                .clickableNoRipple{
                    onBackgroundClick.invoke()
                }
        ) {

            RespProgressBar(
                value = realSliderPosition / range.endInclusive,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .align(Alignment.BottomStart)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LoopingText(
                    currentSong?.title ?: "<unknown>",
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    fontSize = 18.sp
                )

                Row {

                    PlayPauseButton(
                        modifier = Modifier.size(minimizedIconSize),
                        isPlaying = isPlaying,
                        enabled = isMinimizedEnabled
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        imageVector = Icons.Default.LocationOn,
                        modifier = Modifier.size(minimizedIconSize),
                        onclick = {
                            currentSong?.let {
                                gotoSong.invoke()
                            }
                        },
                        iconSize = Size(0.8f, 0.8f),
                        iconTint = MyColor.White,
                        enabled = isMinimizedEnabled
                    )
                }


            }
        }


    }


}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResponsiveSlider(sliderPosition: MutableFloatState, enabled: Boolean = true){

    val currentSong by myExoPlayer.currentlyPlayingSong.collectAsState()

    val isPlaying by myExoPlayer.isPlaying.collectAsState(false)
    //val curPos by myExoPlayer.getCurrentPositionAsFlow().progress.collectAsState()
    val duration = remember(currentSong) { currentSong?.duration?:1L }


    val range by remember(currentSong) {
        derivedStateOf {

            (0f..(currentSong?.duration?.toFloat()?:1f)).apply {
                Log(this.endInclusive.toString())
            }
        }
    }

    val isUserDragging = remember { mutableStateOf(false) }
    val curPos by myExoPlayer.getCurrentPositionAsFlow().progress.collectAsState()

    var userDragConcluded by remember { mutableStateOf(false) }

    val realSliderPosition by remember {
        derivedStateOf {
            if(!isUserDragging.value && userDragConcluded == false){
                curPos.toFloat()
            }else{
                sliderPosition.floatValue
            }
        }
    }

    LaunchedEffect(userDragConcluded) {
        if(userDragConcluded){
            delay(200)
            userDragConcluded = false
        }
    }

    val shape = remember { RoundedCornerShape(100) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Slider(
            modifier = Modifier
                .padding(horizontal = 13.dp),
            value = realSliderPosition,
            onValueChange = { newValue ->

                isUserDragging.value = true

                sliderPosition.floatValue = newValue
            },
            onValueChangeFinished = {

                myExoPlayer.seekTo(sliderPosition.floatValue.toLong())

                isUserDragging.value = false

                userDragConcluded = true

            },
            valueRange = range,
            thumb = {
                Box(
                    Modifier
                        .size(16.dp)
                        .background(Color.White, CircleShape)
                )
            },
            track = {


                val borderColor: Color? = remember { MyColor.Black }

                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(8.dp).shadow(elevation = 3.dp)
                        .clip(shape)
                        .background(MyColor.SeparatorGray)
                        .let {
                            if (borderColor != null) {
                                it.border(
                                    width = Dp.Hairline,
                                    color = borderColor,
                                    shape = shape
                                )
                            } else {
                                it
                            }
                        }


                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(realSliderPosition / range.endInclusive)
                            .fillMaxHeight()
                            .background(color = MyColor.Blue)
                    )
                }
            },
            enabled = enabled
        )

        // Time text
        Box(modifier = Modifier
            .fillMaxWidth()
        ){
            Text(
                if(duration<5*1000) realSliderPosition.toLong().toString().padStart(4, '0')
                else
                    DateUtils.formatElapsedTime((realSliderPosition/1000).toLong()),
                color = Color.White,
                modifier = Modifier.align(Alignment.CenterStart)
            )



            Text(
                if(duration<5*1000) range.endInclusive.toLong().toString().padStart(4, '0')
                else
                    DateUtils.formatElapsedTime((range.endInclusive/1000).toLong()),
                color = Color.White,
                modifier = Modifier.align(Alignment.CenterEnd)
            )
        }
    }

}

@Preview(showBackground = true)
@Composable
fun ResponsivePlayerPreview(){
    SrufTheme {
        Surface {
            Box(modifier = Modifier
                .fillMaxSize()
                .background(MyColor.White)){



            }
        }
    }

}



object ResponsivePlayerBSDefaults{

}