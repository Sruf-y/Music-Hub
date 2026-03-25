package ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
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
            
            // if height is unbounded due to being in a Column, the component needs to be passed that unbounded height. Otherwise, use the maxHeight constrained.
            val contentPlaceable = content.measure(
               constraints.copy(
                  maxHeight = if(constraints.hasBoundedHeight) {
                     (constraints.maxHeight - bottomPlaceable.height).coerceAtLeast(
                        constraints.minHeight.coerceAtLeast(0)
                     )
                     
                  }else{
                     constraints.maxHeight
                  },
                  maxWidth = if(constraints.hasBoundedWidth) {
                     (constraints.maxWidth - bottomPlaceable.width).coerceAtLeast(
                        constraints.minWidth.coerceAtLeast(0)
                     )
                     
                  }else{
                     constraints.maxWidth
                  }
               )
            )
            
            val totalHeight = (bottomPlaceable.height+contentPlaceable.height).coerceAtMost(constraints.maxHeight)
            val totalWidth = maxOf(bottomPlaceable.width, contentPlaceable.width).coerceAtMost(constraints.maxWidth)
            
            // +1 pixel due to possible rounding error along the way
            layout(totalWidth, totalHeight+1){
               contentPlaceable.placeRelative(0,0)
               
               bottomPlaceable.placeRelative(0,contentPlaceable.height)
            }
         }
         // Layout with only one placeable detected -> assume it's the content placeable
         else{
            val contentPlaceable = content.measure(constraints)
            layout(contentPlaceable.width,contentPlaceable.height){
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



