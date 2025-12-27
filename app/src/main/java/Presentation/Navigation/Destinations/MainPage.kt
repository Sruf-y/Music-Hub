package Presentation.Navigation.Destinations

import SrufComposeComponents.CustomPagerTab
import SrufComposeComponents.NestedScrollingFront
import SrufComposeComponents.TabRowIndicator
import SrufComposeComponents.Text
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.TabPosition
import androidx.compose.material3.TabRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.content.edit
import com.aacomposetest.Activities.SearchTopBar
import com.aacomposetest.Classes.MyPrefs
import com.aacomposetest.ui.theme.MyColor
import com.aacomposetest.ui.theme.Theme
import com.composables.core.HorizontalSeparator
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
object MainPage {



        @Composable
        fun invoke(initialPage:Int, pageContent:@Composable (page:Int)->Unit) {

            val contextRef = LocalContext.current
            val prefs = remember { MyPrefs.getPrefs(contextRef) }

            var pagerTitles by remember {
                mutableStateOf(
                    listOf(
                        "Songs",
                        "Playlists"
                    )
                )
            }

            var pagerState = rememberPagerState(
                initialPage = initialPage,
                pageCount = { pagerTitles.size })




            // Save current page
            LaunchedEffect(pagerState.currentPage) {

                prefs.edit() {
                    putInt(
                        MyPrefs._prefs_PagerPosition.name,
                        pagerState.currentPage
                    )

                }
            }


            /**
             * Main screen pager. This currently handles the Searchbar, the Tabrow and the Pager's combined scroll
             * */
            val animationScope = rememberCoroutineScope()

            val customIndicator =
                @Composable { tabPositions: List<TabPosition> ->
                    TabRowIndicator(tabPositions, pagerState, 2.dp)
                }
            NestedScrollingFront(
                appbar = {
                    SearchTopBar()
                },
                tabRow = {
                    TabRow(
                        modifier = Modifier.wrapContentHeight(),
                        indicator = customIndicator,
                        divider = {
                            HorizontalSeparator(
                                color = MyColor.Inactive,
                                thickness = 2.dp
                            )
                        },
                        selectedTabIndex = pagerState.currentPage
                    ) {
                        pagerTitles.forEachIndexed { index, title ->
                            val interactionSource =
                                remember { MutableInteractionSource() }

                            CustomPagerTab(
                                modifier = Modifier
                                    .height(40.dp)
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
                                    Text(
                                        text = title,
                                        fontSize = 16.sp,
                                        modifier = Modifier.zIndex(2f)
                                    )
                                }
                            )
                        }
                    }
                },
                pager = {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { newPage ->
                        Box(
                            modifier = Modifier.fillMaxSize()
                        ) {

                            pageContent.invoke(newPage)

                        }
                    }
                }
            )


            // PAGER_END //

        }

    }



