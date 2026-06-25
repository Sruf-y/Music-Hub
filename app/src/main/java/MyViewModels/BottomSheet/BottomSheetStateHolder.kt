package MyViewModels.BottomSheet

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import com.composables.core.SheetDetent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay

class BottomSheetStateHolder(
   initialDetent: SheetDetent = SheetDetent.Hidden
)
{
   private val _currentDetent = mutableStateOf(initialDetent)
   val currentDetent: SheetDetent by _currentDetent
   
   private val _targetDetent = mutableStateOf(initialDetent)
   val targetDetent: SheetDetent by _targetDetent
   
   private val _progress = mutableStateOf(0f)
   val progress: Float by _progress
   
   private val _isIdle = mutableStateOf(true)
   val isIdle: Boolean by _isIdle
   
   private val _isAnimating = mutableStateOf(false)
   val isAnimating: Boolean by _isAnimating
   
   private val animationScope = CoroutineScope(Dispatchers.Main.immediate)
   
   suspend fun animateTo(target: SheetDetent) {
      if (_isAnimating.value) return
      
      _isAnimating.value = true
      _isIdle.value = false
      
      try {
         // Simple animation - you can replace with more sophisticated animation
         val startProgress = _progress.value
         val targetProgress = if (target == SheetDetent.FullyExpanded) 1f else 0f
         
         _targetDetent.value = target
         
         // Animate progress
         val duration = 300 // 300ms
         val startTime = System.currentTimeMillis()
         
         while (_progress.value != targetProgress) {
            val elapsed = System.currentTimeMillis() - startTime
            val fraction = (elapsed.toFloat() / duration).coerceAtMost(1f)
            val easedFraction = easeInOut(fraction)
            
            val newProgress = startProgress + (targetProgress - startProgress) * easedFraction
            _progress.value = newProgress.coerceIn(0f, 1f)
            
            delay(16) // ~60fps
         }
         
         _progress.value = targetProgress
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
      _progress.value = if (target == SheetDetent.FullyExpanded) 1f else 0f
      _isIdle.value = true
      _isAnimating.value = false
   }
   
   private fun easeInOut(t: Float): Float {
      return if (t < 0.5f) 2f * t * t else -1f + (4f - 2f * t) * t
   }
}

// Companion object for creating instances
object BottomSheetState {
   fun create(initialDetent: SheetDetent = SheetDetent.Hidden): BottomSheetStateHolder {
      return BottomSheetStateHolder(initialDetent)
   }
}