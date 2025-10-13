package SrufComposeComponents

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabPosition
import androidx.compose.material3.TabRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
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

    val separatorThickness: Dp = 4.dp

    val customIndicator = @Composable { tabPositions:List<TabPosition>->

        TabRowIndicator(tabPositions,pagerState,separatorThickness)
    }

    Column(modifier = modifier) {



        TabRow(indicator = customIndicator,
            divider = {
                HorizontalSeparator(color = MyColor.Inactive, thickness = separatorThickness)
            },
            selectedTabIndex = pagerState.currentPage
        ) {
            tabs.forEachIndexed { index, tab ->

                Tab(modifier = Modifier.padding(bottom = separatorThickness),
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