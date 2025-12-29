package ui.components

import SrufComposeComponents.Text
import androidx.compose.foundation.MarqueeAnimationMode
import androidx.compose.foundation.basicMarquee
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.aacomposetest.ui.theme.Theme

@Composable
fun MarqueeText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Theme. colorScheme().inverseSurface,
) {
    Text(
        text = text,
        modifier = modifier.basicMarquee(
            animationMode = MarqueeAnimationMode.Immediately,
            initialDelayMillis = 0,
            velocity = 50.dp
        ),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        color = color
    )
}