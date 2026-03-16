package ui.components

import android.text.Layout
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import kotlin.math.max

/**
 * Box that prioritize content in the middle being on the screen but first it prioritizes the bottom content
 *
 * Priority is:  bottom -> content
 * */
@Composable
fun PriorityBox(
   modifier:Modifier = Modifier,
   bottom: @Composable ()-> Unit = {},
   content: @Composable ()-> Unit
){
   Layout(
      modifier = modifier,
      content = {
         content.invoke()
         bottom.invoke()
      }
   ) { measurables, constraints ->
      
      if(measurables.isNotEmpty()) {
         val content = measurables[0]
         
         if (measurables.size > 1) {
            val bottom = measurables[1]
            
            
            val bottomPlaceable = bottom.measure(constraints)
            
            val contentPlaceable = content.measure(
               constraints.copy(
                  maxHeight = (constraints.maxHeight - bottomPlaceable.height).coerceAtLeast(0)
               )
            )
            
            val totalHeight = (bottomPlaceable.height+contentPlaceable.height).coerceAtMost(constraints.maxHeight)
            
            // +1 due to possible rounding error along the way
            layout(constraints.maxWidth, totalHeight+1){
               contentPlaceable.placeRelative(0,0)
               
               bottomPlaceable.placeRelative(0,contentPlaceable.height)
            }
         }
         // Layout with only one placeable detected -> assume it's the content placeable
         else{
            val contentPlaceable = content.measure(constraints)
            layout(constraints.maxWidth,contentPlaceable.height){
               contentPlaceable.placeRelative(0,0)
            }
         }
         
         
         
      }
      // Empty layout
      else{
         layout(constraints.maxWidth,constraints.maxHeight) {  }
      }
      
      
      
      
      
   }
}



