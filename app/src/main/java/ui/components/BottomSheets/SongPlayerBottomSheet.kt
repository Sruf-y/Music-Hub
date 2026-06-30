package ui.components.BottomSheets

import AppDatabase.SongRelated.Song
import ExoPlayer.ExoPlayerViewModel
import ExoPlayer.myExoPlayer
import SrufComposeComponents.IconButton
import SrufComposeComponents.Text
import android.app.Activity
import android.text.format.DateUtils
import android.view.View
import androidx.activity.compose.LocalActivity
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.aacomposetest.R
import com.aacomposetest.clickableNoRipple
import com.aacomposetest.invert
import com.aacomposetest.ui.theme.MyColor
import com.aacomposetest.ui.theme.Theme
import com.composables.core.BottomSheet
import com.composables.core.BottomSheetState
import com.composables.core.SheetDetent
import kotlinx.coroutines.delay
import ui.components.LoopingText
import java.io.File
import kotlin.math.roundToInt


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
   @DrawableRes errorDrawable: Int? = Theme.DefaultBackground(),
   //@FloatRange(0.0,1.0)
   blur: Float = 0f,
   @FloatRange(0.0,1.0)
   darkening:Float = 0f,
   defaultBackground:Color = Color.Black
){
   Box(modifier = modifier){
      
      // Default background
      Box(modifier = Modifier
         .fillMaxSize()
         .background(defaultBackground)
      )
      if(song==null || !File(song.thumbnailPath).exists()){
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
               .fillMaxSize(),
            contentScale = ContentScale.Crop // CenterCrop
         )
      }else{
         AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
               .data(song?.thumbnailPath)
               .allowHardware(true)
               .memoryCacheKey(song?.thumbnailPath)
               .placeholderMemoryCacheKey(song?.thumbnailPath)
               .crossfade(durationMillis = 250)
               .build(),
            contentDescription = "Song thumbnail scaled",
            modifier = Modifier
               .fillMaxSize()
               .let{
                  if(song==null || !File(song.thumbnailPath).exists())
                  {
                     it
                  }else{
                     it.blur((blur).dp)
                  }
               }
            ,
            contentScale = ContentScale.Crop // CenterCrop
         )
         
         // Darkening effect over the picture
         
         Box(
            modifier = Modifier
               .fillMaxSize()
               .background(
                  color = MyColor.Black.copy(alpha = darkening)
               )
         
         )
      }
      
      
   }
   
}



@Composable
private fun ButtonsRow(
   modifier: Modifier = Modifier.fillMaxWidth(),
   isPlaying: Boolean,
   horizontalArrangement: Arrangement.Horizontal = Arrangement.Center,
   playIconSize: Dp = 90.dp,
   sideIconSize:Dp = 70.dp,
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
            .size(sideIconSize)
            .clickable(
               interactionSource = remember { MutableInteractionSource() }, // to get rid of the default ripple effect
               indication = null, onClick = {
                  if (isEnabled) {
                     ExoPlayerViewModel.playPrevious()
                  }
               })
      )
      
      
      PlayPauseButton(modifier = Modifier.size(playIconSize),isPlaying = isPlaying,enabled = isEnabled)
      
      Image(
         painter = painterResource(R.drawable.ic_next),
         contentDescription = null,
         modifier = Modifier
            .size(sideIconSize)
            .clickable(
               interactionSource = remember { MutableInteractionSource() }, // to get rid of the default ripple effect
               indication = null, onClick = {
                  if (isEnabled) {
                     ExoPlayerViewModel.playNext()
                  }
               }
            )
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
   filledCollor: Color = Theme.Sourcesets.progressBar,
   backgroundColor: Color = Theme.Sourcesets.barBackground,
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
   onBackgroundClick:(Activity)->Unit = {
   
   }
) {
   val activity = LocalActivity.current
   
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
            .layout { measurable, constraints ->
               
               val placeable = measurable.measure(constraints)
               
               layout(placeable.width, placeable.height) {
                  placeable.place(0, 0 + backgroundYoffset.roundToInt())
               }
            },
         blur = 3f,
         darkening = 0.5f,
         song = currentSong,
         defaultBackground = Theme.colorScheme().onBackground
      )
      
      
      val range by remember(currentSong) {
         derivedStateOf {
            0f..(currentSong?.duration?.toFloat()?:1f)
         }
      }
      val curPos by myExoPlayer.currentPositionVM().value.collectAsState()
      
      
      
      // Expanded
      
      val isExpandedEnabled by remember {
         derivedStateOf {
            progressScalar != 0f
         }
      }
      
      BigPlayer(
         isExpandedEnabled = isExpandedEnabled,
         modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
               this.alpha = progressScalar
            }
            .clickableNoRipple(enabled = state.isIdle && progressScalar > 0f) {
               activity?.run {
                  
                  onBackgroundClick.invoke(activity)
               }
            },
         state = state,
         swapControls = swapControls,
         currentSong = currentSong,
         slider = {
            ResponsiveSlider(
               enabled = isExpandedEnabled
            )
         }
      )
      
      
      // Minimized
      val isMinimizedEnabled by remember {
         derivedStateOf {
            progressScalar != 1f
         }
      }
      
      SmallPlayer(
         isMinimizedEnabled = isMinimizedEnabled,
         modifier = Modifier
            .fillMaxWidth()
            .height(peekHeight)
            .graphicsLayer {
               alpha = invertedProgressScalar
            },
         value = curPos.toFloat()/range.endInclusive,
         currentSong = currentSong,
         gotoSong = gotoSong
      )
      
      
   }
   
   
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResponsiveSlider(
   modifier:Modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 12.dp),
   enabled: Boolean = true
) {
   
   val currentSong by myExoPlayer.currentlyPlayingSong.stateFlow.collectAsState()
   val curPos by myExoPlayer.currentPositionVM().value.collectAsState()
   
   val range by remember(currentSong) {
      derivedStateOf {
         0f..(currentSong?.duration?.toFloat()?:1f)
      }
   }
   
   val isUserDragging = remember { mutableStateOf(false) }
   val userDragConcluded = remember { mutableStateOf(false) }
   
   val originalSliderPosition = remember { mutableFloatStateOf(0f) }
   
   
   
   val calculatedSliderPosition by remember {
      derivedStateOf {
         
         if (isUserDragging.value == false && userDragConcluded.value == false) {
            curPos.toFloat()
         } else {
            originalSliderPosition.floatValue
         }
      }
   }
   
   LaunchedEffect(userDragConcluded.value) {
      if(userDragConcluded.value){
         delay(200)
         userDragConcluded.value = false
      }
   }
   
   val shape = remember { RoundedCornerShape(100) }
   
   Box(
      modifier = modifier
   ){
      val isSongShort by remember {
         derivedStateOf {
            myExoPlayer.isSongDurationShort
         }
      }
      // Time text
      Box(modifier = Modifier.fillMaxWidth()){
         
         val fontSize = remember { 14.sp }
         /** Left text */
         Text(
            if(isSongShort) calculatedSliderPosition.toLong().toString().padStart(4, '0')
            else
               DateUtils.formatElapsedTime((calculatedSliderPosition/1000).toLong()),
            color = Color.White,
            modifier = Modifier.align(Alignment.TopStart),
            fontSize = fontSize
         )
         
         
         /** Right text */
         Text(
            if(isSongShort) range.endInclusive.toLong().toString().padStart(4, '0')
            else
               DateUtils.formatElapsedTime((range.endInclusive/1000).toLong()),
            color = Color.White,
            modifier = Modifier.align(Alignment.TopEnd),
            fontSize = fontSize
         )
      }
      
      Slider(
         modifier = Modifier
            .padding(horizontal = 13.dp)
            .padding(top = 20.dp)
            .wrapContentHeight(),
         value = calculatedSliderPosition,
         onValueChange = { newValue ->
            
            isUserDragging.value = true
            
            originalSliderPosition.floatValue = newValue
         },
         onValueChangeFinished = {
            
            myExoPlayer.seekTo(originalSliderPosition.floatValue.toLong())
            
            isUserDragging.value = false
            
            userDragConcluded.value = true
            
         },
         valueRange = range,
         thumb = {
            Box(
               Modifier
                  .size(16.dp)
                  .background(Theme.Sourcesets.thumb, CircleShape)
            )
         },
         track = {
            
            val borderColor: Color? = remember { MyColor.Black }
            
            Box(
               Modifier
                  .fillMaxWidth()
                  .height(8.dp)
                  .shadow(elevation = 3.dp, shape = shape)
                  .clip(shape)
                  .background(Theme.Sourcesets.barBackground)
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
                     .fillMaxWidth(calculatedSliderPosition / range.endInclusive)
                     .fillMaxHeight()
                     .background(color = Theme.Sourcesets.seekBar)
               )
            }
         },
         enabled = enabled
      )
   }
   
}



@Composable
private fun SmallPlayer(
   isMinimizedEnabled: Boolean,
   modifier:Modifier,
   @FloatRange(0.0, 1.0) value: Float,
   currentSong: Song?,
   iconSize: Dp = 40.dp,
   gotoSong: () -> Unit,
) {
   Box(
      modifier
   ) {
      val isPlaying by myExoPlayer.isPlaying.collectAsState()
      
      RespProgressBar(
         value =value,
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
            currentSong?.title ?: stringResource(R.string.unknown),
            modifier = Modifier
               .weight(1f)
               .padding(horizontal = 16.dp),
            fontSize = 18.sp
         )
         
         Row {
            
            PlayPauseButton(
               modifier = Modifier.size(iconSize),
               isPlaying = isPlaying,
               enabled = isMinimizedEnabled
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            IconButton(
               imageVector = Icons.Default.LocationOn,
               modifier = Modifier.size(iconSize),
               onClick = {
                  currentSong?.let {
                     gotoSong.invoke()
                  }
               },
               iconSize = Size(0.8f, 0.8f),
               tint = MyColor.White,
               enabled = isMinimizedEnabled
            )
         }
         
         
      }
   }
}

@Composable
private fun BigPlayer(
   isExpandedEnabled: Boolean,
   modifier:Modifier,
   state: BottomSheetState,
   swapControls: Boolean,
   currentSong: Song?,
   slider:@Composable ()-> Unit,
   headText:@Composable ()->Unit = {
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
         LoopingText(currentSong?.title ?: stringResource(R.string.unknown), fontWeight = FontWeight.Bold)
         
         LoopingText(currentSong?.artistName?:stringResource(R.string.unknown), color = MyColor.SeparatorGray,)
      }
      
   }
){
   val isPlaying by myExoPlayer.isPlaying.collectAsState()
   
   LazyColumn(
      modifier = modifier,
      verticalArrangement = Arrangement.SpaceEvenly,
      horizontalAlignment = Alignment.CenterHorizontally,
      reverseLayout = swapControls
   ) {
      // Text area
      if (!swapControls) {
         item {
            Box(
               modifier = Modifier.fillMaxWidth(0.9f),
               contentAlignment = Alignment.Center
            ) {
               headText.invoke()
            }
         }
      }
      
      
      
      item {
         slider.invoke()
      }
      
      item {
         ButtonsRow(
            playIconSize = 75.dp,
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
               headText.invoke()
            }
         }
         
      }
      
      
   }
}

object ResponsivePlayerBSDefaults{

}