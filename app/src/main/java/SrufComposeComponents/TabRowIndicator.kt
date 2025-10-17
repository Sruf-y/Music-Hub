package SrufComposeComponents


import Settings.Storeables
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.TabPosition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

@Composable
fun TabRowIndicator(tabPositions:List<TabPosition>, pagerState: PagerState, separatorHeight: Dp){
    val transition = updateTransition(pagerState.currentPage)
    val indicatorStart by transition.animateDp (
        transitionSpec = {
            if(initialState < targetState){
                spring(dampingRatio = 1f, stiffness = 100f)
            }else{
                spring(dampingRatio = 1f, stiffness = 1000f)
            }
        },
        label = "TabStart"
        ){
        tabPositions[it].left
    }

    val indicatorEnd by transition.animateDp(
        transitionSpec = {
            if(initialState < targetState){
                spring(dampingRatio = 1f, stiffness = 1000f)
            }else{
                spring(dampingRatio = 1f, stiffness = 100f)
            }
        },
        label = "TabEnd"
    ){
        tabPositions[it].right
    }

    var rect by remember { mutableStateOf<Rect?>(null) }
    Box(modifier = Modifier
        .offset(x = indicatorStart)
        .wrapContentSize(align = Alignment.BottomStart)
        .fillMaxHeight(0.1f)
        .padding(bottom = separatorHeight)
        .width(indicatorEnd-indicatorStart)
        .onGloballyPositioned{
            rect = it.boundsInParent()
        }
        .padding(horizontal = 30.dp)
        .background(color = Storeables.Colors.Blue.color, shape = RoundedCornerShape(50))
        .border(width = 1.dp,color = Storeables.Colors.Pink.color, shape = RoundedCornerShape(50))
        .zIndex(-1f)
    )
}