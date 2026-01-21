package Navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import ui.components.ViewDrawer
import ui.components.ViewDrawerState
import ui.components.ViewDrawerValue
import ui.components.rememberViewDrawerState


@Composable
fun SafeDrawer(
    state: ViewDrawerState = rememberViewDrawerState(initialValue = ViewDrawerValue.Closed),
    drawerContent: @Composable ()->Unit,
    mainContent: @Composable ()->Unit,
) {
    val systemBars = WindowInsets.systemBars
    val navBars = WindowInsets.navigationBars
    val displayCutout = WindowInsets.displayCutout
    
    val layoutDirection = LocalLayoutDirection.current
    
    val localDensity = LocalDensity.current
    
    ViewDrawer(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                start = with(localDensity) {
                    maxOf(
                        systemBars.getLeft(this, layoutDirection),
                        navBars.getLeft(this, layoutDirection),
                        displayCutout.getLeft(this, layoutDirection)
                    ).toDp()
                }
            ),
        state = state,
        //scrimColor = MyColor.Transparent.toArgb(),
        mainContent = mainContent,
        drawerContent = drawerContent
    )
}