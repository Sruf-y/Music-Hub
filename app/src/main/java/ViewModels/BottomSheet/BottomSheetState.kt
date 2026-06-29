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
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
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
   initialOffset: Float = 0f,
   private var sheetHeight: Float = 0f,
   private val detents: List<Float> = listOf(0f) // Offsets in pixels, sorted ascending
)
{
   // ============ PUBLIC STATE ============
   
   private val _offset = mutableFloatStateOf(initialOffset.coerceIn(0f, sheetHeight))
   val offset: Float by _offset
   
   private val _progress = mutableFloatStateOf(0f)
   val progress: Float by _progress
   
   private val _isIdle = mutableStateOf(true)
   val isIdle: Boolean by _isIdle
   
   private val _isAnimating = mutableStateOf(false)
   val isAnimating: Boolean by _isAnimating
   
   private val _velocity = mutableFloatStateOf(0f)
   val velocity: Float by _velocity
   
   // ============ INTERNAL STATE ============
   
   private val animationScope = CoroutineScope(Dispatchers.Main.immediate)
   private var animationJob: Job? = null
   private val animatable = Animatable(_offset.floatValue)
   private var isDragging = false
   
   // ============ ANIMATION SPECS ============
   
   private val defaultSettleSpec = spring<Float>(
      dampingRatio = Spring.DampingRatioNoBouncy,
      stiffness = Spring.StiffnessMedium
   )
   
   // ============ PUBLIC API ============
   
   /**
    * Update the container height (for progress calculation)
    */
   fun updateSheetHeight(height: Float) {
      sheetHeight = height
      // Clamp current offset to new height
      val clampedOffset = _offset.floatValue.coerceIn(0f, sheetHeight)
      if (clampedOffset != _offset.floatValue) {
         _offset.floatValue = clampedOffset
         _progress.floatValue = calculateProgress(clampedOffset)
      }
   }
   
   /**
    * Update the container height (Dp version)
    */
   fun updateSheetHeight(height: Dp) {
      updateSheetHeight(height.value)
   }
   
   /**
    * Dispatch a raw delta from drag gesture
    *
    * @param delta The change in pixels (positive = dragging down/closing)
    * @param isLift Whether the user has lifted their finger (triggers settling)
    */
   suspend fun dispatchRawDelta(delta: Float, isLift: Boolean = false) {
      // Stop any ongoing animation
      stopAnimation()
      
      // Apply delta with bounds (0 = expanded, sheetHeight = hidden)
      val newOffset = (_offset.floatValue + delta).coerceIn(0f, sheetHeight)
      
      // Update state
      _offset.floatValue = newOffset
      _progress.floatValue = calculateProgress(newOffset)
      
      // Track velocity (rough estimate from frame time)
      _velocity.floatValue = if (delta != 0f) {
         delta / 16f * 1000f // pixels per second (assuming 16ms frame)
      } else {
         _velocity.floatValue
      }
      
      // If this is the lift event, settle to nearest detent
      if (isLift) {
         settle()
      }
   }
   
   /**
    * Settle to the nearest detent
    *
    * @param velocity Optional velocity for fling detection
    * @param animationSpec Optional custom animation spec
    */
   suspend fun settle(
      velocity: Float = _velocity.floatValue,
      animationSpec: AnimationSpec<Float> = defaultSettleSpec
   ) {
      if (_isAnimating.value) return
      if (detents.isEmpty()) return
      
      _isAnimating.value = true
      _isIdle.value = false
      
      try {
         val currentOffset = _offset.floatValue
         val targetOffset = determineTargetOffset(currentOffset, velocity)
         
         animationJob = animationScope.launch {
            animatable.snapTo(currentOffset)
            
            animatable.animateTo(
               targetValue = targetOffset,
               animationSpec = animationSpec,
               block = {
                  _offset.floatValue = value
                  _progress.floatValue = calculateProgress(value)
                  _velocity.floatValue = 0f
               }
            )
            
            // Finalize
            _offset.floatValue = targetOffset
            _progress.floatValue = calculateProgress(targetOffset)
            _velocity.floatValue = 0f
            _isAnimating.value = false
            _isIdle.value = true
         }
         
         animationJob?.join()
         
      } catch (e: Exception) {
         _isAnimating.value = false
         _isIdle.value = true
      }
   }
   
   /**
    * Settle to a specific detent
    */
   suspend fun settleTo(
      detent: Float,
      animationSpec: AnimationSpec<Float> = defaultSettleSpec
   ) {
      if (_isAnimating.value) return
      if (detent !in detents) {
         throw IllegalArgumentException("Detent $detent not in available detents: $detents")
      }
      
      _isAnimating.value = true
      _isIdle.value = false
      
      try {
         val currentOffset = _offset.floatValue
         
         animationJob = animationScope.launch {
            animatable.snapTo(currentOffset)
            
            animatable.animateTo(
               targetValue = detent,
               animationSpec = animationSpec
            ) {
               _offset.floatValue = value
               _progress.floatValue = calculateProgress(value)
            }
            
            // Finalize
            _offset.floatValue = detent
            _progress.floatValue = calculateProgress(detent)
            _velocity.floatValue = 0f
            _isAnimating.value = false
            _isIdle.value = true
         }
         
         animationJob?.join()
         
      } catch (e: Exception) {
         _isAnimating.value = false
         _isIdle.value = true
      }
   }
   
   /**
    * Stop any ongoing animation
    */
   fun stopAnimation() {
      animationJob?.cancel()
      animationJob = null
      _isAnimating.value = false
   }
   
   /**
    * Jump directly to an offset without animation
    */
   fun jumpTo(offset: Float) {
      val clampedOffset = offset.coerceIn(0f, sheetHeight)
      stopAnimation()
      _offset.floatValue = clampedOffset
      _progress.floatValue = calculateProgress(clampedOffset)
      _velocity.floatValue = 0f
      _isIdle.value = true
      _isAnimating.value = false
   }
   
   /**
    * Jump directly to a detent without animation
    */
   fun jumpToDetent(detent: Float) {
      if (detent !in detents) {
         throw IllegalArgumentException("Detent $detent not in available detents: $detents")
      }
      jumpTo(detent)
   }
   
   /**
    * Animate to a specific offset with a custom animation spec
    *
    * @param targetOffset The offset to animate to (in pixels)
    * @param animationSpec The animation spec to use (defaults to spring with no bounce)
    */
   suspend fun animateTo(
      targetOffset: Float,
      animationSpec: AnimationSpec<Float> = defaultSettleSpec
   ) {
      if (_isAnimating.value) return
      
      // Clamp target to valid range
      val clampedTarget = targetOffset.coerceIn(0f, sheetHeight)
      
      _isAnimating.value = true
      _isIdle.value = false
      
      try {
         val currentOffset = _offset.floatValue
         
         animationJob = animationScope.launch {
            animatable.snapTo(currentOffset)
            
            animatable.animateTo(
               targetValue = clampedTarget,
               animationSpec = animationSpec
            ) {
               _offset.floatValue = value
               _progress.floatValue = calculateProgress(value)
               _velocity.floatValue = 0f
            }
            
            // Finalize
            _offset.floatValue = clampedTarget
            _progress.floatValue = calculateProgress(clampedTarget)
            _velocity.floatValue = 0f
            _isAnimating.value = false
            _isIdle.value = true
         }
         
         animationJob?.join()
         
      } catch (e: Exception) {
         _isAnimating.value = false
         _isIdle.value = true
      }
   }
   
   /**
    * Animate to a specific detent
    *
    * @param detent The detent to animate to (must be in the detents list)
    * @param animationSpec The animation spec to use
    */
   suspend fun animateToDetent(
      detent: Float,
      animationSpec: AnimationSpec<Float> = defaultSettleSpec
   ) {
      if (detent !in detents) {
         throw IllegalArgumentException("Detent $detent not in available detents: $detents")
      }
      animateTo(detent, animationSpec)
   }
   
   /**
    * Check if the sheet is at a specific detent
    */
   fun isAtDetent(detent: Float): Boolean {
      return abs(_offset.floatValue - detent) < 0.5f
   }
   
   // ============ PRIVATE HELPERS ============
   
   /**
    * Calculate progress based on current offset and sheet height
    * 0.0 = Fully Expanded, 1.0 = Fully Hidden
    */
   private fun calculateProgress(offset: Float): Float {
      return if (sheetHeight > 0) {
         (offset / sheetHeight).coerceIn(0f, 1f)
      } else {
         0f
      }
   }
   
   /**
    * Determine which detent to settle to based on position and velocity
    */
   private fun determineTargetOffset(currentOffset: Float, velocity: Float): Float {
      val sortedDetents = detents.sorted()
      val velocityThreshold = 500f // pixels per second
      
      // Find current detent based on position
      val currentDetent = sortedDetents.minByOrNull { abs(it - currentOffset) }
         ?: return sortedDetents.first()
      
      val currentIndex = sortedDetents.indexOf(currentDetent)
      
      return when {
         // Fast fling up (negative velocity = opening) → top detent
         velocity < -velocityThreshold -> sortedDetents.first()
         
         // Fast fling down (positive velocity = closing) → bottom detent
         velocity > velocityThreshold -> sortedDetents.last()
         
         // Check if we're past the threshold to the next detent
         currentIndex < sortedDetents.size - 1 -> {
            val nextDetent = sortedDetents[currentIndex + 1]
            val threshold = currentDetent + (nextDetent - currentDetent) * 0.3f
            
            if (currentOffset > threshold) {
               nextDetent
            } else {
               currentDetent
            }
         }
         
         // Last detent or no next detent
         else -> currentDetent
      }
   }
}