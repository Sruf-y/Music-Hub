package ui.components


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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachReversed
import androidx.lifecycle.ViewModel
import com.aacomposetest.ui.theme.MyColor
import com.composables.core.BottomSheet
import com.composables.core.BottomSheetScope
import com.composables.core.BottomSheetState
import com.composables.core.SheetDetent
import com.composables.core.rememberBottomSheetState
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock


class SheetViewmodel(): ViewModel(){
   
   internal val stack = mutableStateListOf<BottomSheetInstance>()
   
   private val actionMutex = Mutex()
   
   fun addSheet(content: BottomSheetInstance){
      stack.add(content)
   }
   
   suspend fun dismissAll(){
      actionMutex.withLock {
         stack.fastForEachReversed {
            it.sheetState.animateTo(SheetDetent.Hidden)
            
         }
         stack.clear()
      }
      
   }
   
   suspend fun dismissCurrent(){
      actionMutex.withLock {
         if(stack.isNotEmpty()) {
            stack.last().sheetState.animateTo(SheetDetent.Hidden)
            stack.removeLastOrNull()
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
         .onGloballyPositioned {
            parentHeight = it.size.height
         }
      ){
         stack.forEachIndexed { index, it ->
            
            
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
                  wasShown = true
               }
               
               if (it.sheetState.targetDetent == SheetDetent.Hidden && it.sheetState.progress == 1f && wasShown) {
                  // then it was hidden manually
                  stack.remove(it)
               }
            }
            
            var intHeight by remember { mutableIntStateOf(0) }
            val density = LocalDensity.current
            
            // Clickable Invisible Overlay
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
                  .onGloballyPositioned {
                     intHeight = it.size.height
                  } then it.sheetModifier,
               contentPadding = config.contentPadding
            ) {
               it.content.invoke(this)
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

class SheetConfig private constructor() {
   
   var backgroundColor: Color = Color.Unspecified
   lateinit var contentPadding: PaddingValues
   lateinit var shape: Shape
   
   
   private fun initialize(
      backgroundColor: Color = MyColor.SurfaceDark,
      contentPadding: PaddingValues = PaddingValues(0.dp),
      shape: Shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
   ) {
      this.backgroundColor = backgroundColor
      this.contentPadding = contentPadding
      this.shape = shape
   }
   companion object{
      @Composable
      fun remember(
         backgroundColor: Color = MyColor.SurfaceDark,
         contentPadding: PaddingValues = PaddingValues(0.dp),
         shape: Shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
      ): SheetConfig {
         return remember(backgroundColor,contentPadding,shape){
            SheetConfig().apply {
               initialize(
                  backgroundColor = backgroundColor,
                  contentPadding = contentPadding,
                  shape = shape
            )
         }
         }
      }
   }
}


@OptIn(ExperimentalMaterial3Api::class)
data class BottomSheetInstance(
   var sheetState: BottomSheetState,
   val sheetConfig:@Composable ()-> SheetConfig,
   val sheetModifier:Modifier = Modifier.shadow(5.dp),
   val content: @Composable BottomSheetScope.() -> Unit,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun rememberBottomSheetInstance(
   initialDetent: SheetDetent = SheetDetent.Hidden,
   state: BottomSheetState = rememberBottomSheetState(SheetDetent.Hidden),
   sheetConfig: @Composable ()-> SheetConfig = {
      SheetConfig.remember(MyColor.Background())
   },
   content: @Composable BottomSheetScope.() -> Unit,
): BottomSheetInstance {
   
   // Save only the current detent
   var savedDetent by rememberSaveable {
      mutableStateOf(initialDetent.identifier)
   }
   
   // Track state changes using snapshotFlow
   // (best way to track something, but more verbose and is single-key so it can't track more than 1 value)
   LaunchedEffect(state) {
      snapshotFlow { state.currentDetent }
         .collect { detent ->
            savedDetent = detent.identifier
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




