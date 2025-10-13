package ui.components

import AppDatabase.SongRelated.Song
import ExoPlayer.ExoPlayerViewModel
import SrufComposeComponents.IconButton
import SrufComposeComponents.Text
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.aacomposetest.R
import com.aacomposetest.ui.theme.ComposeTestTheme
import com.aacomposetest.ui.theme.MyColor
import java.io.File


@Preview(showBackground = true)
@Composable
private fun Preview(){
    ComposeTestTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier
                .fillMaxSize()
                .background(MyColor.SeparatorGray), contentAlignment = Alignment.Center){

                SongItem(modifier = Modifier.background(MyColor.Red))
            }
        }
    }
}



@Composable
fun SongItem(
    modifier: Modifier = Modifier,
    song: Song? = null,
    textColor:Color = MyColor.TextColor(),
    titleFontSize: TextUnit = 16.sp,
    artistFontSize: TextUnit = 12.sp,
    thumbNailSize:Float = 55f,
    height:Dp = 70.dp,
    textPadding: PaddingValues = PaddingValues(10.dp,0.dp),
    optionsSize: Float = 45f,
    showOptionsButton: Boolean = true,
    onOptionsClick: @Composable ((MutableState<Boolean>) -> Unit)? = {},
    leftmostContent: @Composable (RowScope.() -> Unit)? = null
) {

    val iconTint = if(isSystemInDarkTheme()) Color.White else Color.Black


    Box(modifier = modifier){
        Row(
            modifier = Modifier
                .height(height)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {


            leftmostContent?.invoke(this@Row)



            // Song icon
            Box(contentAlignment = Alignment.Center,
                modifier = Modifier
                    .padding(10.dp)
                    .size(thumbNailSize.dp)
                    .clip(RoundedCornerShape(size = 10.dp))
            ){
                if(File(song?.thumbnailPath?:"").exists()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(song?.thumbnailPath)
                            .placeholder(R.drawable.x)
                            .error(R.drawable.exclamation_sign)
                            .memoryCacheKey(song?.thumbnailPath)
                            .placeholderMemoryCacheKey(song?.thumbnailPath)
                            .allowHardware(true)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop // CenterCrop
                    )
                }
                else{
                    Icon(painter = painterResource(R.drawable.exclamation_sign),
                        contentDescription = "Song thumbnail",
                        tint = iconTint,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // Song title & artist
            Box(modifier = Modifier
                .weight(1f)
                .padding(textPadding)){
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                ) {
                    Spacer(modifier = Modifier.weight(2f))

                    Text(
                        text = song?.title?:"<song title>",
                        fontSize = titleFontSize,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = textColor

                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Text(
                        text = song?.artistName?:"<artist name>",
                        fontSize = artistFontSize,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = textColor
                    )

                    Spacer(modifier = Modifier.weight(1f))
                }
            }


            var menuIsExpanded = remember { mutableStateOf(false) }

            // Playlist More options
            if(showOptionsButton) {
                Box(contentAlignment = Alignment.Center,modifier = Modifier.padding(5.dp)) {

                    IconButton(
                        imageVector = Icons.Default.MoreVert,
                        modifier = Modifier.size(optionsSize.dp,optionsSize.dp),
                        iconSize = Size(0.75f,0.75f),
                        iconTint = iconTint,
                        onclick = {
                            menuIsExpanded.value = true
                        }
                    )

                    onOptionsClick?.invoke(menuIsExpanded)
                }
            }
        }
    }


}