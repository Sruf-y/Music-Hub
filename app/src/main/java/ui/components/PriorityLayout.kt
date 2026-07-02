package ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import SrufComposeComponents.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp

/**
 * Layout that prioritizes the [bottom] content over the main [content].
 *
 * This is useful for making sure compound components of bottom-up layout design will not overflow.
 *
 * @sample Sample_PriorityLayout
 * @sample Sample_PriorityLayout2
 * */
@Composable
fun PriorityLayout(
   modifier: Modifier = Modifier,
   bottom: @Composable () -> Unit = {},
   content: @Composable () -> Unit
) {
   Layout(
      modifier = modifier,
      content = {
         content.invoke()
         bottom.invoke()
      }
   ) { measurables, constraints ->
      
      if (measurables.isNotEmpty()) {
         val contentMeasurable = measurables[0]
         
         if (measurables.size > 1) {
            val bottomMeasurable = measurables[1]
            
            // Measure bottom with full constraints
            val bottomPlaceable = bottomMeasurable.measure(constraints)
            
            // Calculate remaining space for content
            val contentConstraints = Constraints(
               minWidth = constraints.minWidth.coerceAtLeast(0),
               maxWidth = if (constraints.hasBoundedWidth) {
                  constraints.maxWidth.coerceAtLeast(constraints.minWidth)
               } else {
                  Constraints.Infinity
               },
               minHeight = constraints.minHeight.coerceAtLeast(0),
               maxHeight = if (constraints.hasBoundedHeight) {
                  (constraints.maxHeight - bottomPlaceable.height)
                     .coerceAtLeast(constraints.minHeight)
               } else {
                  Constraints.Infinity
               }
            )
            
            // Measure content with remaining space
            val contentPlaceable = contentMeasurable.measure(contentConstraints)
            
            // Calculate total dimensions respecting max constraints
            val totalWidth = maxOf(bottomPlaceable.width, contentPlaceable.width)
               .coerceIn(constraints.minWidth, constraints.maxWidth)
            
            val totalHeight = (bottomPlaceable.height + contentPlaceable.height)
               .coerceIn(constraints.minHeight, constraints.maxHeight)
            
            layout(totalWidth, totalHeight) {
               // Place content at top
               contentPlaceable.placeRelative(0, 0)
               
               // Place bottom after content, but ensure it stays within bounds
               val bottomY = contentPlaceable.height.coerceAtMost(totalHeight)
               bottomPlaceable.placeRelative(0, bottomY)
            }
         } else {
            // Only content exists - respect max constraints fully
            val contentPlaceable = contentMeasurable.measure(constraints)
            
            val width = contentPlaceable.width.coerceIn(constraints.minWidth, constraints.maxWidth)
            val height = contentPlaceable.height.coerceIn(constraints.minHeight, constraints.maxHeight)
            
            layout(width, height) {
               contentPlaceable.placeRelative(0, 0)
            }
         }
      } else {
         // Empty layout - respect constraints
         val width = if (constraints.hasBoundedWidth) {
            constraints.maxWidth.coerceAtLeast(constraints.minWidth)
         } else {
            constraints.minWidth
         }
         
         val height = if (constraints.hasBoundedHeight) {
            constraints.maxHeight.coerceAtLeast(constraints.minHeight)
         } else {
            constraints.minHeight
         }
         
         layout(width, height) {}
      }
   }
}

@Composable
fun CenteredPriorityLayout(
   modifier: Modifier = Modifier,
   bottom: @Composable () -> Unit = {},
   content: @Composable () -> Unit
) {
   Layout(
      modifier = modifier,
      content = {
         content.invoke()
         bottom.invoke()
      }
   ) { measurables, constraints ->
      
      if (measurables.isNotEmpty()) {
         val contentMeasurable = measurables[0]
         
         if (measurables.size > 1) {
            val bottomMeasurable = measurables[1]
            
            // Measure both with natural sizes
            val contentPlaceable = contentMeasurable.measure(
               Constraints(
                  minWidth = 0,
                  maxWidth = constraints.maxWidth,
                  minHeight = 0,
                  maxHeight = Constraints.Infinity
               )
            )
            
            val bottomPlaceable = bottomMeasurable.measure(
               Constraints(
                  minWidth = 0,
                  maxWidth = constraints.maxWidth,
                  minHeight = 0,
                  maxHeight = Constraints.Infinity
               )
            )
            
            // Calculate total dimensions
            val totalWidth = maxOf(contentPlaceable.width, bottomPlaceable.width)
               .coerceIn(constraints.minWidth, constraints.maxWidth)
            
            val totalHeight = (contentPlaceable.height + bottomPlaceable.height)
               .coerceIn(constraints.minHeight, constraints.maxHeight)
            
            layout(totalWidth, totalHeight) {
               // Center content horizontally
               val contentX = (totalWidth - contentPlaceable.width) / 2
               contentPlaceable.placeRelative(contentX, 0)
               
               // Center bottom horizontally
               val bottomX = (totalWidth - bottomPlaceable.width) / 2
               bottomPlaceable.placeRelative(bottomX, contentPlaceable.height)
            }
         } else {
            // Only content exists - center it horizontally
            val contentPlaceable = contentMeasurable.measure(constraints)
            
            val width = contentPlaceable.width.coerceIn(constraints.minWidth, constraints.maxWidth)
            val height = contentPlaceable.height.coerceIn(constraints.minHeight, constraints.maxHeight)
            
            layout(width, height) {
               val x = (width - contentPlaceable.width) / 2
               contentPlaceable.placeRelative(x, 0)
            }
         }
      } else {
         // Empty layout
         layout(constraints.minWidth, constraints.minHeight) {}
      }
   }
}

@Composable
internal fun Sample_PriorityLayout(){
   
   Box(
      modifier = Modifier
         .systemBarsPadding()
         .fillMaxSize()
         .background(Color.LightGray)
         .safeDrawingPadding(),
      contentAlignment = Alignment.BottomCenter
   ){
      var text by remember { mutableStateOf("") }
      
      PriorityLayout(
         bottom = {
            
            if(text.isNotEmpty()){
               Text("Text is not empty",color = Color.Red)
            }
            
         }
      ) {
         
         BasicTextField(
            value = text, onValueChange = { text = it },
            modifier = Modifier
               .background(Color.Gray)
               .width(200.dp)
         )
      }
   }
}



