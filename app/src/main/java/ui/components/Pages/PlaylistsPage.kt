package ui.components.Pages

import AppDatabase.PlaylistRelated.Playlist
import MyViewModels.PlaylistViewModel
import SrufComposeComponents.MyModalBottomSheet
import SrufComposeComponents.Text
import TestLayouts.TestActivity
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.res.Configuration
import androidx.appcompat.widget.PopupMenu
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aacomposetest.MyApplication
import com.aacomposetest.R
import com.aacomposetest.ui.theme.ComposeTestTheme
import com.aacomposetest.ui.theme.MyColor
import com.aacomposetest.ui.theme.Theme
import com.composables.core.HorizontalSeparator
import ui.components.BottomSheets.Content.PlaylistAddOrEdit_Content
import ui.components.BottomSheets.Content.PlaylistDelete_Content
import ui.components.PlaylistItem




@Composable
fun PlaylistsPage(
    state: LazyListState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() },
    modifier: Modifier = Modifier.fillMaxSize(),
    playlists: List<Playlist>,
    bottomPadding: Dp = 70.dp,
    onPlusClick: (() -> Unit)? = null,
    onOptionsClick:(()->Unit)? = null,
    onPlaylistClick: ((Playlist) -> Unit)? = null,
    onPlaylistOptionsClick: @Composable ((it:Playlist, menuIsOpen:MutableState<Boolean>)->Unit)? = null
) {


    Box(modifier = modifier) {


        Column(modifier = Modifier.fillMaxSize()) {

            // Topbar
            TopBar(
                playlistsList = playlists,
                onPlusClick = onPlusClick,
                onOptionsClick = onOptionsClick
            )
            //Separator
            HorizontalSeparator(color = MyColor.SeparatorGray)

            // Playlists

            LazyColumn(state = state,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = bottomPadding)
            ) {
                items(items = playlists) { thisPlaylist ->

                    PlaylistItem(
                        playlist = thisPlaylist,
                        onClick = onPlaylistClick,
                        onOptionsClick = { menuIsOpen ->

                            onPlaylistOptionsClick?.invoke(thisPlaylist,menuIsOpen)

                        }
                    )

                }
            }
        }
    }

}


@Composable
private fun TopBar(
    playlistsList: List<Playlist>,
    onPlusClick:(()-> Unit)? = null,
    onOptionsClick:(()->Unit)? = null
) {


    val buttonsSize = 45.dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
    ) {
        // Playlist count

        Text(text = "Playlists: ${playlistsList.size}", fontSize = 20.sp,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 10.dp)
        )

        Row(
            modifier = Modifier
                .wrapContentSize()
                .align(Alignment.CenterEnd)
        ) {

            // PLUS ICON
            Icon(
                painter = painterResource(R.drawable.ic_add),
                contentDescription = null,
                tint = Theme.colorScheme().inverseSurface,
                modifier = Modifier
                    .size((buttonsSize / 1.25f))
                    .clip(CircleShape)
                    .clickable(
                        onClick = {
                            onPlusClick?.invoke()
                        }
                    )
            )

            // MORE ICON
            Box{
                var isExpanded by remember { mutableStateOf(false) }
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = null,
                    tint = Theme.colorScheme().inverseSurface,
                    modifier = Modifier
                        .size((buttonsSize / 1.25f))
                        .clip(CircleShape)
                        .clickable(onClick = {
                            onOptionsClick?.invoke()

                            isExpanded = true
                        }
                        )
                )



                DropdownMenu(isExpanded,{isExpanded = false}) {
                    DropdownMenuItem(text = {Text("Launch testLayout1")}, onClick = {
                        MyApplication.instance.startActivity(Intent(MyApplication.instance,TestActivity::class.java).apply { flags = FLAG_ACTIVITY_NEW_TASK })
                        isExpanded = false
                    })
                }
            }

        }

    }
}








@Preview(showBackground = true,uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable()
private fun PDeletePreview(){
    ComposeTestTheme(forceDarkMode = true) {
        Surface(){
            PlaylistDelete_Content(playlist = Playlist("My playlist"),null,null)
        }
    }
}
