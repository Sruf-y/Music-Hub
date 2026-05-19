package ui.components.BottomSheets

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.composables.core.BottomSheetState
import com.composables.core.SheetDetent
import com.composables.core.rememberBottomSheetState

// Used for Player bottom sheet, just a coagulation class.
data class BottomSheetConfig(
    val peekHeight: Dp? = BSConfigDefaults.peekHeight,
    val expandedHeight: Dp = BSConfigDefaults.expandedHeight,
    val isExpandedByDefault: Boolean,
    val isHideable: Boolean,
    val alpha: Animatable<Float, AnimationVector1D> = Animatable(BSConfigDefaults.alpha),
    val peekDetent: SheetDetent? = peekHeight?.let {
        SheetDetent(
            identifier = "Peek",
            calculateDetentHeight = { containerHeight, sheetHeight ->
                peekHeight
            }
        )

    },
    val detents: List<SheetDetent> = mutableListOf<SheetDetent>().let{
        if(peekDetent == null || isHideable){
            it.add(SheetDetent.Hidden)
        }

        if(peekDetent!=null){
            it.add(peekDetent)
        }

        it.add(SheetDetent.FullyExpanded)
        it
    }.toList(),
    val bottomHeight: Dp = peekHeight?:0.dp
){
    fun resolveInitialState(): SheetDetent{
        return (if(isExpandedByDefault) SheetDetent.FullyExpanded else peekDetent)?: SheetDetent.Hidden
    }
    
    suspend fun animateVisibilityTo(
        targetValue: Float,
        animationSpec: AnimationSpec<Float> = tween(500),
    ) {
        alpha.animateTo(
            targetValue = targetValue,
            animationSpec = animationSpec
        )
    }
    
    suspend fun snapVisibilityTo(targetValue:Float){
        alpha.snapTo(
            targetValue = targetValue
        )
    }
    
    @Composable
    fun rememberDerivedBottomSheetState(initialDetent: SheetDetent = detents.first()): BottomSheetState {
        return rememberBottomSheetState(
            initialDetent = initialDetent,
            detents = detents
        )
    }
}

@Composable
fun rememberBottomSheetConfig(
    peekHeight: Dp? = BSConfigDefaults.peekHeight,
    expandedHeight: Dp = BSConfigDefaults.expandedHeight,
    isExpandedByDefault: Boolean = true,
    isHideable: Boolean = false,
    alpha:Float = 1f
): BottomSheetConfig = remember(peekHeight, expandedHeight, isHideable) {
    BottomSheetConfig(
       peekHeight = peekHeight,
       expandedHeight = expandedHeight,
       isExpandedByDefault = isExpandedByDefault,
       isHideable = isHideable,
       alpha = Animatable(alpha)
    )
}

object BSConfigDefaults{
    val expandedHeight = 250.dp
    val peekHeight = 60.dp
    val alpha = 1f
}