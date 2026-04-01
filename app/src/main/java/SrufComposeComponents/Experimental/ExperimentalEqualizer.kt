package SrufComposeComponents.Experimental

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.gestures.DragScope
import androidx.compose.foundation.gestures.DraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberSliderState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.aacomposetest.ui.theme.MyColor
import com.aacomposetest.ui.theme.Theme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExperimentalEqualizer(modifier: Modifier = Modifier.fillMaxSize()) {
   
   
   Box(modifier = modifier){
      Theme.RenderDefaultBackground()
      
      Box(modifier = Modifier
         .fillMaxWidth()
         .fillMaxHeight(0.5f)){
         val band1 = rememberSliderState().apply {
            this.onValueChange = {
               this.value = it
            }
            
         }
      }
      
   }
}

class VerticalSliderState(
   initialValue: Float,
   private val valueRange: ClosedFloatingPointRange<Float>,
   private val onValueChange: (Float) -> Unit,
   private val onFinishChanges: (Float) -> Unit,
): DraggableState {
   var value by mutableFloatStateOf(
      initialValue.coerceIn(valueRange.start,valueRange.endInclusive)
   )
      private set
   
   var valueAsFactor by mutableFloatStateOf(
      initialValue.coerceIn(valueRange.start,valueRange.endInclusive)/valueRange.endInclusive
   )
   /**
    * Internal do not call;
    * */
   internal fun onValueChange(value:Float){
      this.value = value
      this.valueAsFactor = value/valueRange.endInclusive
      this.onValueChange.invoke(value)
   }
   
   
   /**
    * Internal do not call;
    * */
   internal fun onValueChangeFinished(value:Float){
      this.value = value
      this.valueAsFactor = value/valueRange.endInclusive
      this.onValueChange.invoke(value)
      
      this.onFinishChanges.invoke(value)
   }
   
   override suspend fun drag(
      dragPriority: MutatePriority,
      block: suspend DragScope.() -> Unit,
   ) {
   todo tomorrow
   }
   
   override fun dispatchRawDelta(delta: Float) {
      todo tomorrow
   }
   
}

@Composable
fun rememberVerticalSliderState(initialValue:Float = 0f, valueRange: ClosedFloatingPointRange<Float> = 0f..1f, onValueChange:(Float)->Unit = {}, onFinishChanges:(Float)->Unit = {}):VerticalSliderState{
   return remember { VerticalSliderState(initialValue,valueRange,onValueChange,onFinishChanges) }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerticalSlider(state: VerticalSliderState,modifier:Modifier = Modifier,trackColor: Color = Color.Blue,topCornerRadius: Dp = 2.dp,bottomCornerRadius: Dp = 2.dp){
   
   Canvas(modifier) {
      
      drawTrackPath(
         path = Path(),
         offset = Offset(0f,0f),
         size = size,
         color = MyColor.Red,
         topCornerRadius = topCornerRadius.toPx(),
         bottomCornerRadius = bottomCornerRadius.toPx()
      )
      
   drawTrackPath(
      path = Path(),
      offset = Offset(0f,size.height-state.valueAsFactor*size.height),
      size = Size(size.width,state.valueAsFactor*size.height),
      color = trackColor.copy(alpha = 0.5f),
      topCornerRadius = topCornerRadius.toPx(),
      bottomCornerRadius = bottomCornerRadius.toPx()
   )
   
   }
   
}

@Preview
@Composable
private fun Preview(){
   
   val state = rememberVerticalSliderState(0.6f)
   Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
      VerticalSlider(state,modifier = Modifier.width(200.dp).height(400.dp))
   }
   
}

/**
 * !! Copiat din Jetpack Compose project
 *
 * Editat de mine
 * */
fun DrawScope.drawTrackPath(
   path: Path,
   offset: Offset,
   size: Size,
   color: Color,
   topCornerRadius: Float,
   bottomCornerRadius: Float,
) {
   val startCorner = CornerRadius(topCornerRadius, topCornerRadius)
   val endCorner = CornerRadius(bottomCornerRadius, bottomCornerRadius)
   val track =
      RoundRect(
            rect = Rect(offset, size = Size(size.width, size.height)),
            topLeft = startCorner,
            topRight = startCorner,
            bottomRight = endCorner,
            bottomLeft = endCorner,
         )
      
   path.addRoundRect(track)
   drawPath(path, color)
   path.rewind()
}