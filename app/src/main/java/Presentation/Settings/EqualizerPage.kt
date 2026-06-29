package Presentation.Settings

import ExoPlayer.EqualizerController
import ExoPlayer.EqualizerController.Companion.BAND_LOWER_BOUNDS
import ExoPlayer.EqualizerController.Companion.BAND_UPPER_BOUNDS
import ExoPlayer.myExoPlayer
import SrufComposeComponents.Text
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import com.aacomposetest.clickableNoRipple
import com.aacomposetest.totalLength
import com.aacomposetest.ui.theme.MyColor
import com.aacomposetest.ui.theme.Theme
import ui.components.CenteredPriorityLayout
import ui.components.CubicControl
import ui.components.Overlay
import ui.components.VerticalSlider
import ui.components.rememberSliderState
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EqualizerPage(modifier: Modifier = Modifier.fillMaxSize(),equalizer: EqualizerController) {
   // Background
   Theme.RenderDefaultBackground()
   
   val data by remember {
      derivedStateOf {
         myExoPlayer.equalizerController?.bands
      }
   }
   
   Box(
      modifier = Modifier
         .fillMaxSize()
         .safeDrawingPadding(),
      contentAlignment = Alignment.Center,
   ) {
      Column() {
         
         
         // Enable switch
         Overlay(
            modifier = Modifier.fillMaxWidth(),
            overlay = {
               Box(
                  Modifier.clickableNoRipple {
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
                  androidx.compose.material3.Text("Enable")
                  Switch(
                     equalizer.enabled,
                     {
                     
                     }
                  )
               }
            }
            
         }
         
         // Spacer box
         Box(Modifier
            .fillMaxWidth()
            .height(10.dp))
         
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
               
               val cubicControl: CubicControl = remember { CubicControl(0.3f, 0.1f, 0.7f, 0.9f) }
               
               
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
               
               Card(
                  modifier = Modifier
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
                              for(i in 0 until centerPoints.lastIndex) {
                                 val p1 = centerPoints[i]
                                 val p2 = centerPoints[i + 1]
                                 
                                 val totalLength = p2.x - p1.x
                                 val gapSize = totalLength * 0.15f
                                 val lineLength = (totalLength - gapSize) / 2
                                 
                                 // Draw first line segment
                                 val path1 = Path().apply {
                                    moveTo(p1.x + gapSize, p1.y)
                                    lineTo(p1.x + lineLength, p1.y)
                                 }
                                 drawPath(
                                    path = path1,
                                    color = disconnectedColor,
                                    style = Stroke(
                                       width = disconnectedLineWidth.toPx(),
                                       cap = StrokeCap.Round
                                    )
                                 )
                                 
                                 // Draw second line segment
                                 val path2 = Path().apply {
                                    moveTo(p1.x + lineLength + gapSize, p1.y)
                                    lineTo(p2.x - gapSize, p1.y)
                                 }
                                 drawPath(
                                    path = path2,
                                    color = disconnectedColor,
                                    style = Stroke(
                                       width = disconnectedLineWidth.toPx(),
                                       cap = StrokeCap.Round
                                    )
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
                           LaunchedEffect(null) {
                              state.onValueChange = { newVal ->
                                 val roundedvalue =
                                    if (newVal < centerAnchorSnapRange && newVal > -centerAnchorSnapRange) {
                                       0f
                                    } else newVal
                                 state.value = roundedvalue  // Update the state
                                 equalizer.updateBand(index, roundedvalue)
                              }
                           }
                           
                           val fontSize = remember { 14.sp }
                           
                           // UI
                           CenteredPriorityLayout(
                              bottom = {
                                 // Display Hertz
                                 Text(
                                    data[index].first.let {
                                       String.format(
                                          Locale.current.platformLocale,
                                          "%1.0f${if (it > 999) "K" else ""}",
                                          if (it > 999) it / 1000 else it
                                       )
                                    }
                                    ,
                                    textAlign = TextAlign.Center,
                                    fontSize = fontSize
                                 )
                              }
                           ) {
                              Column(
                                 horizontalAlignment = Alignment.CenterHorizontally,
                                 modifier = Modifier.width(35.dp)
                              ) {
                                 // Display Value of band
                                 Text(
                                    String.format(
                                       Locale.current.platformLocale,
                                       "%1.1f",
                                       state.value
                                    ),
                                    textAlign = TextAlign.Center,
                                    fontSize = fontSize
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