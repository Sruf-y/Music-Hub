package MyViewModels


import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachReversed
import androidx.lifecycle.ViewModel
import com.aacomposetest.exceptIndex
import com.aacomposetest.ui.theme.MyColor
import com.composables.core.BottomSheet
import com.composables.core.BottomSheetScope
import com.composables.core.BottomSheetState
import com.composables.core.SheetDetent
import com.composables.core.rememberBottomSheetState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.apply


class SheetViewmodel(): ViewModel(){
   
   internal var stack by mutableStateOf(emptyList<BottomSheetInstance>())
   
   private val actionMutex = Mutex()
   
   fun addSheet(content: BottomSheetInstance){
      stack = stack.plusElement(content)
   }
   
   /**
    * Clears the stack.
    * */
   suspend fun dismissAll(){
      actionMutex.withLock {
         stack.fastForEachReversed {
            it.sheetState.animateTo(SheetDetent.Hidden)
            
         }
         stack = emptyList()
      }
   }
   
   /**
    * Removes the last item in the stack.
    * */
   suspend fun dismissCurrent(){
      actionMutex.withLock {
         withContext(Dispatchers.Main) {
            if(stack.isNotEmpty()) {
               // synchronously animate and THEN drop the last item from the stack,
               // if there is one to be dropped
               
               stack.last().sheetState.animateTo(SheetDetent.Hidden)
               stack = stack.dropLast(1)
            }
         }
         
      }
   }
   // TODO: replace the entire SheetState class with a copied and modifier version of my own from AOSP
   //  It does work for now but i'd REALLY like to get this done sometime
   private suspend fun removeItemFromStack(item: BottomSheetInstance){
      actionMutex.withLock {
         if(stack.isNotEmpty()) {
            // synchronously animate and THEN drop the last item from the stack,
            // if there is one to be dropped
            stack.indexOf(item).let {
               if(it!=-1){
                  stack[it].sheetState.animateTo(SheetDetent.Hidden)
                  stack = stack.exceptIndex(it)
               }
            }
         }
      }
   }
   
   
   @Composable
   internal fun renderAll(){
      
      var parentHeight by remember { mutableIntStateOf(0) }
      
      val scope = rememberCoroutineScope()
      
      Box(modifier = Modifier
         .fillMaxSize()
         .systemBarsPadding()
         .statusBarsPadding()
         .displayCutoutPadding()
         .navigationBarsPadding()
         .imePadding()
         .onSizeChanged() {
            parentHeight = it.height
         }
      ){
         stack.forEachIndexed { index, it ->
            key("bottomSheetStack_#${index}") {
               
               // This is being used because initially the sheet is hidden and progress is 0, making it remove itself before being fully shown.
               var wasShown by remember { mutableStateOf(false) }
               
               // On Show
               LaunchedEffect(Unit) {
                  if (it.sheetState.currentDetent == SheetDetent.Hidden && it.sheetState.isIdle) {
                     it.sheetState.animateTo(SheetDetent.FullyExpanded)
                  }
               }
               
               // On Dismiss
               LaunchedEffect(it.sheetState.targetDetent, it.sheetState.currentDetent) {
                  
                  if (it.sheetState.currentDetent == SheetDetent.FullyExpanded && wasShown == false) {
                     // buffer for wasShown, in order to not automatically dismiss itself when this launched effect runs for the first time
                     wasShown = true
                  }
                  
                  if (it.sheetState.targetDetent == SheetDetent.Hidden && it.sheetState.progress == 1f && wasShown) {
                     // if it was fully shown AND it has been hidden, but this will brick the logic if the user gets a hold of
                     // the sheet before it reached full height
                     removeItemFromStack(it)
                  }else if(it.sheetState.targetDetent == SheetDetent.Hidden && wasShown == false && it.sheetState.progress == 1f){
                     // Handles the issue introduced by the above IF statement by checking if the sheet is heading towards HIDDEN without ever being fully shown or fully expanded (same thing)
                     removeItemFromStack(it)
                  }
                  
                  
                  
               }
               
               var intHeight by remember { mutableIntStateOf(0) }
               val density = LocalDensity.current
               
               // Clickable Invisible Overlay
               if(!(it.sheetState.targetDetent == SheetDetent.Hidden && it.sheetState.progress >=0.5f))
               Box(
                  modifier = Modifier
                     .fillMaxWidth()
                     .height(with(density) { (parentHeight - intHeight).toDp() })
                     .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                     ) {
                        
                        if (it.sheetState.currentDetent == SheetDetent.FullyExpanded && it.sheetState.isIdle) {
                           
                           scope.launch {
                              dismissCurrent()
                           }
                           
                        }
                     }
               )
               
               val config = it.sheetConfig()
               BottomSheet(
                  it.sheetState,
                  shape = config.shape,
                  backgroundColor = config.backgroundColor,
                  modifier = Modifier
                     .wrapContentSize()
                     .onSizeChanged {
                        intHeight = it.height
                     } then it.sheetModifier,
                  contentPadding = config.contentPadding
               ) {
                  it.content.invoke(this)
               }
            }
         }
         }
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

/**
 * Composable holding the stack of sheets.
 * */
@Composable
fun MyModalBottomSheet(
   modifier:Modifier = Modifier,
   state: SheetViewmodel,
   backgroundTint:Color = MyColor.SemiTransparent()
){
   val scope = rememberCoroutineScope()
   
   BackHandler(
      enabled = state.stack.size>0
   ) {
      scope.launch {
         state.dismissCurrent()
      }
   }
   
   val backgroundAlpha by remember {
      derivedStateOf {
         if (state.stack.size>0) {
            when {
               (state.stack[0].sheetState.targetDetent == SheetDetent.FullyExpanded) -> state.stack[0].sheetState.progress // fade in
               else -> 1f - state.stack[0].sheetState.progress // fade out
            }
         } else {
            0f
         }
      }
   }
   
   
   
   
   Box(modifier = modifier){
      
      // Render the overlay
      if(state.stack.size>0) {
         BlackBackground(
            alpha = backgroundAlpha,
            color = backgroundTint
         )
      }
      
      // Render the sheets
      state.renderAll()
      
   }
   
}

class SheetConfig() {
   var backgroundColor by mutableStateOf(Color.Unspecified)
   var contentPadding by mutableStateOf(PaddingValues(0.dp))
   var shape by mutableStateOf<Shape>(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
}

/**
 * An instance of a visible bottom sheet.
 * */
@OptIn(ExperimentalMaterial3Api::class)
data class BottomSheetInstance(
   var sheetState: BottomSheetState,
   val sheetConfig:@Composable ()-> SheetConfig,
   val sheetModifier:Modifier = Modifier.shadow(5.dp),
   val content: @Composable BottomSheetScope.() -> Unit,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun rememberBottomSheetInstance(
   initialDetent: SheetDetent = SheetDetent.Hidden,
   state: BottomSheetState = rememberBottomSheetState(SheetDetent.Hidden),
   sheetConfig: @Composable ()-> SheetConfig = {
      SheetConfig().apply{
         this.backgroundColor = MyColor.Background()
      }
   },
   content: @Composable BottomSheetScope.() -> Unit,
): BottomSheetInstance {
   
   // Save only the current detent
   var savedDetent by rememberSaveable {
      mutableStateOf(initialDetent.identifier)
   }
   
   // Store the savedDetent before rotation
   DisposableEffect(state) {
      onDispose {
         savedDetent = state.currentDetent.identifier
      }
   }
   
   // Handle restoration after rotation
   LaunchedEffect(Unit) {
      
      val targetDetent = when (savedDetent) {
         "hidden" -> SheetDetent.Hidden
         "fully_expanded" -> SheetDetent.FullyExpanded
         else -> SheetDetent.Hidden // To trigger the onShow mechanism in renderAll
      }
      
      // Only jump if they're different
      if (state.currentDetent != targetDetent) {
         
         state.jumpTo(targetDetent)
      }
   }
   
   return remember() {
      BottomSheetInstance(
         content = content,
         sheetState = state,
         sheetConfig = sheetConfig
      )
   }
}




