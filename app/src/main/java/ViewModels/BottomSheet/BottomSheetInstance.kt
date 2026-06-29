package ViewModels.BottomSheet

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.composables.core.SheetDetent

class SheetConfig() {
   var backgroundColor by mutableStateOf(Color.Unspecified)
   var contentPadding by mutableStateOf(PaddingValues(0.dp))
   var shape by mutableStateOf<Shape>(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
}

data class BottomSheetInstance(
   val state: BottomSheetState = BottomSheetState(),
   val sheetConfig: @Composable () -> SheetConfig = { SheetConfig()},
   val sheetModifier: Modifier = Modifier,
   val content: @Composable SimpleBottomSheetScope.() -> Unit,
) {
   
   private var cleanup: (() -> Unit)? = null
   
   /**
    * Called when the instance is being disposed of. Should remove the instance from the stack.
    * */
   private fun setOnDispose(operation:(() -> Unit)?){
      cleanup = operation
   }
   
   fun dispose() {
      cleanup?.invoke()
   }
   
   companion object{
      class BottomSheetFactory() {
         private val sheetStates = mutableMapOf<BottomSheetInstance, String>()
         
         fun create(
            initialDetent: SheetDetent = SheetDetent.Hidden,
            sheetConfig: @Composable () -> SheetConfig = {
               SheetConfig()
            },
            content: @Composable SimpleBottomSheetScope.() -> Unit
         ): BottomSheetInstance {
            val stateHolder = BottomSheetState(initialDetent)
            
            val instance = BottomSheetInstance(
               state = stateHolder,
               sheetConfig = sheetConfig,
               content = content
            )
            
            // Store initial state for restoration
            sheetStates[instance] = initialDetent.identifier
            
            // Store cleanup
            instance.setOnDispose{
               //lifecycleOwner.lifecycle.removeObserver(observer)
               sheetStates.remove(instance)
            }
            
            return instance
         }
      }
   }
}


