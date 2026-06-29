package Navigation.Destinations

import ExoPlayer.myExoPlayer
import MyViewModels.ListFeedingVM
import MyViewModels.RefreshState
import Navigation.NavExtensions.safeNavigate
import Presentation.Navigation.MyNavigation
import Presentation.SongsPage.MainSongsColumn
import MyViewModels.SheetViewmodel
import MyViewModels.SongsViewModel
import android.app.Activity
import MyViewModels.rememberBottomSheetInstance
import Navigation.Destinations.MainPager.MainPagerState
import Navigation.Destinations.MainPager.SearchTopBar
import Presentation.NestedScrollingFront
import SrufComposeComponents.CustomPagerTab
import SrufComposeComponents.Text
import android.content.SharedPreferences
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.TabRow
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.content.edit
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.aacomposetest.Classes.ActivityLifecycleCallbacks_Open
import com.aacomposetest.Classes.ActivityLifecycleEffect
import com.aacomposetest.Classes.MyPrefs
import com.aacomposetest.Classes.PersistentState
import com.aacomposetest.Classes.Setting
import com.aacomposetest.Classes.getCursorValue
import com.aacomposetest.Classes.putCursorValue
import com.aacomposetest.MyApplication
import com.aacomposetest.ui.theme.MyColor
import com.aacomposetest.ui.theme.Theme
import com.composables.core.HorizontalSeparator
import com.composables.core.SheetDetent
import com.composables.core.rememberBottomSheetState
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import ui.components.BottomSheets.Content.PlaylistAddOrEdit_Content
import ui.components.BottomSheets.ResponsivePlayerBS
import ui.components.BottomSheets.rememberBottomSheetConfig
import ui.components.Pages.PlaylistsPage
import ui.components.ViewDrawerState

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
      drawerState: ViewDrawerState,
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
      
      
      val scope = rememberCoroutineScope()
      
      MyNavigation(navController = localNavController, startDestination = startDestination){
         composable<MainPager> {
            val initialPage = remember { prefs.getCursorValue(MyPrefs._prefs_PagerPosition, "") }
            
            val contextRef = LocalContext.current
            val prefs = remember { MyPrefs.getPrefs(contextRef, MyPrefs.PrefDirs.VisualData) }
            
            val pageState = remember { MainPagerState().apply {
               initialPage.toIntOrNull()?.let {
                  this.initialPage = it
               }
            }}
            
            
            var pagerState = rememberPagerState(
               initialPage = pageState.initialPage,
               pageCount = { pageState.pages.size }
            )
            DisposableEffect(pagerState) {
               PrimaryNavigator.updatePagerState(pagerState)
               onDispose {
                  PrimaryNavigator.disposePagerState()
               }
            }
            
            
            
            
            // Save current page
            LaunchedEffect(pagerState.currentPage) {
               
               pageState.initialPage = pagerState.currentPage
               
               prefs.edit() {
                  putCursorValue(MyPrefs._prefs_PagerPosition,pagerState.currentPage.toString())
               }
            }
            
            
            NestedScrollingFront(
               appbar = {
                  SearchTopBar(drawerState)
               },
               tabRow = {
                  TabRow(
                     modifier = Modifier.wrapContentHeight(),
                     indicator = {
                        customIndicator.invoke(it,pagerState)
                     },
                     divider = {
                        HorizontalSeparator(
                           color = MyColor.Inactive,
                           thickness = 2.dp
                        )
                     },
                     selectedTabIndex = pagerState.currentPage,
                     containerColor = Theme.Sourcesets.TabRowBackground()
                  ) {
                     pageState.pages.forEachIndexed { index, title ->
                        val interactionSource =
                           remember { MutableInteractionSource() }
                        
                        CustomPagerTab(
                           modifier = Modifier
                              .height(40.dp)
                              .clickable(
                                 onClick = {},
                                 interactionSource = interactionSource,
                                 indication = null
                              ),
                           unselectedContentColor = Theme.colorScheme().surfaceDim,
                           selectedContentColor = Theme.colorScheme().outline,
                           selected = pagerState.currentPage == index,
                           onClick = {
                              scope.launch {
                                 pagerState.animateScrollToPage(index)
                              }
                           }, text = {
                              Text(
                                 text = title,
                                 fontSize = 16.sp,
                                 modifier = Modifier.zIndex(2f)
                              )
                           }
                        )
                     }
                  }
               },
               pager = {
                  HorizontalPager(
                     state = pagerState,
                     modifier = Modifier.fillMaxSize()
                  ) { newPage ->
                     Box(
                        modifier = Modifier.fillMaxSize()
                     ) {
                        
                        val curPage = remember(newPage) { MainPages.parse(newPage) }
                        
                        when (curPage) {
                           // Main songs page
                           MainPages.Home -> {
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
                                 
                                 val songs by ListFeedingVM.SortedSongs.sortedAndFilteredSongsFlow.collectAsState()
                                 
                                 DisposableEffect(songsListState,songs) {
                                    
                                    PrimaryNavigator.updateMainPageController(songsListState,songs.map { it.id })
                                    onDispose {
                                       PrimaryNavigator.disposeMainPageController()
                                    }
                                 }
                                 
                                 MainSongsColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    settingsBottomSheetVM = globalBottomSheetVM,
                                    state = songsListState,
                                    songs = songs
                                 )
                              }
                           }
                           
                           // Playlists page
                           MainPages.Playlists -> {
                              
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
                                    scope.launch {
                                       PrimaryNavigator.navigateToAPlaylist(it.title,localNavController)
                                    }
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
               },
               cursorValue = MyPrefs._prefs_songListState_NestedScrollOffset
            )
            
               
               
            
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
               globalBottomSheet = globalBottomSheetVM,
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
               },
               gotoSong = {
                  myExoPlayer.currentlyPlayingSong.getValue?.id?.let {id->
                     scope.launch {
                        PrimaryNavigator.navigateToSong(id, myExoPlayer.curPlaylistVM.playlist.value?.title,localNavController)
                        
                     }
                  }
               }
            )
         
         
      }
   }
}

enum class MainPages{
   Home,
   Playlists;
   fun toInt(): Int {
      return MainPages.toInt(this)
   }
   companion object {
      fun toInt(page: MainPages): Int {
         return when(page){
            MainPages.Home -> 0
            MainPages.Playlists -> 1
         }
      }
      fun parse(page: Int): MainPages {
         return when (page) {
            0 -> {
               Home
            }
            
            1 -> {
               Playlists
            }
            
            else -> {
               Home
            }
         }
      }
   }
}

object PrimaryNavigator{
   private val songVM: SongsViewModel
      get() {
         return MyApplication.instance.songViewModel
      }
   
   private var _playlistLazyList: LazyListState? = null
   private var _playlistList:List<Long>? =  null
   fun updatePlaylistPageController(value: LazyListState,list:List<Long>){
      _playlistLazyList = value
      _playlistList = list
   }
   fun disposePlaylistPageController(){
      _playlistLazyList = null
      _playlistList = null
   }
   private var _mainLazyList: LazyListState? = null
   private var _mainList:List<Long>? =  null
   fun updateMainPageController(value: LazyListState,list:List<Long>){
      _mainLazyList = value
      _mainList = list
   }
   fun disposeMainPageController(){
      _mainLazyList = null
      _mainList = null
   }
   private var _pagerState:PagerState? = null
   fun updatePagerState(value: PagerState){
      _pagerState = value
   }
   fun disposePagerState(){
      _pagerState = null
   }
   
   
   suspend fun navigateToHome(navigator: NavHostController){
      
      if(navigator.currentDestination == Page_ViewPlaylist){
         navigator.popBackStack()
      }
      
      if(navigator.currentDestination != MainPager){
         navigator.safeNavigate(MainPager)
      }
      _pagerState?.run {
         if(MainPages.parse(this.currentPage)!= MainPages.Home){
            
               this@run.animateScrollToPage(MainPages.Home.toInt())
            
         }
      }
      
   }
   suspend fun navigateToPlaylists(navigator: NavHostController){
      
      if(navigator.currentDestination == Page_ViewPlaylist){
         navigator.popBackStack()
      }
      
      if(navigator.currentDestination != MainPager){
         navigator.safeNavigate(MainPager)
      }
      _pagerState?.run {
         if (MainPages.parse(this.currentPage) != MainPages.Playlists) {
            
               this@run.animateScrollToPage(MainPages.Playlists.toInt())
            
         }
      }
   }
   suspend fun navigateToAPlaylist(title:String,navigator: NavHostController){
      navigateToPlaylists(navigator)
      
      navigator.safeNavigate(Page_ViewPlaylist(title))
   }
   
   suspend fun navigateToSong(id:Long,playlist:String?,navigator: NavHostController) {
      
         if (playlist != null) {
            // Song is in a playlist
            navigateToAPlaylist(playlist, navigator)
            //TODO: handle hidden songs too later when i add that. Then i will need to refactor this to use flows
            _playlistList?.let { _playlistLazyList?.scrollToItem(it.indexOf(id)) }
         } else {
            // song is not in playlist
            navigateToHome(navigator)
            _mainList?.let {
               val index = it.indexOf(id)
               _mainLazyList?.scrollToItem(index)
            }
         
      }
   }
   
   
   
}