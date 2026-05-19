package Presentation.SongsPage

import AppDatabase.AndroidDatabase
import AppDatabase.SongRelated.Song
import ExoPlayer.ExoPlayerViewModel
import ExoPlayer.myExoPlayer
import MyViewModels.ListFeedingVM
import MyViewModels.SheetViewmodel
import MyViewModels.rememberBottomSheetInstance
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import com.aacomposetest.Classes.MyPrefs
import com.aacomposetest.Classes.putCursorValue
import com.aacomposetest.Classes.rememberSimpleDropdownMenu
import com.aacomposetest.ui.theme.MyColor
import com.aacomposetest.ui.theme.Theme
import kotlinx.coroutines.launch
import ui.components.BottomSheets.Content.SongsSort_Content
import ui.components.Overlay
import ui.components.SongItem
import ui.components.SongItem_Mockup

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
   settingsBottomSheetVM: SheetViewmodel? = null,
) {
   val scope = rememberCoroutineScope()
   
   val contextRef = LocalContext.current
   
   val songs by ListFeedingVM.SortedSongs.sortedAndFilteredSongsFlow.collectAsState()
   
   val currentlyPlayingSong by myExoPlayer.currentlyPlayingSong.stateFlow.collectAsState()
   
   val curentSortType by ListFeedingVM.SortedSongs.sortTypeFlow.stateFlow.collectAsState()
   val curentSortDirection by ListFeedingVM.SortedSongs.sortDirectionFlow.stateFlow.collectAsState()
   
   val prefs = remember { MyPrefs.getPrefs(contextRef, MyPrefs.PrefDirs.VisualData) }
   
   val songsSortContent = rememberBottomSheetInstance {
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
            
            scope.launch {
               settingsBottomSheetVM?.dismissCurrent()
            }
         }, onCancel = {
            scope.launch {
               settingsBottomSheetVM?.dismissCurrent()
            }
         }
      )
   }
   
   val mockupState = rememberLazyListState(initialFirstVisibleItemIndex = state.firstVisibleItemIndex, initialFirstVisibleItemScrollOffset = state.firstVisibleItemScrollOffset)
   
   if(
      songs.isEmpty() && AndroidDatabase.isRefreshingSongs
   ){
      // Still loading
      LazyColumn(
         modifier = modifier,
         state = mockupState,
         contentPadding = contentPadding,
         userScrollEnabled = false // disable interactions because this is a preview
      ) {
         
         item(key = "sort_header") {
            SortHeader_Mockup(
               onFilterClick = {}
            )
         }
         
         items(count = state.firstVisibleItemIndex+30) {
            Theme.Glint.GlintBox {
               SongItem_Mockup()
            }
         }
         
      }
   }
   else{
      // Succesful load
      LazyColumn(
         modifier = modifier,
         state = if(songs.isEmpty()) mockupState else state,
         contentPadding = contentPadding
      ) {
         
         item(key = "sort_header") {
            
               SortHeader(
                  songs = songs,
                  onFilterClick = {
                     settingsBottomSheetVM?.addSheet(songsSortContent)
                  }
               )
            
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
               
               // For when initiating a refresh while having songs on the screen scrolled
               if(AndroidDatabase.isRefreshingSongs){
                  // Apply mockup of on-screen songs for a smoother loading experience
                  Overlay(
                     modifier = Modifier,
                     overlay = {
                        Theme.Glint.GlintBox(Modifier.fillMaxSize())
                     }
                  ) {
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
                        optionsMenu = null // TODO
                     )
                  }
                  
               }
               else{
                  // Everything works as intended
                  SongItem(
                     song = song,
                     textColor = textColor,
                     modifier = Modifier
                        .animateItem()
                        .clickable(
                           onClick = {
                              onItemClick.invoke(song)
                           }
                        )
                     ,
                     optionsMenu = rememberSimpleDropdownMenu(
                        listOf() // TODO: add options for songs globally. This shouldn't be hard (make an object with callbacks)
                     ),
                  )
               }
               
            }
         )
      }
   }
   
   
   
}