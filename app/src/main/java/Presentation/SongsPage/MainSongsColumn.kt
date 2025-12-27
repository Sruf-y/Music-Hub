package Presentation.SongsPage

import AppDatabase.SongRelated.Song
import ExoPlayer.ExoPlayerViewModel
import ExoPlayer.myExoPlayer
import MyViewModels.ListFeedingVM
import SrufComposeComponents.MyModalBottomSheet
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import com.aacomposetest.Classes.MyPrefs
import com.aacomposetest.Classes.MyPrefs.putCursorValue
import com.aacomposetest.ui.theme.MyColor
import ui.components.BottomSheets.Content.SongsSort_Content
import ui.components.SongItem

/**
 * Collects songs from [ListFeedingVM.SortedSongs.sortedAndFilteredSongsFlow]
 * */
@Composable
fun MainSongsColumn(
    modifier: Modifier = Modifier,
    /**
     * Padding for the column
     * */
    contentPadding: PaddingValues = PaddingValues(0.dp),
    state: LazyListState,
    /**
     * Play the song on click. From null playlist.
     * */
    onItemClick: (Song) -> Unit = {
        ExoPlayerViewModel.setSong(
            song = it,
            startOnPrepared = true,
            playlistTitle = null
            )
    },
    settingsBottomSheetVM: MyModalBottomSheet.ModalVM? = null,
) {

    val contextRef = LocalContext.current

    val songs by ListFeedingVM.SortedSongs.sortedAndFilteredSongsFlow.collectAsState()

    val currentlyPlayingSong by myExoPlayer.currentlyPlayingSong.collectAsState()

    val curentSortType by ListFeedingVM.SortedSongs.sortTypeFlow.collectAsState()
    val curentSortDirection by ListFeedingVM.SortedSongs.sortDirectionFlow.collectAsState()

    val prefs = remember { MyPrefs.getPrefs(contextRef) }

    LazyColumn(
        modifier = modifier,
        /**
         * I pass state like this because if the state loads
         * while the song list is empty, it resets the state's pre-loaded values
         * */
        state = if(songs.isEmpty()) rememberLazyListState() else state,
        contentPadding = contentPadding
    ) {

        item(key = "sort_header") {
            if(songs.isEmpty()){
                SortHeader(songs = songs)
            }
            else {
                SortHeader(
                    songs = songs,
                    onFilterClick = {
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
                )
            }
        }

        items(
            items = songs,
            key = {
                it.id
            },
            itemContent = {song->

                val textColor = if(currentlyPlayingSong == song){
                    MyColor.SelectedGold
                }else{
                    MyColor.TextColor()
                }

                SongItem(
                    song = song,
                    textColor = textColor,
                    modifier = Modifier
                        .animateItem()
                        .clickable(
                            onClick = {
                                onItemClick.invoke(song)
                            }
                        ),
                    optionsMenu = {
                        // TODO
                    }
                )
            }
        )
    }
}