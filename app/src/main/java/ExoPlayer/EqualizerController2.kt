package ExoPlayer

import android.media.audiofx.DynamicsProcessing
import android.view.RoundedCorner
import androidx.annotation.FloatRange
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import com.aacomposetest.Log
import com.aacomposetest.tryTo
import ui.components.ConnectedSliderGroup
import ui.components.rememberSliderGroupState
import kotlin.random.Random
import kotlin.random.nextInt
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastRoundToInt
import com.aacomposetest.ui.theme.MyColor
import ui.components.CenreredPriorityLayout
import ui.components.PriorityLayout
import kotlin.Float
import kotlin.Pair
import kotlin.math.roundToInt

/**
 * TODO: finish this
 * */
@OptIn(UnstableApi::class)
class EqualizerController2 constructor(sessionId: Int){
   /**
    * Equalizer affects only captured noise. This means there is no need to destroy it since it dies with the proccess.
    *
    * Folosesc doar preEq, o singura banda de compresie si un limitator pentru ca este mai simplu. Desi pot extinde pe viitor
    * */
   private val equalizer: DynamicsProcessing
   
   
   var enabled by mutableStateOf(false)
      private set
   
   fun enable(value:Boolean){
      equalizer.enabled = value
      enabled = equalizer.enabled
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
      
      enable(true)
      _initialize()
      functionToTryOutStuffAndDeleteLater()
   }
   
   
   var isDualChannel by mutableStateOf(equalizer.channelCount == 2)
      private set
   
   private fun _initialize(){
      // try to do a load from storage first, then leave the following code as is (hope it doesn't break by then)
      
      // Add the bands to the stateList
      if(equalizer.channelCount>0){
         val preEq = equalizer.getPreEqByChannelIndex(0)
         
         for(i in 0 until preEq.bandCount){
            val band = preEq.getBand(i)
            equalizer.setPreEqBandAllChannelsTo(i,band) // first make sure all channels are aligned
            bands.add(band.cutoffFrequency to band.gain)
         }
      }
   }
   
   private fun iLog(message:String){
      Log(message,"EQUALIZER2","i")
   }
   
   fun setGain(decibels: Float){
      if(decibels in BAND_LOWER_BOUNDS..BAND_UPPER_BOUNDS) {
         equalizer.setInputGainAllChannelsTo(decibels)
      }
   }
   fun updateBand(index:Int,decibels:Float){
      if(decibels in BAND_LOWER_BOUNDS..BAND_UPPER_BOUNDS) {
         val band = bands[index]
         equalizer.setPreEqBandAllChannelsTo(
            index,
            DynamicsProcessing.EqBand(true, band.first, decibels)
         )
         bands[index] = band.copy(second = decibels)
      }
   }
   fun updateMBC(){
   
   }
   fun updateLimiter(){
   
   }
   fun setGainLeft(decibels: Float) {
      if (isDualChannel) {
         if (decibels in BAND_LOWER_BOUNDS..BAND_UPPER_BOUNDS) {
            equalizer.setInputGainbyChannel(0,decibels)
         }
      }
   }
   
   fun setGainRight(decibels: Float){
      if(isDualChannel){
         if(decibels in BAND_LOWER_BOUNDS..BAND_UPPER_BOUNDS) {
            equalizer.setInputGainbyChannel(1,decibels)
         }
      }
   }
   
   
   
   
   /**
    * Delete later, does nothing, just to see what functions do what
    * */
   fun functionToTryOutStuffAndDeleteLater(): Unit{
      var channels = ArrayList<DynamicsProcessing.Channel>()
      for(i in 0 until equalizer.channelCount){
         val channel = equalizer.getChannelByChannelIndex(i)
         channels.add(channel)
      }
      iLog("Found ${channels.size} channels")
      
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
      internal fun BandsTestPreview(){
         
         val data by remember {
            derivedStateOf {
               myExoPlayer.equalizerController?.bands
            }
         }
         
         val groupState = rememberSliderGroupState(data?.map { it.second }?:emptyList(), orientation = Orientation.Vertical,reverseVerticalDirection = true)
         
         Box(Modifier
            .fillMaxSize()
            .safeDrawingPadding()){
            Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
               
               ConnectedSliderGroup(
                  state = groupState,
                  lineWidth = 3.dp,
                  lineColor = Color.Green,
                  thumb = { state, modifier ->
                     Spacer(
                        modifier
                           .size(20.dp)
                           .background(Color.Green, CircleShape)
                     )
                  },
                  modifier = Modifier
                     .wrapContentSize(),
                  lineStartValue = 0.5f,
                  lineEndValue = 0.5f,
                  spaceEventy = true,
                  trackEnforcedHeight = 300.dp,
                  decoration = {index,slider->
                     CenreredPriorityLayout(bottom = {
                        SrufComposeComponents.Text(
                           """
                              ${data?.get(index)?.first?.let {
                              if(it>1000){
                                 "${(it/1000).roundToInt()}k"
                              }else{
                                 it.fastRoundToInt()
                              }
                           }}
                              hz
                           """.trimIndent(),
                           color = MyColor.Black,
                           fontSize = 12.sp,
                           textAlign = TextAlign.Center
                        )
                     }) {
                        slider.invoke()
                     }
                  },
                  track = {
                     Box(Modifier.clip(RoundedCornerShape(15.dp)).width(5.dp).height(300.dp).background(MyColor.SeparatorGray)){
                        Box(modifier = Modifier.align(Alignment.BottomCenter)
                           .fillMaxWidth()
                           .fillMaxHeight(it.value/it.valueRange.endInclusive)
                           .background(MyColor.ConfirmBlue)
                        )
                     }
                  }
               )
               
               Button({
                  tryTo {
                     groupState.setValues {
                        data?.map { it.second }?:emptyList()
                     }
                  }
               }
               ) {
                  Text("Refresh")
               }
            }
         }
      }
   }
}