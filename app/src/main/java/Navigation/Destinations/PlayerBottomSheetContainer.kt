package Navigation.Destinations

import ExoPlayer.myExoPlayer
import MyViewModels.ListFeedingVM
import MyViewModels.RefreshState
import Navigation.NavExtensions.safeNavigate
import Presentation.Navigation.MyNavigation
import Presentation.SongsPage.MainSongsColumn
import MyViewModels.SheetViewmodel
import android.app.Activity
import MyViewModels.rememberBottomSheetInstance
import android.content.SharedPreferences
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.core.content.edit
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.aacomposetest.Classes.ActivityLifecycleCallbacks_Open
import com.aacomposetest.Classes.ActivityLifecycleEffect
import com.aacomposetest.Classes.MyPrefs
import com.aacomposetest.Classes.PersistentState
import com.aacomposetest.Classes.accessCursorValue
import com.aacomposetest.Classes.getCursorValue
import com.aacomposetest.Classes.putCursorValue
import com.composables.core.SheetDetent
import com.composables.core.rememberBottomSheetState
import kotlinx.coroutines.flow.debounce
import kotlinx.serialization.Serializable
import ui.components.BottomSheets.Content.PlaylistAddOrEdit_Content
import ui.components.BottomSheets.ResponsivePlayerBS
import ui.components.BottomSheets.rememberBottomSheetConfig
import ui.components.Pages.PlaylistsPage

@Serializable
object PlayerBottomSheetContainer {
   
   @Composable
   fun RenderBottomSheetContainer(
      startDestination:Any,
      prefs: SharedPreferences,
      localNavController: NavHostController,
      refreshBoxState: PullToRefreshState,
      refreshState: RefreshState,
      globalBottomSheetVM: SheetViewmodel,
      onClickAddPlaylist:(Page_ViewPlaylist)->Unit,
      onBottomSheetClick:()->Unit
   ){
      var isBS_ExpandedByDefault by remember{ PersistentState(
         key = PersistentState.GlobalStates.playerBottomSheetIsExpandedByDefault,
         defaultFactory = {false})
      }
      
      val songBS_Config = rememberBottomSheetConfig(
         isExpandedByDefault = isBS_ExpandedByDefault
      )
      
      val songPlayerBS_State = rememberBottomSheetState(
         initialDetent = songBS_Config.resolveInitialState(),
         detents = songBS_Config.detents,
         confirmDetentChange = {
            isBS_ExpandedByDefault = it == SheetDetent.FullyExpanded
            true
         }
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
      
      
      
      /**
       * Save scroll position for the [All Songs] page
       *
       * * Note: *
       *
       * Is already debounced by the lifecycle!
       * */
      ActivityLifecycleEffect(
         key = songsListState,
         callbacks = object : ActivityLifecycleCallbacks_Open(){
            override fun onActivityPaused(activity: Activity) {
               prefs.edit(true) {
                  putCursorValue(
                     MyPrefs._prefs_songListstate_FirstItem,
                     songsListState.firstVisibleItemIndex
                  )
                  putCursorValue(
                     MyPrefs._prefs_songListState_Offset,
                     songsListState.firstVisibleItemScrollOffset
                  )
               }
            }
         }
      )
      
      // Save scroll position for the [All Playlists] page
      ActivityLifecycleEffect(
         key = playlistsListState,
         callbacks = object : ActivityLifecycleCallbacks_Open(){
            override fun onActivityPaused(activity: Activity) {
               prefs.edit(true) {
                  putCursorValue(
                     MyPrefs._prefs_playlistsListState_FirstItem,
                     playlistsListState.firstVisibleItemIndex
                  )
                  putCursorValue(
                     MyPrefs._prefs_playlistsListState_Offset,
                     playlistsListState.firstVisibleItemScrollOffset
                  )
               }
            }
         }
      )
      
      
      
      
      MyNavigation(navController = localNavController, startDestination = startDestination){
         composable<MainPager> {
            val initialPage = remember { prefs.getCursorValue(MyPrefs._prefs_PagerPosition, "") }
            
            MainPager.RenderPager(initialPage.toIntOrNull()) { newPage ->
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
                        globalBottomSheetViewModel = globalBottomSheetVM
                     )
                  }
               }
               
               
            }
         }
         
         composable<Page_ViewPlaylist> {
            val args = it.toRoute<Page_ViewPlaylist>()
            
            var persistentIndex by remember{
               PersistentState(
                  key = PersistentState.GlobalStates.lastAccessedPlaylistIndex,
                  defaultFactory = {
                     0
                  }
               )
            }
            var persistentOffset by remember{
               PersistentState(
                  key = PersistentState.GlobalStates.lastAccessedPlaylistOffset,
                  defaultFactory = {
                     0
                  }
               )
            }
            
            
            var lastViewedPlaylist by remember {
               PersistentState(
                  key = PersistentState.GlobalStates.lastAccessedPlaylist,
                  defaultFactory = {
                     args.title
                  }
               ).apply {
                  if(this.value != args.title){
                     persistentIndex = 0
                     persistentOffset = 0
                     this.value = args.title
                  }
               }
            }
            
            
            
            val playlistViewingLazyListState = rememberLazyListState(persistentIndex,persistentOffset)
            
            LaunchedEffect(Unit) {
               snapshotFlow {
                  playlistViewingLazyListState.firstVisibleItemIndex to playlistViewingLazyListState.firstVisibleItemScrollOffset
               }.debounce { 500 }
                  .collect {
                     persistentIndex = it.first
                     persistentOffset = it.second
                  }
            }
            
            
            Page_ViewPlaylist.Page(
               playlistTitle = args.title,
               lazyListState = playlistViewingLazyListState,
               onBack = {
                  localNavController.popBackStack()
               },
               bottomPadding = songBS_Config.bottomHeight,
               onPlusClick = {
                  onClickAddPlaylist.invoke(args)
               },
               optionsMenu = null // TODO
            )
            
            
         }
         
         
      }
      
      if (songBS_Config.alpha.value > 0f) {
         val curSong by myExoPlayer.currentlyPlayingSong.stateFlow.collectAsState()
         
         if(curSong!=null)
            ResponsivePlayerBS(
               modifier = Modifier
                  .graphicsLayer {
                     alpha = songBS_Config.alpha.value
                  },
               state = songPlayerBS_State,
               currentSong = curSong,
               onBackgroundClick = {
                  onBottomSheetClick.invoke()
               }
            )
         
         
      }
   }
}