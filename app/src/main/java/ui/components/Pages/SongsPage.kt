package ui.components.Pages

import AppDatabase.PlaylistRelated.Playlist
import AppDatabase.SongRelated.Song
import ExoPlayer.ExoPlayerViewModel
import MyViewModels.ListFeedingVM
import MyViewModels.SongsViewModel
import SrufComposeComponents.IconButton
import SrufComposeComponents.MyModalBottomSheet
import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import ui.components.SongItem
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import com.aacomposetest.Classes.MyPrefs
import com.aacomposetest.Classes.MyPrefs.getCursorValue
import com.aacomposetest.Classes.MyPrefs.putCursorValue
import com.aacomposetest.R
import com.aacomposetest.ui.theme.MyColor
import kotlinx.coroutines.launch
import ui.components.BottomSheets.Content.SongsSort_Content







@Composable
fun SongsPage(
    modifier: Modifier = Modifier.fillMaxSize(),
    settingsBottomSheetVM: MyModalBottomSheet.ModalVM? = null,
    songs:List<Song>,
    exoPlayerVM: ExoPlayerViewModel,
    onOptionsClick:@Composable ((MutableState<Boolean>,Song) -> Unit)? = null,
    state: LazyListState
){
    val contextRef = LocalContext.current

    val prefs = remember { MyPrefs.getPrefs(contextRef) }

    val curentSortType by ListFeedingVM.SortedSongs.sortTypeFlow.collectAsState()
    val curentSortDirection by ListFeedingVM.SortedSongs.sortDirectionFlow.collectAsState()


    val currentlyPlayingSong by exoPlayerVM.currentSong.collectAsState()


    if(songs.isEmpty()){

        LazyColumn(modifier = modifier) {

            item("header") {
                SongsSortHeader(songs)
            }
        }
    }
    else {
        LazyColumn(modifier = modifier, state = state) {

            item("header") {
                SongsSortHeader(songs){

                    settingsBottomSheetVM?.show(){
                        SongsSort_Content(
                            currentType = curentSortType,
                            currentDirection = curentSortDirection,
                            sortTypeOptions = ListFeedingVM.SongSortType.entries.map { it.name },
                            sortDirectionOptions = ListFeedingVM.SongSortDirection.entries.map { it.name },
                            onConfirm = { type, direction->
                                ListFeedingVM.SortedSongs.setSortType(type)
                                ListFeedingVM.SortedSongs.setSortDirection(direction)

                                prefs.edit {
                                    putCursorValue(MyPrefs._prefs_globalSongsSortType,type)
                                    putCursorValue(MyPrefs._prefs_globalSongsSortDirection,direction)
                                }

                                settingsBottomSheetVM.dismissSheet()
                            }, onCancel = {
                                settingsBottomSheetVM.dismissSheet()
                            }
                        )
                    }
                }
            }

            SongsLazyListItems(
                songs = songs,
                currentlyPlayingSong = currentlyPlayingSong,
                exoPlayerVM = exoPlayerVM,
                contextRef = contextRef,
                onOptionsClick = onOptionsClick
            )

        }
    }
}

/**
 * Header for sorting items
 * */
@Composable
private fun SongsSortHeader(
    songs: List<Song>,
    onFilterClick: (() -> Unit)? = null
) {
    Box(modifier = Modifier
        .wrapContentHeight()
        .fillMaxWidth(), contentAlignment = Alignment.CenterStart){
        Text(text = "Songs(${songs.size})")
        Row(modifier = Modifier.align(Alignment.CenterEnd)) {

            if (onFilterClick != null) {
                IconButton(R.drawable.ic_sort) {
                    onFilterClick.invoke()
                }
            }
        }
    }
}



fun LazyListScope.SongsLazyListItems(
    songs: List<Song>,
    currentlyPlayingSong:Song?,
    contextRef: Context,
    exoPlayerVM: ExoPlayerViewModel,
    playlistTitle: String? = null,
    itemModifier: LazyItemScope.(Song) -> Modifier = { it ->
        Modifier
            .animateItem()
            .clickable(
                onClick = {
                    exoPlayerVM.setSong(
                        song = it,
                        startOnPrepared = true,
                        context = contextRef,
                        playlistTitle = playlistTitle,

                        )
                }
            )
    },
    onOptionsClick: @Composable ((MutableState<Boolean>,Song) -> Unit)? = null
){

    items(items = songs, key = { it.id }) { song ->

        val textColor = if(currentlyPlayingSong == song){
            MyColor.SelectedGold
        }else{
            MyColor.TextColor()
        }

        SongItem(
            song = song,
            textColor = textColor,
            modifier = itemModifier(song),
            onOptionsClick = {
                onOptionsClick?.invoke(it, song)
            }
        )
    }
}

