package SrufComposeComponents

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

@Composable
fun ScaledText(
    baseSize: TextUnit = 16.sp,
    scaleFactor: Float = 1f,
    text: @Composable (TextUnit) -> Unit
) {
    val scaledSize = with(LocalDensity.current) {
        (baseSize.value * scaleFactor).sp
    }
    text.invoke(scaledSize)
}