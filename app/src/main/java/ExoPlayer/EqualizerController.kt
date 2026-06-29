package ExoPlayer

import android.media.audiofx.DynamicsProcessing
import androidx.annotation.OptIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import com.aacomposetest.Log
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.util.lerp
import com.aacomposetest.clickableNoRipple
import com.aacomposetest.totalLength
import com.aacomposetest.ui.theme.MyColor
import ui.components.CenteredPriorityLayout
import ui.components.CubicControl
import ui.components.Overlay
import ui.components.VerticalSlider
import ui.components.rememberSliderState
import kotlin.Float
import kotlin.Pair
import kotlin.math.roundToInt

/**
 * TODO: finish this
 * */
@OptIn(UnstableApi::class)
class EqualizerController constructor(sessionId: Int){
   /**
    * Equalizer affects only captured noise. This means there is no need to destroy it since it dies with the proccess.
    *
    * Folosesc doar preEq, o singura banda de compresie si un limitator pentru ca este mai simplu. Desi pot extinde pe viitor
    * */
   private val equalizer: DynamicsProcessing
   
   
   var enabled by mutableStateOf(false)
      private set
   
   fun enable() {
      equalizer.enabled = true
      enabled = equalizer.enabled
   }
   fun disable(){
      equalizer.enabled = false
      enabled = equalizer.enabled
   }
   
   fun switchEnable(){
      if(equalizer.enabled){
         disable()
      }else{
         enable()
      }
   }
   
   
   
   /**
    *
    * @param decibels only accepts values in range of [[BAND_LOWER_BOUNDS],[BAND_UPPER_BOUNDS]]. Values outside are not applied.
    * */
   fun setAllChannelsBandValue(
      index:Int,
      decibels:Float
   ){
      if(decibels in BAND_LOWER_BOUNDS.. BAND_UPPER_BOUNDS){
      
      }
   }
   // First is cutoffFrequency, second is decibels
   val bands: SnapshotStateList<Pair<Float, Float>>
   init {
      equalizer = DynamicsProcessing(
         0,
      sessionId,
      DynamicsProcessing.Config.Builder(
         /* variant = */ DynamicsProcessing.VARIANT_FAVOR_FREQUENCY_RESOLUTION,
         /* channelCount = */ 2,
         /* preEqInUse = */ true,
         /* preEqBandCount = */ 10,
         /* mbcInUse = */ true,
         /* mbcBandCount = */ 1, // single band of compression that goes across the full range
         /* postEqInUse = */ false,
         /* postEqBandCount = */ 0, // not used so 0
         /* limiterInUse = */ true
      ).build()
      )
      
      bands = mutableStateListOf<Pair<Float,Float>>()
      
      disable()
      _initialize()
   }
   
   
   var isDualChannel = false
      private set
   
   private fun _initialize(){
      
      val bandsDefaultCutoffFrequency = listOf<Float>(32f, 64f, 125f, 250f, 500f, 1000f, 2000f, 4000f, 8000f, 16000f)
      
      // try to do a load from storage first, then leave the following code as is (hope it doesn't break by then)
      
      val a = onLoadLogic()
      
      // Add the bands to the stateList
      if(equalizer.channelCount>0){
         if(equalizer.channelCount == 2){
            isDualChannel = true
         }
         
         // get the pre-eq which will become the single point of input for bands
         val preEq = equalizer.getPreEqByChannelIndex(0)
         
         for(i in 0 until preEq.bandCount){
            val band = preEq.getBand(i)
            band.cutoffFrequency = bandsDefaultCutoffFrequency[i]
            equalizer.setPreEqBandAllChannelsTo(i,band) // set the bands from pre-eq for all channels to uniformize channels based on pre-eq values
            bands.add(band.cutoffFrequency to band.gain)
         }
      }
   }
   
   private fun iLog(message:String){
      Log(message,"EQUALIZER","i")
   }
   /**
    * Accepts values between [BAND_LOWER_BOUNDS] and [BAND_UPPER_BOUNDS]
    * */
   fun setGain(decibels: Float){
      if(decibels in BAND_LOWER_BOUNDS..BAND_UPPER_BOUNDS) {
         equalizer.setInputGainAllChannelsTo(decibels)
      }
   }
   fun updateBand(index:Int,decibels:Float){
      if(decibels in BAND_LOWER_BOUNDS..BAND_UPPER_BOUNDS) {
         val band = bands[index]
         
         _privUpdateBand(index,band,decibels)
      }
   }
   fun resetBands(){
      val decibels = 0f
      bands.forEachIndexed {index, band->
         _privUpdateBand(index,band,decibels)
      }
   }
   //TODO
   fun updateMBC(){
   
   }
   //TODO
   fun updateLimiter(){
   
   }
   /**
    * Accepts 0..100.
    * */
   fun setGainLeft(percentage: Float) {
      if (isDualChannel) {
         if (percentage in 0f..100f) {
            equalizer.setInputGainbyChannel(0,percentage)
         }
      }
   }
   /**
    * Accepts 0..100.
    * */
   fun setGainRight(percentage: Float){
      if(isDualChannel){
         if(percentage in 0f..100f) {
            equalizer.setInputGainbyChannel(1,percentage)
         }
      }
   }
   
   private fun _privUpdateBand(index:Int, band:Pair<Float, Float>, decibels:Float){
      equalizer.setPreEqBandAllChannelsTo(
         index,
         DynamicsProcessing.EqBand(true, band.first, decibels)
      )
      bands[index] = band.copy(second = decibels)
   }
   
   
   // TODO: Finish storage logic (REALLY need to automate this sometime)
   /**
    * @param filename the name/id_name of the file to save data to. Ex: song's id to be able to save per song configs
    * */
   fun onSaveLogic(
      filename: String = "Equalizer Data",
      folder: String = "Equalizer"
   ){
   
   }
   
   /**
    * @param filename the name/id_name of the file to restore the data from. Ex: song's id to be able to save per song configs
    * */
   //TODO: Finish storage logic and change its return type to match.
   fun onLoadLogic(
      filename: String = "Equalizer Data",
      folder: String = "Equalizer"
   ):Unit{
   
   }
   
   //TODO: Add logic for checking if a song has an entry in the equalizer config SQL table and update the settings based on that.
   
   companion object{
      
      @JvmField
      val BAND_UPPER_BOUNDS = 15f
      @JvmField
      val BAND_LOWER_BOUNDS = -15f
      
      @kotlin.OptIn(ExperimentalMaterial3Api::class)
      @Composable
      internal fun BandsTestPreview(equalizer: EqualizerController? = myExoPlayer.equalizerController){
         
         val data by remember {
            derivedStateOf {
               equalizer?.bands
            }
         }
         
         
         Box(contentAlignment = Alignment.Center,modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
         ){
            Column {
               if(equalizer!=null){
                  
                  // Enable switch
                  Overlay(
                     modifier = Modifier.fillMaxWidth(),
                     overlay = {
                        Box(
                           Modifier.clickableNoRipple{
                              equalizer.switchEnable()
                           }
                        )
                     }
                  ) {
                     Card {
                        Row(
                           verticalAlignment = Alignment.CenterVertically,
                           horizontalArrangement = Arrangement.SpaceBetween,
                           modifier = Modifier.fillMaxWidth()
                        ) {
                           Text("Enable")
                           Switch(
                              equalizer.enabled,
                              {}
                           )
                        }
                     }
                     
                  }
                  
                  // Bands modulation
                  data?.let { data ->
                     Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        
                        var parentCoords: LayoutCoordinates? by remember { mutableStateOf(null) }
                        
                        val points = remember {
                           SnapshotStateList<Offset>(
                              data.size
                           ) { Offset.Zero }
                        }
                        val centerPoints = remember {
                           SnapshotStateList<Offset>(
                              data.size
                           ) { Offset.Zero }
                        }
                        
                        val sliderHeight = remember { 300.dp }
                        val centerAnchorSnapRange = remember { 0.5f }
                        
                        val cubicControl: CubicControl = remember { CubicControl(0.3f,0.1f,0.7f,0.9f) }
                        
                        
                        val lineWidth = remember { 3.dp }
                        val connectionColor by remember {
                           derivedStateOf {
                              if (equalizer.enabled) {
                                 MyColor.Green
                              } else {
                                 MyColor.DisabledGray
                              }
                           }
                        }
                        val disconnectedLineWidth = remember { 1.dp }
                        val disconnectedColor = remember { MyColor.SeparatorGray }
                        
                        Card(modifier = Modifier
                           .clipToBounds()
                           .onPlaced {
                              parentCoords = it
                           }
                        ) {
                           Overlay(
                              overlay = {
                                 Canvas(modifier = Modifier) {
                                    if (points.isEmpty()) return@Canvas
                                    
                                    
                                    // First draw the disconnected horizontal lines underneath
                                    if (centerPoints.size > 1) {
                                       for (i in 0 until centerPoints.lastIndex) {
                                          val p1 = centerPoints[i]
                                          val p2 = centerPoints[i + 1]
                                          
                                          val totalLength = p2.x - p1.x
                                          val gapSize = totalLength * 0.15f
                                          val lineLength = (totalLength - gapSize) / 2
                                          
                                          // Draw first line segment
                                          val path1 = Path().apply {
                                             moveTo(p1.x+gapSize, p1.y)
                                             lineTo(p1.x + lineLength, p1.y)
                                          }
                                          drawPath(
                                             path = path1,
                                             color = disconnectedColor,
                                             style = Stroke(width = disconnectedLineWidth.toPx(), cap = StrokeCap.Round)
                                          )
                                          
                                          // Draw second line segment
                                          val path2 = Path().apply {
                                             moveTo(p1.x + lineLength + gapSize, p1.y)
                                             lineTo(p2.x-gapSize, p1.y)
                                          }
                                          drawPath(
                                             path = path2,
                                             color = disconnectedColor,
                                             style = Stroke(width = disconnectedLineWidth.toPx(), cap = StrokeCap.Round)
                                          )
                                       }
                                    }
                                    
                                    
                                    
                                    val connectionPath = Path()
                                    val _cubicControl = cubicControl
                                    
                                    
                                    // Start at first point
                                    /*
                                    if (startPointOffset != null) {
                                       startPointOffset?.apply {
                                          path.moveTo(this.x, this.y)
                                          path.cubicTo(
                                             x1 = lerp(this.x, points[0].x, _cubicControl.x1),
                                             y1 = lerp(this.y, points[0].y, _cubicControl.y1),
                                             x2 = lerp(this.x, points[0].x, _cubicControl.x2),
                                             y2 = lerp(this.y, points[0].y, _cubicControl.y2),
                                             x3 = points[0].x,
                                             y3 = points[0].y
                                          )
                                       }
                                    } else {
                                       path.moveTo(points[0].x, points[0].y)
                                    }
                                    
                                     */
                                    /**
                                     * Remove if re-enabling the above code
                                     * */
                                    connectionPath.moveTo(points[0].x, points[0].y)
                                    
                                    // Generate smooth curve through all points
                                    for(i in 0..<points.lastIndex) {
                                       connectionPath.cubicTo(
                                          x1 = lerp(points[i].x, points[i + 1].x, _cubicControl.x1),
                                          y1 = lerp(points[i].y, points[i + 1].y, _cubicControl.y1),
                                          x2 = lerp(points[i].x, points[i + 1].x, _cubicControl.x2),
                                          y2 = lerp(points[i].y, points[i + 1].y, _cubicControl.y2),
                                          x3 = points[i + 1].x,
                                          y3 = points[i + 1].y
                                       )
                                    }
                                    /*
                                    endPointOffset?.apply {
                                       path.cubicTo(
                                          x1 = lerp(points[points.lastIndex].x, this.x, _cubicControl.x1),
                                          y1 = lerp(points[points.lastIndex].y, this.y, _cubicControl.y1),
                                          x2 = lerp(points[points.lastIndex].x, this.x, _cubicControl.x2),
                                          y2 = lerp(points[points.lastIndex].y, this.y, _cubicControl.y2),
                                          x3 = this.x,
                                          y3 = this.y
                                       )
                                    }
                                    
                                     */
                                    
                                    
                                    drawPath(
                                       path = connectionPath,
                                       color = connectionColor,
                                       style = Stroke(
                                          width = lineWidth.toPx(),
                                          cap = StrokeCap.Round
                                       )
                                    
                                    )
                                    connectionPath.rewind()
                                    
                                    
                                    
                                 }
                              }
                           ) {
                              Row(
                                 horizontalArrangement = Arrangement.Center
                              ) {
                                 
                                 
                                 repeat(data.size) { index ->
                                    
                                    val state = rememberSliderState(
                                       data[index].second,
                                       valueRange = BAND_LOWER_BOUNDS..BAND_UPPER_BOUNDS
                                    )
                                    // set onValueChange for the STATE
                                    state.onValueChange = { newVal ->
                                       val roundedvalue =
                                          if (newVal < centerAnchorSnapRange && newVal > -centerAnchorSnapRange) {
                                             0f
                                          } else newVal
                                       state.value = roundedvalue  // Update the state
                                       equalizer.updateBand(index, roundedvalue)
                                    }
                                    
                                    // UI
                                    CenteredPriorityLayout(
                                       bottom = {
                                          Text(
                                             """
                                    ${
                                                data[index].first.let {
                                                   if (it > 999) {
                                                      String.format(
                                                         Locale.current.platformLocale,
                                                         "%1.0fk",
                                                         it / 1000
                                                      )
                                                   } else {
                                                      String.format(
                                                         Locale.current.platformLocale,
                                                         "%1.0f",
                                                         it
                                                      )
                                                   }
                                                }
                                             }
                                    hz
                                 """.trimIndent(),
                                             textAlign = TextAlign.Center
                                          )
                                       }
                                    ) {
                                       Column(
                                          horizontalAlignment = Alignment.CenterHorizontally,
                                          modifier = Modifier.width(35.dp)
                                       ) {
                                          // Value
                                          Text(
                                             state.value.roundToInt().toString(),
                                             textAlign = TextAlign.Center
                                          )
                                          
                                          VerticalSlider(
                                             enabled = equalizer.enabled,
                                             state = state,
                                             modifier = Modifier.height(sliderHeight),
                                             track = {
                                                Box(
                                                   Modifier
                                                      .clip(RoundedCornerShape(15.dp))
                                                      .width(4.dp)
                                                      .height(250.dp)
                                                      .background(MyColor.SeparatorGray)
                                                      .onGloballyPositioned {
                                                         centerPoints[index] =
                                                            parentCoords?.localPositionOf(it)
                                                               .let { off ->
                                                                  if (off != null) {
                                                                     Offset(
                                                                        off.x + it.size.width / 2,
                                                                        off.y + it.size.height / 2
                                                                     )
                                                                  } else {
                                                                     off
                                                                  }
                                                                  
                                                               }
                                                               ?: Offset.Zero
                                                      }
                                                ) {
                                                   Box(
                                                      modifier = Modifier
                                                         .align(Alignment.BottomCenter)
                                                         .fillMaxWidth()
                                                         .fillMaxHeight(
                                                            (it.value + (-it.valueRange.start)) /
                                                               (it.valueRange.totalLength())
                                                         )
                                                         .background(MyColor.ConfirmBlue)
                                                   )
                                                }
                                             },
                                             thumb = {
                                                Spacer(
                                                   Modifier
                                                      .size(20.dp)
                                                      .background(
                                                         connectionColor,
                                                         CircleShape
                                                      )
                                                      .onGloballyPositioned {
                                                         points[index] =
                                                            parentCoords?.localPositionOf(it)
                                                               .let { off ->
                                                                  if (off != null) {
                                                                     Offset(
                                                                        off.x + it.size.width / 2,
                                                                        off.y + it.size.height / 2
                                                                     )
                                                                  } else {
                                                                     off
                                                                  }
                                                                  
                                                               }
                                                               ?: Offset.Zero
                                                      }
                                                )
                                             },
                                             reverseDirection = true
                                          )
                                          
                                          
                                       }
                                    }
                                    
                                    
                                 }
                              }
                           }
                        }
                        
                     }
                  }
                  
                  
                  
               }
            }
         }
         
      }
   }
}