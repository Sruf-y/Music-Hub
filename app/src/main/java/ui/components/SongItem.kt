package ui.components

import AppDatabase.SongRelated.Song
import SrufComposeComponents.IconButton
import SrufComposeComponents.Text
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.aacomposetest.Classes.MyIcons
import com.aacomposetest.Classes.SimpleDropdownMenuState
import com.aacomposetest.R
import com.aacomposetest.ui.theme.SrufTheme
import com.aacomposetest.ui.theme.MyColor
import com.aacomposetest.ui.theme.Theme
import com.aacomposetest.ui.theme.Theme.Glint.GlintMaskBox
import java.io.File


@Preview(showBackground = true)
@Composable
private fun Preview(){
    SrufTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier
                .fillMaxSize()
                .background(MyColor.SeparatorGray),
                verticalArrangement = Arrangement.Center
            ){

                SongItem_Mockup(modifier = Modifier.background(MyColor.Red))
                SongItem(modifier = Modifier.background(MyColor.Blue))
            }
        }
    }
}

@Composable
fun SongItem_Mockup(
    modifier: Modifier = Modifier,
    height:Dp = MyIcons.sizeBig,
    iconTint:Color = Theme.IconTint(),
    textPadding: PaddingValues = PaddingValues(0.dp,0.dp),
    optionsMenu: SimpleDropdownMenuState? = null,
    leftmostContent: @Composable (RowScope.() -> Unit)? = null
){
    Box(modifier = modifier.padding(vertical = (2.5).dp)){
        Row(
            modifier = Modifier
                .padding(vertical = 5.dp)
                .height(height)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            
            
            leftmostContent?.invoke(this@Row)
            
            
            
            // Song icon
            Box(contentAlignment = Alignment.Center,
                modifier = Modifier
                    .padding(horizontal = 10.dp)
                    .fillMaxHeight(0.8f)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(size = 10.dp))
            ){
                
                Theme.Glint.GlintMaskBox(modifier = Modifier.fillMaxSize())
                
            }
            
            // Song title & artist
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(textPadding)
            ){
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                ) {
                    Spacer(modifier = Modifier.weight(2f))
                    
                    GlintMaskBox(modifier = Modifier
                        .height(20.dp)
                        .width(150.dp))
                    
                    GlintMaskBox(modifier = Modifier
                        .height(20.dp)
                        .width(80.dp))
                    
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            
            
            
            
            // Playlist More options
            
            if(optionsMenu!=null){
                Box(contentAlignment = Alignment.Center,modifier = Modifier.padding(5.dp)) {
                    
                    IconButton(enabled = false,
                        imageVector = Icons.Default.MoreVert,
                        modifier = Modifier.size(45.dp,45.dp),
                        iconSize = Size(0.75f,0.75f),
                        tint = iconTint,
                        onClick = {
                            optionsMenu.open()
                        }
                    )
                    
                    optionsMenu.RenderMenu()
                }
            }
        }
    }
}

@Composable
fun SongItem(
    modifier: Modifier = Modifier,
    song: Song? = null,
    textColor:Color = Theme.TextColor(),
    titleFontSize: TextUnit = 16.sp,
    artistFontSize: TextUnit = 12.sp,
    height:Dp = MyIcons.sizeBig,
    iconTint:Color = Theme.IconTint(),
    textPadding: PaddingValues = PaddingValues(0.dp,0.dp),
    optionsMenu: SimpleDropdownMenuState? = null,
    leftmostContent: @Composable (RowScope.() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .padding(vertical = (2.5).dp)
    ) {
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
                    .padding(horizontal = 5.dp)
                    .fillMaxHeight(0.8f)
                    .aspectRatio(1f)
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
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(textPadding)
            ){
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
            

            // Playlist More options

            if(optionsMenu!=null){
                Box(contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(MyIcons.sizeMedium, MyIcons.sizeMedium)
                        .padding(5.dp)
                ) {
                    IconButton(
                        imageVector = Icons.Default.MoreVert,
                        modifier = Modifier.fillMaxSize(),
                        iconSize = Size(1f,1f),
                        tint = iconTint,
                        onClick = {
                            optionsMenu.open()
                        }
                    )
                    
                    optionsMenu.RenderMenu()
                }
            }
        }
    }


}