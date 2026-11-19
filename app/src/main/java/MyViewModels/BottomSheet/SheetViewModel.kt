package MyViewModels.BottomSheet

import androidx.compose.animation.splineBasedDecay
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import androidx.lifecycle.ViewModel
import com.composables.core.BottomSheet
import com.composables.core.BottomSheetScope
import com.composables.core.BottomSheetState
import com.composables.core.SheetDetent
import com.musichub.ui.theme.MyColor

class SheetViewModel() : ViewModel(
) {
   // Single source of truth. Order = stacking order.
   internal var stack by mutableStateOf(emptyList<BottomSheetData>())
      private set
   
   // ---- external API (callable from plain code) ----
   
   fun addSheet(data: BottomSheetData) {
      stack = stack + data
   }
   fun addSheet(content: @Composable BottomSheetScope.()-> Unit) {
      stack = stack + BottomSheetData(content = content)
   }
   
   fun requestDismissCurrent() {
      stack.lastOrNull()
         ?.takeIf { it.sheetState?.isIdle != false }   // don't fight an active drag
         ?.let { markDismissing(it) }
   }
   
   fun requestDismissAll() {
      stack.forEach { markDismissing(it) }
   }
   
   private fun markDismissing(sheet: BottomSheetData) {
      sheet.desiredDetent = SheetDetent.Hidden   // observable var on the instance
   }
   
   internal fun onSheetHidden(id: String) {
      stack = stack.filterNot { it.id == id }
   }
   
   // ---- rendering ----
   
   @Composable
   internal fun renderAll(
      specFor: (BottomSheetData) -> SheetSpec = { SheetSpec() },scrim: @Composable (progress: Float)-> Unit
   ) {
      val scope = rememberCoroutineScope()
      val density = LocalDensity.current
      
      // Attach/re-attach sheetState. Keyed on `scope` so a rotation (which cancels the old
      // scope) rebuilds the state with a live scope instead of animating on a dead one.
      LaunchedEffect(stack, scope) {
         stack.forEach { sheet ->
            if (sheet.sheetState == null || sheet.sheetStateScope !== scope) {
               val isFirstAttach = sheet.sheetState == null
               val spec = specFor(sheet)
               sheet.sheetState = BottomSheetState(
                  initialDetent = if (isFirstAttach) SheetDetent.Hidden else sheet.desiredDetent,   // resume at intent, not initialDetent
                  detents = spec.detents,
                  coroutineScope = scope,
                  animationSpec = spec.animationSpec,
                  velocityThreshold = spec.velocityThreshold
                     ?: { with(density) { 125.dp.toPx() } },
                  positionalThreshold = spec.positionalThreshold,
                  decayAnimationSpec = spec.decayAnimationSpec
                     ?: splineBasedDecay(density),
                  confirmDetentChange = spec.confirmDetentChange,
                  density = { density },
               )
               sheet.sheetStateScope = scope
            }
         }
      }
      
      
      
      Box(
         modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .statusBarsPadding()
            .displayCutoutPadding()
            .navigationBarsPadding()
            .imePadding()
      ) {
         stack.fastForEachIndexed { index, sheet ->
            val state = sheet.sheetState ?: return@fastForEachIndexed
            
            if(index == stack.lastIndex || index == stack.lastIndex-1){
               scrim.invoke(state.progress(SheetDetent.Hidden, SheetDetent.FullyExpanded))
            }
            
            key(sheet.id) {
               
               // 1) Drive programmatic intent. Keyed on desiredDetent only.
               LaunchedEffect(sheet.desiredDetent) {
                  if (state.isIdle && state.currentDetent != sheet.desiredDetent) {
                     state.animateTo(sheet.desiredDetent)
                  }
               }
               
               // 2) Arm: as soon as the sheet has moved off Hidden (mid-rise counts).
               LaunchedEffect(state.progress(SheetDetent.Hidden, SheetDetent.FullyExpanded)) {
                  if (state.progress(SheetDetent.Hidden, SheetDetent.FullyExpanded) > 0.001f) {
                     sheet.wasShown = true
                  }
               }
               
               // 3) Remove once settled at Hidden AND armed. ONE copy.
               LaunchedEffect(state.currentDetent, state.isIdle) {
                  if (state.isIdle && state.currentDetent == SheetDetent.Hidden && sheet.wasShown) {
                     onSheetHidden(sheet.id)
                  }
               }
               
               BottomSheet(
                  state,
                  shape = sheet.shape,
                  backgroundColor = sheet.backgroundColor,
                  modifier = Modifier
                     .wrapContentSize()
                     .then(sheet.sheetModifier)
                     .clickable { /* no-op */ },
                  contentPadding = sheet.contentPadding,
               ) {
                  sheet.content.invoke(this)
               }
               
               
            }
         }
      }
   }
}



@Composable
internal fun BlackBackground(
   alpha:Float,
   color:Color = MyColor.SemiTransparent(),
   onClick:()-> Unit
) =
   Box(modifier = Modifier
      .fillMaxSize()
      .alpha(alpha)
      .background(color)
      .clickable(
         interactionSource = remember { MutableInteractionSource() },
         indication = null,
         onClick = onClick
      )
   )