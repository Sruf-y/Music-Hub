package ViewModels.BottomSheet

import ViewModels.BottomSheet.BottomSheetInstance.Companion.BottomSheetFactory
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachReversed
import androidx.lifecycle.ViewModel
import com.aacomposetest.clickableNoRipple
import com.aacomposetest.exceptIndex
import com.aacomposetest.ui.theme.MyColor
import com.composables.core.SheetDetent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class SheetViewmodel(
   private val sheetFactory: BottomSheetFactory = BottomSheetFactory()
) : ViewModel(

) {
   internal var stack by mutableStateOf(emptyList<BottomSheetInstance>())
   private val actionMutex = Mutex()
   
   fun createAndAddSheet(
      initialDetent: SheetDetent = SheetDetent.Hidden,
      config: @Composable () ->  SheetConfig = {
         SheetConfig().apply { backgroundColor =  MyColor.Background() }
      },
      content: @Composable SimpleBottomSheetScope.() -> Unit,
   ) {
      val instance = sheetFactory.create(
         initialDetent = initialDetent,
         sheetConfig = config,
         content = content
      )
      stack = stack.plusElement(instance)
   }
   
   // ... rest of your ViewModel code with updated state references
   suspend fun dismissAll() {
      actionMutex.withLock {
         stack.fastForEachReversed {
            it.state.animateTo(SheetDetent.Hidden)
         }
         stack = emptyList()
      }
   }
   /**
    * Removes the last item in the stack.
    * */
   suspend fun dismissCurrent() {
      actionMutex.withLock {
         withContext(Dispatchers.Main) {
            if(stack.isNotEmpty()) {
               stack.last().state.animateTo(SheetDetent.Hidden)
               stack = stack.dropLast(1)
            }
         }
      }
   }
   
   suspend fun removeItemFromStack(item: BottomSheetInstance){
      actionMutex.withLock {
         if(stack.isNotEmpty()) {
            // synchronously animate and THEN drop the last item from the stack,
            // if there is one to be dropped
            stack.indexOf(item).let {
               if(it!=-1){
                  
                  stack[it].state.animateTo(SheetDetent.Hidden)
                  stack = stack.exceptIndex(it)
               }
            }
         }
      }
   }
}

@Composable
internal fun SheetViewmodel.renderAll() {
   var parentHeight by remember { mutableIntStateOf(0) }
   val scope = rememberCoroutineScope()
   
   Box(modifier = Modifier
      .fillMaxSize()
      .safeDrawingPadding()
      .onSizeChanged() {
         parentHeight = it.height
      }
   ){
      val density = LocalDensity.current
      
      // Clickable Invisible Overlay
      if(stack.isNotEmpty())
         Box(
            modifier = Modifier
               .fillMaxWidth()
               .fillMaxHeight()
               .clickableNoRipple(role = Role.Button){
                  if (stack.last().state.currentDetent == SheetDetent.FullyExpanded && stack.last().state.isIdle) {
                     scope.launch {
                        dismissCurrent()
                     }
                  }
               }
         )
      
      stack.forEachIndexed { index, it ->
         key("bottomSheetStack_#${index}") {
            var wasShown by remember { mutableStateOf(false) }
            
            // On Show - using stateHolder
            LaunchedEffect(Unit) {
               if (it.state.currentDetent == SheetDetent.Hidden && it.state.isIdle) {
                  it.state.animateTo(SheetDetent.FullyExpanded)
               }
            }
            
            // On Dismiss - using stateHolder
            LaunchedEffect(it.state.targetDetent, it.state.currentDetent) {
               if (it.state.currentDetent == SheetDetent.FullyExpanded && !wasShown) {
                  wasShown = true
               }
               
               if (it.state.targetDetent == SheetDetent.Hidden &&
                  it.state.progress == 1f &&
                  wasShown) {
                  removeItemFromStack(it)
               } else if (it.state.targetDetent == SheetDetent.Hidden &&
                  !wasShown &&
                  it.state.progress == 1f) {
                  removeItemFromStack(it)
               }
            }
            
            
            var intHeight by remember { mutableIntStateOf(0) }
            
            
            val config = it.sheetConfig()
            val sheetScope = remember { SimpleBottomSheetScope() }
            
            
            CustomBottomSheet(
               state = it.state,
               shape = config.shape,
               backgroundColor = config.backgroundColor,
               modifier = Modifier
                  .align(Alignment.BottomCenter)
                  .onSizeChanged {
                     intHeight = it.height
                  }
                  then it.sheetModifier,
               contentPadding = config.contentPadding
            ) {
               it.content.invoke(sheetScope)
            }
         }
      }
   }
}

class SimpleBottomSheetScope {
   // Implement any necessary methods/properties
   // Since you're not using any specific BottomSheetScope features,
   // this can be empty or contain minimal implementation
}

@Composable
private fun CustomBottomSheet(
   state: BottomSheetState,
   shape: Shape,
   backgroundColor: Color,
   modifier: Modifier = Modifier,
   contentPadding: PaddingValues = PaddingValues(0.dp),
   content: @Composable () -> Unit
) {
   val progress = state.progress
   val density = LocalDensity.current
   val isExpanded = state.currentDetent == SheetDetent.FullyExpanded
   
   
   Box(
      modifier = Modifier
         .clickableNoRipple{} // To prevent clicking through the sheet
         then modifier
         .wrapContentSize()
         // TODO: add dragging AND with dragging add the settling and auto-dispose functionalities. These should really be in the state i think
         .background(backgroundColor, shape)
         .clip(shape)
         .padding(contentPadding),
      content = {
         content.invoke()
      }
   )
}

/**
 * Composable holding the stack of sheets.
 * */
@Composable
fun MyModalBottomSheet2(
   modifier:Modifier = Modifier,
   viewModel: SheetViewmodel,
   backgroundTint:Color = MyColor.SemiTransparent()
){
   val scope = rememberCoroutineScope()
   
   BackHandler(
      enabled = viewModel.stack.isNotEmpty()
   ) {
      scope.launch {
         viewModel.dismissCurrent()
      }
   }
   
   val backgroundAlpha by remember {
      derivedStateOf {
         if (viewModel.stack.isNotEmpty()) {
            val firstSheet = viewModel.stack[0]
            when {
               (firstSheet.state.targetDetent == SheetDetent.FullyExpanded) -> firstSheet.state.progress // fade in
               else -> firstSheet.state.progress // fade out
            }
         } else {
            0f
         }
      }
   }
   
   
   Box(modifier = modifier) {
      // Render the overlay
      if (viewModel.stack.isNotEmpty()) {
         BlackBackground(
            alpha = backgroundAlpha,
            color = backgroundTint
         )
      }
      // Render the sheets
      viewModel.renderAll()
   }
   
}

@Composable
internal fun BlackBackground(
   alpha:Float,
   color:Color = MyColor.SemiTransparent()
) =
   Box(modifier = Modifier
      .fillMaxSize()
      .alpha(alpha)
      .background(color)
      .clickable(
         interactionSource = remember { MutableInteractionSource() },
         indication = null
      ) {
         // NO-OP
      }
   )