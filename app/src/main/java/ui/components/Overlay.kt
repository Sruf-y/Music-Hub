package ui.components

import SrufComposeComponents.Text
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.RadioButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.dp
import com.aacomposetest.of


private val defaultOverlay: @Composable () -> Unit = {
   Box(modifier = Modifier
      .fillMaxSize()
      .clickable(
         onClick = {
         
         }
      )
   )
}

private val defaultClickableOverlay: @Composable (Modifier) -> Unit = {modifier->
   Box(modifier = Modifier
      .fillMaxSize()
      then modifier
   )
}
@Deprecated("Uses static modifier", replaceWith = ReplaceWith("Overlay"))
@Composable
fun Overlay(
   overlay: @Composable ()->Unit = defaultOverlay,
   content: @Composable ()->Unit
){
   SubcomposeLayout(modifier = Modifier.fillMaxWidth().wrapContentHeight()) {constraints->
      
      val primaryPlaceable = subcompose("primary") {
         content.invoke()
      }.first().measure(constraints)
      
      val secondaryPlaceable = subcompose("overlay") {
         overlay.invoke()
      }.first().measure(
         constraints.of(primaryPlaceable)
      )
      
      
      layout(primaryPlaceable.width,primaryPlaceable.height) {
         primaryPlaceable.place(0,0,)
         secondaryPlaceable.place(0,0,1f)
      }
   }
}
@Composable
fun Overlay(
   modifier: Modifier = Modifier,
   overlay: @Composable ()->Unit = defaultOverlay,
   content: @Composable ()->Unit
){
   SubcomposeLayout(modifier = modifier) {constraints->
      
      val primaryPlaceable = subcompose("primary") {
         content.invoke()
      }.first().measure(constraints)
      
      val secondaryPlaceable = subcompose("overlay") {
         overlay.invoke()
      }.first().measure(
         constraints.of(primaryPlaceable)
      )
      
      
      layout(primaryPlaceable.width,primaryPlaceable.height) {
         primaryPlaceable.place(0,0,)
         secondaryPlaceable.place(0,0,1f)
      }
   }
}


@Composable
fun Overlay(
   onClickModifier:Modifier = Modifier,
   content: @Composable ()->Unit
){
   Overlay(
      overlay = { defaultClickableOverlay(onClickModifier) },
      content = content
   )
}

