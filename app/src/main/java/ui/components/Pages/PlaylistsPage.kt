package ui.components.Pages

import AppDatabase.PlaylistRelated.Playlist
import MyViewModels.SheetViewmodel
import MyViewModels.rememberBottomSheetInstance
import SrufComposeComponents.IconButton
import SrufComposeComponents.Text
import android.content.res.Configuration
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
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.musichub.Classes.SimpleDropdownMenuState
import com.musichub.Classes.rememberSimpleDropdownMenu
import com.musichub.R
import com.musichub.ui.theme.SrufTheme
import com.musichub.ui.theme.MyColor
import com.musichub.ui.theme.Theme
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
    onPlusClick: (()-> Unit)? = null,
    optionsButtonMenu:SimpleDropdownMenuState? = null,
    onPlaylistClick: ((Playlist) -> Unit)? = null,
    globalBottomSheetViewModel: SheetViewmodel
) {
    var playlistToEdit by remember { mutableStateOf<Playlist?>(null) }
    
    val deleteItem_Content = rememberBottomSheetInstance {
        PlaylistDelete_Content(
            playlist = playlistToEdit,
            bottomSheetVM = globalBottomSheetViewModel
        )
    }
    val renameItem_Content = rememberBottomSheetInstance {
        PlaylistAddOrEdit_Content(
            existingPlaylists = playlists,
            bottomSheetVM = globalBottomSheetViewModel,
            playlist = playlistToEdit?.title
        )
    }

    Box(modifier = modifier) {


        Column(modifier = Modifier.fillMaxSize()) {

            // Topbar
            TopBar(
                playlistsList = playlists,
                onPlusClick = onPlusClick,
                optionsButtonMenu = optionsButtonMenu
            )
            
            //Separator
            HorizontalSeparator(color = MyColor.SeparatorGray)

            // Playlists
            LazyColumn(state = state,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = bottomPadding)
            ) {
                items(items = playlists) { item ->
                    PlaylistItem(
                        playlist = item,
                        onClick = onPlaylistClick,
                        optionsDropdownMenu = rememberSimpleDropdownMenu(
                            listOf(
                                "Rename" to {
                                    playlistToEdit = item
                                    
                                    globalBottomSheetViewModel.addSheet(
                                        renameItem_Content
                                    )
                                },
                                "Delete" to {
                                    playlistToEdit = item
                                    
                                    globalBottomSheetViewModel.addSheet(
                                        deleteItem_Content
                                    )
                                }
                            )
                        )
                    )
                }
            }
        }
    }

}


@Composable
private fun TopBar(
    playlistsList: List<Playlist>,
    onPlusClick:(()->Unit)? = null,
    optionsButtonMenu: SimpleDropdownMenuState? = null
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
            IconButton(
                resId = R.drawable.ic_add,
                tint = Theme.colorScheme().inverseSurface,
                modifier = Modifier
                    .size(buttonsSize)
                    .clip(CircleShape),
                iconScale = Size(0.75f, 0.75f)
            ) {
                onPlusClick?.invoke()
            }
            
            

            // MORE ICON
            Box{
                IconButton(
                    imageVector = Icons.Default.MoreVert,
                    tint = Theme.colorScheme().inverseSurface,
                    modifier = Modifier
                        .size(buttonsSize)
                        .clip(CircleShape),
                    iconSize = Size(0.75f,0.75f)
                ){
                    optionsButtonMenu?.open()
                }
                optionsButtonMenu?.RenderMenu()
            }

        }

    }
}








@Preview(showBackground = true,uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable()
private fun PDeletePreview(){
    SrufTheme(forceDarkMode = true) {
        Surface(){
            PlaylistDelete_Content(playlist = Playlist("My playlist"),null)
        }
    }
}
