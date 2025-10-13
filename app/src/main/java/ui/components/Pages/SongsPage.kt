package ui.components.Pages

import AppDatabase.PlaylistRelated.Playlist
import AppDatabase.SongRelated.Song
import ExoPlayer.ExoPlayerViewModel
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongsRefreshPage(
    modifier: Modifier = Modifier.fillMaxSize(),
    prefs: SharedPreferences,
    listPadding: PaddingValues = PaddingValues(0.dp),
    songs: List<Song>,
    exoPlayerVM: ExoPlayerViewModel,
    onOptionsClick: @Composable ((MutableState<Boolean>,Song) -> Unit)? = null,
    listState: LazyListState,
    isRefreshing: MutableState<Boolean>,
    onRefresh: suspend () -> Unit = {},
    settingsBottomSheetVM: MyModalBottomSheet.ModalVM? = null,
) {
    val scope = rememberCoroutineScope()



    val refreshState = rememberPullToRefreshState()


    PullToRefreshBox(
        state = refreshState,
        modifier = modifier,
        isRefreshing = isRefreshing.value,
        onRefresh = {
            scope.launch {
                onRefresh.invoke()
            }

        }
    ) {
        SongsPage(
            prefs = prefs,
            modifier = Modifier
                .fillMaxSize()
                .padding(listPadding),
            settingsBottomSheetVM = settingsBottomSheetVM,
            songs = songs,
            exoPlayerVM = exoPlayerVM,
            onOptionsClick = onOptionsClick,
            state = listState
        )
    }



}




@Composable
fun SongsPage(
    modifier: Modifier = Modifier.fillMaxSize(),
    settingsBottomSheetVM: MyModalBottomSheet.ModalVM? = null,
    prefs: SharedPreferences,
    songs:List<Song>,
    exoPlayerVM: ExoPlayerViewModel,
    onOptionsClick:@Composable ((MutableState<Boolean>,Song) -> Unit)? = null,
    state: LazyListState
){

    val sortTypeOptions = listOf<String>("DateAdded","Title","Artist","Length")
    val sortDirectionOptions = listOf<String>("Descending","Ascending")


    var filterType by remember { mutableStateOf(prefs.getCursorValue(MyPrefs._prefs_globalSongsSortType,sortTypeOptions.first())) }
    var filterDirection by remember { mutableStateOf(prefs.getCursorValue(MyPrefs._prefs_globalSongsSortDirection,sortDirectionOptions.first())) }

    val contextRef = LocalContext.current

    val currentlyPlayingSong by exoPlayerVM.currentSong.collectAsState()

    val filteredSongs by remember(songs, filterType, filterDirection) {
        derivedStateOf {
            val sortedList = when(filterType) {
                sortTypeOptions[0] -> songs.sortedBy { it.id }
                sortTypeOptions[1] -> songs.sortedBy { it.title }
                sortTypeOptions[2] -> songs.sortedBy { it.artistName } // Handle potential null author
                sortTypeOptions[3] -> songs.sortedBy { it.duration }
                else -> songs.sortedBy { it.id } // Default case
            }

            // And sort direction
            if (filterDirection == sortDirectionOptions[0]) {
                sortedList.reversed()
            } else {
                sortedList
            }
        }
    }


    if(filteredSongs.isEmpty()){

        LazyColumn(modifier = modifier, state = state) {

            item("header") {
                SongsSortHeader(filteredSongs)
            }
        }
    }
    else {
        LazyColumn(modifier = modifier, state = state) {

            item("header") {
                SongsSortHeader(filteredSongs){

                    settingsBottomSheetVM?.show(){
                        SongsSort_Content(
                            currentType = filterType,
                            currentDirection = filterDirection,
                            sortTypeOptions = sortTypeOptions,
                            sortDirectionOptions = sortDirectionOptions,
                            onConfirm = { type, direction->
                                filterType = type
                                filterDirection = direction

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
                songs = filteredSongs,
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

