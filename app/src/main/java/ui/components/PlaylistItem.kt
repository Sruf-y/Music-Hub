package ui.components

import AppDatabase.PlaylistRelated.Playlist
import SrufComposeComponents.Text
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aacomposetest.ui.theme.SrufTheme

@Composable
fun PlaylistItem(
    modifier:Modifier = Modifier
        .height(60.dp)
        .fillMaxWidth(),
    playlist: Playlist,
    fontSize: TextUnit = 18.sp,
    thumbNailSize:Dp = 45.dp,
    textPadding: PaddingValues = PaddingValues(10.dp,0.dp),
    optionsSize: Dp = 45.dp,
    onOptionsClick: @Composable (MutableState<Boolean>) -> Unit = {},
    onClick:((Playlist)->Unit)? = null
) {

    val iconTint = if(isSystemInDarkTheme()) Color.White else Color.Black


    Row(
        modifier = modifier
        .clickable(
            onClick = {
                onClick?.invoke(playlist)
            }
        ),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {

        // Playlist icon
        Box(contentAlignment = Alignment.Center){
            Icon(painter = painterResource(playlist.thumbNail),
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(thumbNailSize)
            )
        }

        // Playlist title
        Box(modifier = Modifier.weight(1f)){
            Text(text = playlist.title,
                fontSize = fontSize,
                modifier = Modifier.padding(textPadding))
        }


        var menuIsExpanded = remember { mutableStateOf(false) }
        // Playlist More options
        Box(contentAlignment = Alignment.Center){

            Button(
                onClick = {
                    menuIsExpanded.value = true
                },
                colors = ButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.White,
                    disabledContentColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent
                ),
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(optionsSize)
            ) {

            }

            Icon(imageVector = Icons.Default.MoreVert,
                contentDescription = null,
                modifier = Modifier
                    .size(optionsSize / 1.5f),
                tint = iconTint
            )

            onOptionsClick.invoke(menuIsExpanded)
        }
    }
}


@Preview(showBackground = true)
@Composable
private fun MyPreview(){
    SrufTheme {

            Surface(modifier = Modifier.fillMaxSize()){
                Box(modifier = Modifier.fillMaxSize()){

                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center
                    ) {
                        PlaylistItem(playlist = Playlist("My Playlist Title"))
                    }


                }
        }

    }
}