package Navigation.Destinations

import ExoPlayer.myExoPlayer
import MyViewModels.ListFeedingVM
import MyViewModels.RefreshState
import Navigation.NavExtensions.safeNavigate
import Presentation.Navigation.MyNavigation
import Presentation.SongsPage.MainSongsColumn
import ui.components.SheetViewmodel
import SrufComposeComponents.Text
import ui.components.rememberBottomSheetInstance
import android.content.SharedPreferences
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.core.content.edit
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.aacomposetest.Classes.MyPrefs
import com.aacomposetest.Classes.getCursorValue
import com.aacomposetest.Classes.putCursorValue
import com.composables.core.SheetDetent
import com.composables.core.rememberBottomSheetState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.serialization.Serializable
import ui.components.BottomSheets.Content.PlaylistAddOrEdit_Content
import ui.components.BottomSheets.Content.PlaylistDelete_Content
import ui.components.BottomSheets.ResponsivePlayerBS
import ui.components.BottomSheets.rememberBottomSheetConfig
import ui.components.Pages.PlaylistViewing
import ui.components.Pages.PlaylistsPage

@Serializable
object PlayerBottomSheet_Frame {
   
   @Composable
   fun invoke(
      startDestination:Any,
      prefs: SharedPreferences,
      localNavController: NavHostController,
      refreshBoxState: PullToRefreshState,
      refreshState: RefreshState,
      globalBottomSheetVM: SheetViewmodel,
      onClickAddPlaylist:(Page_ViewPlaylist)->Unit,
      onBottomSheetClick:()->Unit
   ){
      val songBS_Config = rememberBottomSheetConfig()
      
      val songPlayerBS_State = rememberBottomSheetState(
         initialDetent = songBS_Config.peekDetent?: SheetDetent.Hidden,
         detents = songBS_Config.detents
      )
      
      val songsListState = rememberSaveable(saver = LazyListState.Saver) {
         LazyListState(
            firstVisibleItemIndex = prefs.getInt(
               MyPrefs._prefs_songListstate_FirstItem.name,
               0
            ),
            firstVisibleItemScrollOffset = prefs.getInt(
               MyPrefs._prefs_songListState_Offset.name,
               0
            )
         )
      }
      val playlistsListState = rememberSaveable(saver = LazyListState.Saver) {
         LazyListState(
            firstVisibleItemIndex = prefs.getInt(
               MyPrefs._prefs_playlistsListState_FirstItem.name,
               0
            ),
            firstVisibleItemScrollOffset = prefs.getInt(
               MyPrefs._prefs_playlistsListState_Offset.name,
               0
            )
         )
      }
      
      val allPlaylists by ListFeedingVM.SortedPlaylists.filteredPlaylists.collectAsState(
         emptyList()
      )
      
      // Save scroll position for the [All Songs] page
      LaunchedEffect(songsListState) {
         snapshotFlow {
            songsListState.firstVisibleItemIndex to songsListState.firstVisibleItemScrollOffset
         }
            .debounce(500L)
            .collectLatest { (firstItem, offset) ->
               prefs.edit {
                  putCursorValue(
                     MyPrefs._prefs_songListstate_FirstItem,
                     firstItem
                  )
                  putCursorValue(MyPrefs._prefs_songListState_Offset, offset)
               }
            }
      }
      // Save scroll position for the [All Playlists] page
      LaunchedEffect(playlistsListState) {
         snapshotFlow {
            playlistsListState.firstVisibleItemIndex to playlistsListState.firstVisibleItemScrollOffset
         }
            .debounce(500L)
            .collectLatest { (firstItem, offset) ->
               prefs.edit {
                  putInt(
                     MyPrefs._prefs_playlistsListState_FirstItem.name,
                     firstItem
                  )
                  putInt(MyPrefs._prefs_playlistsListState_Offset.name, offset)
               }
            }
      }
      
      
      
      
      
      MyNavigation(navController = localNavController, startDestination = startDestination){
         composable<MainPager> {
            val initialPage = remember { prefs.getCursorValue(MyPrefs._prefs_PagerPosition, "") }
            
            MainPager.invoke(initialPage.toIntOrNull()) { newPage ->
               when (newPage) {
                  
                  // Main songs page
                  0 -> {
                     
                     PullToRefreshBox(
                        state = refreshBoxState,
                        modifier = Modifier
                           .fillMaxSize()
                           .padding(bottom = songBS_Config.bottomHeight),
                        isRefreshing = refreshState.state,
                        onRefresh = {
                           refreshState.state = true
                           refreshState.onRefresh.invoke()
                        }
                     ) {
                        
                        
                        MainSongsColumn(
                           modifier = Modifier.fillMaxSize(),
                           settingsBottomSheetVM = globalBottomSheetVM,
                           state = songsListState
                        )
                     }
                  }
                  
                  // Playlists page
                  1 -> {
                     // State yet unused for the playlist viewing dropdown menu.
                     val playlistsPageMoreMenuState by remember {
                        mutableStateOf(
                           false
                        )
                     }
                     
                     val plusClickContent = rememberBottomSheetInstance {
                        PlaylistAddOrEdit_Content(
                           allPlaylists,
                           bottomSheetVM = globalBottomSheetVM
                        )
                     }
                     
                     PlaylistsPage(
                        playlists = allPlaylists,
                        state = playlistsListState,
                        onPlaylistClick = {
                           localNavController.safeNavigate(Page_ViewPlaylist(it.title))
                        },
                        onPlusClick = {
                           globalBottomSheetVM
                              .addSheet(plusClickContent)
                        },
                        onPlaylistOptionsClick = { playlist, menuIsOpen ->
                           DropdownMenu(
                              expanded = menuIsOpen.value,
                              onDismissRequest = {
                                 menuIsOpen.value = false
                              }
                           ) {
                              
                              val deleteItemContent = rememberBottomSheetInstance {
                                 PlaylistDelete_Content(
                                    playlist = playlist,
                                    bottomSheetVM = globalBottomSheetVM
                                 )
                              }
                              
                              DropdownMenuItem(
                                 text = { Text("Delete") },
                                 onClick = {
                                    
                                    globalBottomSheetVM
                                       .addSheet (deleteItemContent)
                                    
                                    menuIsOpen.value = false
                                 }
                              )
                              
                           }
                           
                        }
                     )
                  }
               }
               
               
            }
         }
         
         composable<Page_ViewPlaylist> {
            val args = it.toRoute<Page_ViewPlaylist>()
            
            val playlistViewingLazyListState =
               rememberSaveable(saver = LazyListState.Saver) { LazyListState() }
            
            
            
            PlaylistViewing(
               playlistTitle = args.title,
               lazyListState = playlistViewingLazyListState,
               onBack = {
                  localNavController.popBackStack()
               },
               bottomPadding = songBS_Config.bottomHeight,
               plusButtonClick = {
                  onClickAddPlaylist.invoke(args)
               },
            )
            
            
         }
         
         
      }
      
      if (songBS_Config.alpha.value > 0f) {
         val curSong by myExoPlayer.currentlyPlayingSong.collectAsState()
         ResponsivePlayerBS(
            modifier = Modifier.alpha(songBS_Config.alpha.value),
            state = songPlayerBS_State,
            currentSong = curSong,
            onBackgroundClick = {
               
               onBottomSheetClick.invoke()
               
               
            }
         )
         
         
      }
   }
}