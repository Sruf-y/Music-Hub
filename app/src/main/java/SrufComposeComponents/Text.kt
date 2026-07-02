package SrufComposeComponents

import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import com.musichub.ui.theme.Theme
import androidx.compose.material3.Text as OriginalText
/**
 * @param fontWeight the typeface thickness to use when painting the text (e.g., [FontWeight.Bold]).
 * */
@Composable
fun Text(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current.copy(
        platformStyle = PlatformTextStyle(
            includeFontPadding = false // remove font padding
        )
    ),
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextStyle.Default.lineHeight,
    fontSize: TextUnit = TextStyle.Default.fontSize,
    letterSpacing: TextUnit = TextStyle.Default.letterSpacing,
    fontWeight: FontWeight? = null,
    color: Color = Theme.colorScheme().inverseSurface, // text color
    singleLine: Boolean = false,
    minLines: Int = 1,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip
) {
    OriginalText(
        text = text,
        modifier = modifier,
        style = style,
        textAlign = textAlign,
        lineHeight = lineHeight,
        fontSize = fontSize,
        letterSpacing = letterSpacing,
        fontWeight = fontWeight,
        color = color,
        maxLines = if (singleLine) 1 else maxLines,
        minLines = minLines,
        overflow = overflow
    )
}