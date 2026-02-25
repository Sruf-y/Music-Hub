package ui.components

import SrufComposeComponents.withDropDown
import SrufComposeComponents.Experimental.ColorPicker
import SrufComposeComponents.Text
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.draggable2D
import androidx.compose.foundation.gestures.rememberDraggable2DState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import kotlin.math.roundToInt
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import com.aacomposetest.ui.theme.MyColor


@Composable
fun FabParent(modifier:Modifier = Modifier,fabContent:@Composable ()-> Unit = {},content: @Composable ()->Unit){
   var fabOffset by remember { mutableStateOf(IntOffset.Zero) }
   
   val view = LocalView.current
   
   var fabHalfSize by remember { mutableStateOf(IntSize.Zero) }
   
   val draggableState = rememberDraggable2DState(
      onDelta = { delta ->
         fabOffset = IntOffset(
            x = (fabOffset.x + delta.x).roundToInt().coerceIn(0, view.width - fabHalfSize.width),
            y = (fabOffset.y + delta.y).roundToInt().coerceIn(0, view.height - fabHalfSize.height)
         )
      }
   )
   
   // Render it all
   
   content.invoke()
   
   Box(
      modifier = modifier then Modifier
         .offset {
            fabOffset
         }
         .onSizeChanged {
            fabHalfSize = it
         }
         .draggable2D(draggableState)
   ) {
      fabContent.invoke()
   }
}


@Composable
internal fun _TestFabparent(){
   
   FabParent(
      fabContent = {
         Button(
            modifier = Modifier
               .size(100.dp)
               .clip(CircleShape)
               .background(
                  MyColor.Teal
               ),
           onClick = {}
         ) {
            Text("+")
         }
      }
   ) {
      Box(
         modifier = Modifier
            .fillMaxSize()
            .background(
               MyColor.Green
            ),
         contentAlignment = Alignment.Center
      ) {
         Text("Hello world!")
      }
   }
   
   
}


@Preview
@Composable
private fun _DropDownFab(){
   val dropDownState = remember { mutableStateOf(false) }
   
   val context = LocalContext.current
   
   var popup0State by remember { mutableStateOf(false) }
   
   val options = remember {
      listOf(
         "Hello world",
         "Welcome",
         "New York"
      ).mapIndexed { index,it->
         it to {
            if(index == 0){
               popup0State = true
            }
            else
               Toast.makeText(context, "Pressed ${index}",Toast.LENGTH_SHORT).show()
            
            dropDownState.value = false
         }
      }
   }
   
   FabParent(
      fabContent = {
         Box(modifier = Modifier
            .size(100.dp)
            .clip(CircleShape)
            .background(
               MyColor.Teal
            )
            .clickable(
               onClick = {
                  dropDownState.value = true
               }
            ),
            contentAlignment = Alignment.Center
         ){
            Text("+")
            
            withDropDown(
               state = dropDownState,
               actions = options
            )
         }
        
         
      }
   ) {
      Box(
         modifier = Modifier
            .fillMaxSize()
            .background(
               MyColor.Green
            ),
         contentAlignment = Alignment.Center
      ) {
         Text("Hello world!")
         
         if(popup0State) {
            Popup(
               alignment = Alignment.Center,
               onDismissRequest = {
                  popup0State  = false
               }
            ) {
               Box(
                  modifier = Modifier
                     .fillMaxSize(0.6f)
                     //.aspectRatio(1f)
                     .background(MyColor.Yellow),
                  contentAlignment = Alignment.Center
               ){
                  ColorPicker()
                  
                  BackHandler {
                     popup0State = false
                  }
               }
            }
         }
      }
   }
}



