package MyViewModels.BottomSheet

import MyViewModels.SheetConfig
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.composables.core.BottomSheetScope
import com.composables.core.SheetDetent

data class BottomSheetInstance(
   val state: BottomSheetStateHolder = BottomSheetStateHolder(),
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
      class BottomSheetFactory(
         //private val lifecycleOwner: LifecycleOwner
      )
      {
         private val sheetStates = mutableMapOf<BottomSheetInstance, String>()
         
         fun create(
            initialDetent: SheetDetent = SheetDetent.Hidden,
            sheetConfig: @Composable () -> SheetConfig = {
               SheetConfig()
            },
            content: @Composable SimpleBottomSheetScope.() -> Unit
         ): BottomSheetInstance {
            val stateHolder = BottomSheetStateHolder(initialDetent)
            
            val instance = BottomSheetInstance(
               state = stateHolder,
               sheetConfig = sheetConfig,
               content = content
            )
            
            // Store initial state for restoration
            sheetStates[instance] = initialDetent.identifier
            /*
            {// Setup lifecycle observer
               val observer = object : DefaultLifecycleObserver {
                  override fun onPause(owner: LifecycleOwner) {
                     sheetStates[instance] = stateHolder.currentDetent.identifier
                  }
                  
                  override fun onResume(owner: LifecycleOwner) {
                     val savedDetent = sheetStates[instance] ?: "hidden"
                     val targetDetent = when (savedDetent) {
                        "hidden" -> SheetDetent.Hidden
                        "fully_expanded" -> SheetDetent.FullyExpanded
                        else -> SheetDetent.Hidden
                     }
                     
                     if (stateHolder.currentDetent != targetDetent) {
                        stateHolder.jumpTo(targetDetent)
                     }
                  }
               }
               
               lifecycleOwner.lifecycle.addObserver(observer)
            }
            
             */
            
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


