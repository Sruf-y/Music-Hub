package SrufComposeComponents

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
/**
 * @param modifier The modifier of the box that contains the content.
 * @param content The keyboard aware content. It gives isImeVisible:Boolean, imeInsets:Int, navBarInsets:Int and expects to return.
 * */
@Composable
fun KeyboardAwareBox(modifier:Modifier = Modifier, content:@Composable BoxScope.(isImeVisible:Boolean, imeInsets:Int, navBarInsets:Int)-> Unit) {
    var isKeyboardVisible by remember { mutableStateOf(false) }

    val imeInsets = WindowInsets.ime.getBottom(LocalDensity.current)
    val navigationBarsInsets = WindowInsets.navigationBars.getBottom(LocalDensity.current)

    // Keyboard is considered visible if IME insets are greater than navigation bar insets
    val keyboardVisible = imeInsets > navigationBarsInsets

    LaunchedEffect(keyboardVisible) {
        if (keyboardVisible != isKeyboardVisible) {
            isKeyboardVisible = keyboardVisible

        }
    }

    Box(modifier = modifier){
        content.invoke(this,
            isKeyboardVisible,
            imeInsets,
            navigationBarsInsets
        )
    }


}
