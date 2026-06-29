package ViewModels.BottomSheet

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.aacomposetest.toDp
import com.composables.core.SheetDetent
import com.composables.core.rememberBottomSheetState
import com.composables.core.rememberModalBottomSheetState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * State management for a custom bottom sheet with drag and settle capabilities.
 *
 * @param initialOffset The initial offset in pixels (0 = fully expanded, sheetHeight = hidden)
 * @param sheetHeight The total height of the sheet in pixels
 * @param detents List of detent offsets in pixels (must be sorted ascending)
 */
class BottomSheetState(
   initialDetent: SheetDetent = SheetDetent.Hidden,
) {
   private var density: Density? = null
   fun updateDensity(density: Density){
      this.density = density
   }
   // Existing state
   private val _currentDetent = mutableStateOf(initialDetent)
   val currentDetent: SheetDetent by _currentDetent
   
   private val _targetDetent = mutableStateOf(initialDetent)
   val targetDetent: SheetDetent by _targetDetent
   
   private val _progress = mutableFloatStateOf(0f)
   val progress: Float by _progress
   
   private val _isIdle = mutableStateOf(true)
   val isIdle: Boolean by _isIdle
   
   private val _isAnimating = mutableStateOf(false)
   val isAnimating: Boolean by _isAnimating
   
   // Height maintainers
   private val _containerHeight = mutableIntStateOf(0)
   val containerHeight: Int by _containerHeight
   
   private val _contentHeight = mutableIntStateOf(0)
   val contentHeight: Int by _contentHeight
   
   // Derived values
   val contentHeightDp: Dp
      get() = with(density) {
         if(this ==null){
            return 0.dp
         }
         _contentHeight.intValue.toDp()
      }
   
   val containerHeightDp: Dp
      get() = with(density) {
         if(this ==null){
            return 0.dp
         }
         _containerHeight.intValue.toDp()
      }
   
   val offset: Dp
      get() {
         if(density==null){
            return 0.dp
         }
         val contentH = _contentHeight.intValue.toDp(density!!)
         val containerH = _containerHeight.intValue.toDp(density!!)
         
         if (contentH == 0.dp || containerH == 0.dp) return 0.dp
         
         // Calculate offset based on progress
         val maxOffset = contentH - containerH
         val currentOffset = maxOffset * (1f - _progress.floatValue)
         
         // Ensure we don't go below 0 or above maxOffset
         return currentOffset.coerceIn(0.dp, maxOffset)
      }
   
   val normalizedProgress: Float
      get() = _progress.floatValue.coerceIn(0f, 1f)
   
   val isFullyExpanded: Boolean
      get() = _currentDetent.value == SheetDetent.FullyExpanded && _progress.floatValue == 1f
   
   val isHidden: Boolean
      get() = _currentDetent.value == SheetDetent.Hidden && _progress.floatValue == 0f
   
   val isPartiallyVisible: Boolean
      get() = _progress.floatValue > 0f && _progress.floatValue < 1f
   
   // Update functions
   fun updateContainerHeight(height: Int) {
      if (height != _containerHeight.intValue) {
         _containerHeight.intValue = height
         recalculateProgress()
      }
   }
   
   fun updateContentHeight(height: Int) {
      if (height != _contentHeight.intValue) {
         _contentHeight.intValue = height
         recalculateProgress()
      }
   }
   
   fun updateHeights(containerHeight: Int, contentHeight: Int) {
      val changed = containerHeight != _containerHeight.intValue ||
         contentHeight != _contentHeight.intValue
      if (changed) {
         _containerHeight.intValue = containerHeight
         _contentHeight.intValue = contentHeight
         recalculateProgress()
      }
   }
   
   private fun recalculateProgress() {
      // Recalculate progress based on current detent and heights
      val targetProgress = if (currentDetent == SheetDetent.FullyExpanded) 1f else 0f
      _progress.floatValue = targetProgress
   }
   
   private val animationScope = CoroutineScope(Dispatchers.Main.immediate)
   
   suspend fun animateTo(target: SheetDetent) {
      if (_isAnimating.value) return
      if (_contentHeight.intValue == 0 || _containerHeight.intValue == 0) {
         // Can't animate without heights
         jumpTo(target)
         return
      }
      
      _isAnimating.value = true
      _isIdle.value = false
      
      try {
         val startProgress = _progress.floatValue
         val targetProgress = if (target == SheetDetent.FullyExpanded) 1f else 0f
         
         _targetDetent.value = target
         
         // Animate progress
         val duration = 300 // 300ms
         val startTime = System.currentTimeMillis()
         
         while (_progress.floatValue != targetProgress) {
            val elapsed = System.currentTimeMillis() - startTime
            val fraction = (elapsed.toFloat() / duration).coerceAtMost(1f)
            val easedFraction = easeInOut(fraction)
            
            val newProgress = startProgress + (targetProgress - startProgress) * easedFraction
            _progress.floatValue = newProgress.coerceIn(0f, 1f)
            
            delay(16) // ~60fps
         }
         
         _progress.floatValue = targetProgress
         _currentDetent.value = target
         _targetDetent.value = target
         
      } finally {
         _isAnimating.value = false
         _isIdle.value = true
      }
   }
   
   fun jumpTo(target: SheetDetent) {
      _currentDetent.value = target
      _targetDetent.value = target
      _progress.floatValue = if (target == SheetDetent.FullyExpanded) 1f else 0f
      _isIdle.value = true
      _isAnimating.value = false
   }
   
   suspend fun toggleExpanded() {
      if (isFullyExpanded || isPartiallyVisible) {
         animateTo(SheetDetent.Hidden)
      } else {
         animateTo(SheetDetent.FullyExpanded)
      }
   }
   
   // Drag handlers
   fun onDragStart(down:PointerInputChange, slopTriggerChange:PointerInputChange, offset:Offset){
      // Cancel any ongoing animation
      if (_isAnimating.value) {
         // Interrupt animation
         _isAnimating.value = false
      }
      _isIdle.value = false
   }
   
    fun onDrag (change:PointerInputChange, offset:Offset) :Unit  {
      if (_contentHeight.intValue == 0 || _containerHeight.intValue == 0)
      return
      
      val dragDelta = offset.y
      val maxOffset = (_contentHeight.intValue - _containerHeight.intValue).toFloat()
      
      if (maxOffset <= 0) return@onDrag
      
      // Calculate new progress based on drag
      val currentOffset = maxOffset * (1f - _progress.floatValue)
      val newOffset = (currentOffset + dragDelta).coerceIn(0f, maxOffset)
      val newProgress = 1f - (newOffset / maxOffset)
      
      _progress.floatValue = newProgress.coerceIn(0f, 1f)
      
      // Update current detent based on progress
      _currentDetent.value = if (_progress.floatValue >= 0.5f) {
         SheetDetent.FullyExpanded
      } else {
         SheetDetent.Hidden
      }
   }
   
   fun onDragEnd(change:PointerInputChange){
      if (_contentHeight.intValue == 0 || _containerHeight.intValue == 0) {
         _isIdle.value = true
         return
      }
      
      // Snap to nearest detent
      val target = if (_progress.floatValue >= 0.5f) {
         SheetDetent.FullyExpanded
      } else {
         SheetDetent.Hidden
      }
      
      if (_isAnimating.value) {
         // If currently animating, let it finish
         return@onDragEnd
      }
      
      // Animate to target
      animationScope.launch {
         try {
            _isAnimating.value = true
            val startProgress = _progress.floatValue
            val targetProgress = if (target == SheetDetent.FullyExpanded) 1f else 0f
            val duration = 200 // Faster for snap
            
            val startTime = System.currentTimeMillis()
            while (_progress.floatValue != targetProgress) {
               val elapsed = System.currentTimeMillis() - startTime
               val fraction = (elapsed.toFloat() / duration).coerceAtMost(1f)
               val easedFraction = easeInOut(fraction)
               
               val newProgress = startProgress + (targetProgress - startProgress) * easedFraction
               _progress.floatValue = newProgress.coerceIn(0f, 1f)
               
               delay(16)
            }
            
            _progress.floatValue = targetProgress
            _currentDetent.value = target
            _targetDetent.value = target
            
         } finally {
            _isAnimating.value = false
            _isIdle.value = true
         }
      }
      
   }
   
   fun onDragCancel(){
      _isIdle.value = true
      _isAnimating.value = false
   }
   
   private fun easeInOut(t: Float): Float {
      return if (t < 0.5f) 2f * t * t else -1f + (4f - 2f * t) * t
   }
   
   // Utility functions
   fun reset() {
      jumpTo(SheetDetent.Hidden)
      _containerHeight.intValue = 0
      _contentHeight.intValue = 0
   }
   
   fun isAtDetent(detent: SheetDetent): Boolean {
      return _currentDetent.value == detent &&
         _progress.floatValue == if (detent == SheetDetent.FullyExpanded) 1f else 0f
   }
   
   // For debugging
   override fun toString(): String {
      return "BottomSheetState(" +
         "currentDetent=$currentDetent, " +
         "targetDetent=$targetDetent, " +
         "progress=$progress, " +
         "containerHeight=$containerHeight, " +
         "contentHeight=$contentHeight, " +
         "offset=$offset, " +
         "isIdle=$isIdle, " +
         "isAnimating=$isAnimating" +
         ")"
   }
}

// Extension functions for easier usage
suspend fun BottomSheetState.animateToExpanded() = animateTo(SheetDetent.FullyExpanded)
suspend fun BottomSheetState.animateToHidden() = animateTo(SheetDetent.Hidden)