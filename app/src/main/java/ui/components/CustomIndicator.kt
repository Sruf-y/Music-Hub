package ui.components

import SrufComposeComponents.TabRowIndicator
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.TabPosition
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import kotlinx.serialization.Serializable

@Composable
fun CustomIndicator(tabPositions: List<TabPosition>,pagerState: PagerState) =
   TabRowIndicator(tabPositions, pagerState, 2.dp)
   



