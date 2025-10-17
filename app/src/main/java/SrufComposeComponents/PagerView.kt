package SrufComposeComponents

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabPosition
import androidx.compose.material3.TabRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.aacomposetest.Classes.NavigationRelated.MyPagerTab
import com.aacomposetest.ui.theme.MyColor
import com.aacomposetest.ui.theme.Theme
import com.composables.core.HorizontalSeparator
import kotlinx.coroutines.launch

@Composable
fun PagerView(modifier: Modifier = Modifier.fillMaxSize(), pagerState: PagerState, tabs:List<MyPagerTab>) {

    val animationScope = rememberCoroutineScope()

    val separatorThickness: Dp = 2.dp

    val customIndicator = @Composable { tabPositions:List<TabPosition>->

        TabRowIndicator(tabPositions,pagerState,separatorThickness)
    }

    Column(modifier = modifier) {



        TabRow(modifier = Modifier.wrapContentHeight(),
            indicator = customIndicator,
            divider = {
                HorizontalSeparator(color = MyColor.Inactive, thickness = separatorThickness)
            },
            selectedTabIndex = pagerState.currentPage
        ) {
            tabs.forEachIndexed { index, tab ->
                val interactionSource = remember { MutableInteractionSource() }

                CustomTab(modifier = Modifier.height(40.dp)
                    .clickable(
                        onClick = {},
                        interactionSource = interactionSource,
                        indication = null
                    ),
                    unselectedContentColor = Theme.colorScheme().surfaceDim,
                    selectedContentColor = Theme.colorScheme().outline,
                    selected = pagerState.currentPage == index,
                    onClick = {
                        animationScope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    }, text = {
                        Text(text = tab.title, fontSize = 16.sp,modifier = Modifier.zIndex(2f))
                    }
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { newPage ->
            Box(
                modifier = Modifier.fillMaxSize()
            ) {

                tabs[newPage].content.invoke()

            }
        }
    }
}

@Composable
fun CustomTab(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    unselectedContentColor:Color = Theme.colorScheme().surfaceDim,
    selectedContentColor: Color = Theme.colorScheme().outline,
    enabled: Boolean = true,

    text: @Composable () -> Unit
) {
    // Use Box instead of Tab to avoid default ripple
    Box(
        modifier = modifier
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null, // This disables the ripple!
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        val color = if (selected) {
            selectedContentColor
        } else {
            unselectedContentColor
        }

        CompositionLocalProvider(LocalContentColor provides color) {
            text()
        }
    }
}