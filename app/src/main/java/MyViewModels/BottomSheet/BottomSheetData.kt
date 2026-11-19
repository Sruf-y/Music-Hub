package MyViewModels.BottomSheet

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.DecayAnimationSpec
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.composables.core.BottomSheetScope
import com.composables.core.BottomSheetState
import com.composables.core.SheetDetent
import com.musichub.ui.theme.MyColor
import kotlinx.coroutines.CoroutineScope
import java.util.UUID

/**
 * An instance of a visible bottom sheet.
 * */
@OptIn(ExperimentalMaterial3Api::class)
@Immutable
class BottomSheetData(
   val id: String = UUID.randomUUID().toString(),
   val content: @Composable BottomSheetScope.() -> Unit,
   val initialDetent: SheetDetent = SheetDetent.FullyExpanded,
)
{
   internal var wasShown: Boolean by mutableStateOf(false)
   
   // --- render-time, filled by renderAll's attach effect ---
   internal var sheetState: BottomSheetState? by mutableStateOf(null)
   internal var sheetStateScope: CoroutineScope? = null      // which scope sheetState is bound to
   
   // --- intent / styling, mutable by callers ---
   var desiredDetent: SheetDetent by mutableStateOf(initialDetent)
   var sheetModifier: Modifier = Modifier.shadow(5.dp)
   var backgroundColor: Color by mutableStateOf(MyColor.BackgroundDark)
   var contentPadding: PaddingValues = PaddingValues(0.dp)
   var shape: Shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
}

@Immutable
class SheetSpec(
   val detents: List<SheetDetent> = listOf(SheetDetent.FullyExpanded, SheetDetent.Hidden),
   val animationSpec: AnimationSpec<Float> = spring(),
   val velocityThreshold: (() -> Float)? = null,               // null => 125.dp in px, resolved in renderAll
   val positionalThreshold: (Float) -> Float = { it * 0.5f },
   val decayAnimationSpec: DecayAnimationSpec<Float>? = null,  // null => splineBasedDecay(density)
   val confirmDetentChange: (SheetDetent) -> Boolean = { true },
)