package ui.components.Pages

import AppDatabase.PlaylistRelated.Playlist
import ExoPlayer.myExoPlayer
import MyViewModels.ListFeedingVM
import SrufComposeComponents.ElevatedButton
import SrufComposeComponents.IconButton
import SrufComposeComponents.Text
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.aacomposetest.Classes.Checkable
import com.aacomposetest.ui.theme.SrufTheme
import androidx.compose.runtime.getValue
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ui.components.SongItem
import androidx.compose.runtime.produceState


@Preview(showBackground = true)
@Composable
private fun Preview(){
    SrufTheme {
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
    onBack:()->Unit = {}
) {
    val allSongs by ListFeedingVM.allSongs.collectAsState()

    val scope = rememberCoroutineScope()

    val contextRef = LocalContext.current

    val playlist by produceState<Playlist?>(initialValue = null, playlistTitle) {
        value = withContext(Dispatchers.IO) {
            Playlist.getPlaylist(contextRef, playlistTitle)
        }
    }

    val checkableItems = remember(allSongs, playlist) {
        val initialCheckedIds = playlist?.songsIdList?.toSet() ?: emptySet()

        allSongs?.map { song ->
            Checkable(
                item = song,
                isChecked = initialCheckedIds.contains(song.id)  // This initializes properly
            )
        } ?: emptyList()
    }

    val selectedCount by remember(checkableItems) {
        derivedStateOf {
            checkableItems.count { it.isChecked.value }
        }
    }


    BackHandler(true) {
        onBack.invoke()
    }


    Surface(modifier = modifier){


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
                    Text(text = "Selected: $selectedCount from ${checkableItems.size}",modifier = Modifier )
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


                    if(playlist!=null) {
                        items(items = checkableItems, key = { it.item.id }) { item ->
                            SongItem(
                                leftmostContent = {
                                    val checkboxInteractionSong =
                                        remember { MutableInteractionSource() }
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

                                    }
                                )
                            )
                        }
                    }else{
                        item {
                            Text("Playlist is NULL. Title = [${playlistTitle}]")
                        }
                    }
            }
        }



        Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.weight(10f))

            // If the current valid list is different from the playlist list
            //if(playlist?.songsIdList?.containsAll(validList.filter { it->it.isChecked.value }.map{it->it.item.id})==false)
                ElevatedButton(
                    onClick = {



                            playlist?.songsIdList?.apply {


                                removeAll(checkableItems.filter { it -> !it.isChecked.value }
                                    .map { it -> it.item.id })

                                checkableItems.filter { it -> it.isChecked.value }
                                    .map { it -> it.item.id }.let { mappedList ->


                                        addAll(mappedList.filter { it ->
                                            !this.contains(
                                                it
                                            )
                                        })
                                    }

                            }



                            playlist?.save(contextRef) {

                                /**
                                 * If the currently playing song was in this playlist, don't continue playing in this playlist.
                                 * */
                                myExoPlayer.currentlyPlayingSong.value?.let {
                                    if(playlist?.songsIdList?.contains(it.id) == true){
                                        myExoPlayer.setCurrentPlaylist(null)
                                    }
                                }

                                onBack.invoke()
                            }



                }
                ) {
                    Text(text = "Confirm", fontSize = 18.sp, modifier = Modifier.padding(5.dp))
                }


            Spacer(Modifier.weight(0.5f))
        }
    }
}