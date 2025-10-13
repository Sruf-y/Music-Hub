package ui.components.Pages

import AppDatabase.PlaylistRelated.Playlist
import AppDatabase.SongRelated.Song
import SrufComposeComponents.ElevatedButton
import SrufComposeComponents.IconButton
import SrufComposeComponents.Text
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.aacomposetest.Classes.Checkable
import com.aacomposetest.ui.theme.ComposeTestTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.room.util.TableInfo
import com.aacomposetest.ui.theme.MyColor
import com.aacomposetest.ui.theme.Theme
import kotlinx.coroutines.launch
import ui.components.SongItem


@Preview(showBackground = true)
@Composable
private fun Preview(){
    ComposeTestTheme {
        Surface(modifier = Modifier.fillMaxSize()){
            Box(modifier = Modifier.fillMaxSize()){
                AddSongsToPlaylistPage()
            }
        }
    }
}


@Composable
fun AddSongsToPlaylistPage(
    modifier: Modifier = Modifier.fillMaxSize(),
    lazyListState: LazyListState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() },
    playlistTitle: String= "",
    allSongs: List<Song>? = null,
    navController: NavHostController? = null,
    onBack:()->Unit = {},
    onConfirm: ((List<Checkable<Song>>) -> Unit)? = null
) {
    val contextRef = LocalContext.current
    val scope = rememberCoroutineScope()

    var playlist: Playlist? by remember { mutableStateOf(null) }

    DisposableEffect(Unit) {

        scope.launch {
            playlist = Playlist.getPlaylist(contextRef,playlistTitle)
        }

        onDispose {}
    }


    val validList = remember(allSongs, playlist) {
        allSongs?.map { song ->
                Checkable(
                    item = song,
                    isChecked = playlist?.songsIdList?.contains(song.id) == true
                )
        }?:emptyList()
    }

    BackHandler(true) {
        onBack.invoke()
    }


    Surface(modifier = modifier){

        var selectedCount by rememberSaveable {
            mutableIntStateOf(
                validList.count { it.isChecked.value }
            )
        }
        Column {
            // Header
            Box(modifier = Modifier
                .height(50.dp)
                .fillMaxWidth()
            ){
                // Selected counter
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .wrapContentWidth()
                        .fillMaxHeight()
                        .padding(horizontal = 10.dp)
                ){
                    Text(text = "Selected: $selectedCount",modifier = Modifier )
                }

                // More button
                Box(modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .wrapContentWidth()
                    .fillMaxHeight()
                    .padding(horizontal = 5.dp)
                ) {

                    var menuShown by rememberSaveable { mutableStateOf(false) }

                    IconButton(
                        Icons.Default.MoreVert,
                        iconSize = Size(0.8f,0.8f),
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(40.dp)
                    )

                    DropdownMenu(expanded = menuShown, onDismissRequest = {
                        menuShown = false
                    }) {

                    }
                }
            }

            LazyColumn(modifier = Modifier
                .fillMaxSize(),
                state = lazyListState,
            ) {


                if (playlist != null)
                    items(items = validList) { item ->
                        SongItem(
                            leftmostContent = {
                                val checkboxInteractionSong = remember { MutableInteractionSource() }
                                Checkbox(
                                    modifier = Modifier
                                        .padding(horizontal = 10.dp),
                                    checked = item.isChecked.value,
                                    onCheckedChange = null
                                )
                            },
                            song = item.item,
                            modifier = Modifier.clickable(
                                onClick = {
                                    // inverse the check and update the selected count
                                    item.isChecked.value = !item.isChecked.value

                                    selectedCount = validList.count { it.isChecked.value }
                                })
                        )
                    }
            }
        }



        Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.weight(10f))

            // If the current valid list is different from the playlist list
            //if(playlist?.songsIdList?.containsAll(validList.filter { it->it.isChecked.value }.map{it->it.item.id})==false)
                ElevatedButton(onClick = {
                    onConfirm?.invoke(validList.map { it -> it })
                }) {
                    Text(text = "Confirm", fontSize = 18.sp, modifier = Modifier.padding(5.dp))
                }


            Spacer(Modifier.weight(0.5f))
        }
    }
}