package ui.components

import SrufComposeComponents.Text
import androidx.compose.foundation.MarqueeAnimationMode
import androidx.compose.foundation.basicMarquee
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.musichub.ui.theme.Theme
import com.composeunstyled.LocalTextStyle
/**
 * @param fontWeight the typeface thickness to use when painting the text (e.g., [FontWeight.Bold]).
 * */
@Composable
fun LoopingText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Theme. colorScheme().inverseSurface,
    textAlign: TextAlign? = TextAlign.Center,
    fontSize: TextUnit = 16.sp,
    style: TextStyle = LocalTextStyle.current,
    fontWeight: FontWeight? = null
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
        color = color,
        textAlign = textAlign,
        fontSize = fontSize,
        style = style,
        fontWeight = fontWeight
    )
}