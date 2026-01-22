package Navigation.Destinations

import ExoPlayer.myExoPlayer
import MyViewModels.ListFeedingVM
import MyViewModels.RefreshState
import Navigation.Destinations.StandaloneLocations.FullscreenPlayer
import Navigation.Destinations.StandaloneLocations.PlaylistAddSongsPage
import Navigation.NavExtensions.safeNavigate
import Presentation.Navigation.MyNavigation
import Presentation.SongsPage.MainSongsColumn
import SrufComposeComponents.CustomPagerTab
import SrufComposeComponents.IconButton
import SrufComposeComponents.KeyboardAwareBox
import SrufComposeComponents.MyModalBottomSheet
import SrufComposeComponents.NestedScrollingFront
import SrufComposeComponents.TabRowIndicator
import SrufComposeComponents.Text
import android.content.SharedPreferences
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.TabPosition
import androidx.compose.material3.TabRow
import androidx.compose.material3.TextField
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.content.edit
import androidx.navigation.NavController
import androidx.navigation.NavHost
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.aacomposetest.Classes.MyPrefs
import com.aacomposetest.Classes.MyPrefs.getCursorValue
import com.aacomposetest.Classes.MyPrefs.putCursorValue
import com.aacomposetest.R
import com.aacomposetest.ui.theme.MyColor
import com.aacomposetest.ui.theme.Theme
import com.composables.core.HorizontalSeparator
import com.composables.core.SheetDetent
import com.composables.core.rememberBottomSheetState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import ui.components.BottomSheets.Content.PlaylistAddOrEdit_Content
import ui.components.BottomSheets.Content.PlaylistDelete_Content
import ui.components.BottomSheets.ResponsivePlayerBS
import ui.components.BottomSheets.rememberBottomSheetConfig
import ui.components.Pages.PlaylistViewing
import ui.components.Pages.PlaylistsPage

@Serializable
object BS_PlayerHolder {
   
   @Serializable
   object BSHolder{
      
      @Composable
      fun invoke(
         startDestination:Any,
         prefs: SharedPreferences,
         globalNavController: NavController,
         refreshBoxState: PullToRefreshState,
         refreshState: RefreshState,
         globalBottomSheetVM: MyModalBottomSheet.ModalVM,
         onClickAddPlaylist:(BS_PlayerHolder.PlaylistViewPage)->Unit,
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
         
         
         
         val localNavController = rememberNavController()
         
         MyNavigation(navController = localNavController, startDestination = startDestination){
            composable<MainPager> {
               val initialPage = remember { prefs.getCursorValue(MyPrefs._prefs_PagerPosition, 0) }
               
               MainPager.invoke(initialPage) { newPage ->
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
                        
                        PlaylistsPage(
                           playlists = allPlaylists,
                           state = playlistsListState,
                           onPlaylistClick = {
                              localNavController.safeNavigate(PlaylistViewPage(it.title))
                           },
                           onPlusClick = {
                              globalBottomSheetVM
                                 .show { isFocusHeld ->
                                    PlaylistAddOrEdit_Content(
                                       allPlaylists,
                                       bottomSheetVM = globalBottomSheetVM,
                                       isFocusHeld = isFocusHeld
                                    )
                                 }
                           },
                           onPlaylistOptionsClick = { playlist, menuIsOpen ->
                              DropdownMenu(
                                 expanded = menuIsOpen.value,
                                 onDismissRequest = {
                                    menuIsOpen.value = false
                                 }
                              ) {
                                 
                                 
                                 DropdownMenuItem(
                                    text = { Text("Delete") },
                                    onClick = {
                                       
                                       globalBottomSheetVM
                                          .show {
                                             PlaylistDelete_Content(
                                                playlist = playlist,
                                                bottomSheetVM = globalBottomSheetVM
                                             )
                                          }
                                       
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
            
            composable<PlaylistViewPage> {
               val args = it.toRoute<PlaylistViewPage>()
               
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
   
   @Serializable
   object MainPager {
      
      @Composable
      fun invoke(initialPage:Int, pageContent:@Composable (page:Int)->Unit) {
         
         val contextRef = LocalContext.current
         val prefs = remember { MyPrefs.getPrefs(contextRef) }
         
         var pagerTitles by remember {
            mutableStateOf(
               listOf(
                  "Songs",
                  "Playlists"
               )
            )
         }
         
         var pagerState = rememberPagerState(
            initialPage = initialPage,
            pageCount = { pagerTitles.size })
         
         
         
         
         // Save current page
         LaunchedEffect(pagerState.currentPage) {
            
            prefs.edit() {
               putInt(
                  MyPrefs._prefs_PagerPosition.name,
                  pagerState.currentPage
               )
               
            }
         }
         
         
         /**
          * Main screen pager. This currently handles the Searchbar, the Tabrow and the Pager's combined scroll
          * */
         val animationScope = rememberCoroutineScope()
         
         val customIndicator =
            @Composable { tabPositions: List<TabPosition> ->
               TabRowIndicator(tabPositions, pagerState, 2.dp)
            }
         NestedScrollingFront(
            appbar = {
               SearchTopBar()
            },
            tabRow = {
               TabRow(
                  modifier = Modifier.wrapContentHeight(),
                  indicator = customIndicator,
                  divider = {
                     HorizontalSeparator(
                        color = MyColor.Inactive,
                        thickness = 2.dp
                     )
                  },
                  selectedTabIndex = pagerState.currentPage
               ) {
                  pagerTitles.forEachIndexed { index, title ->
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
                           animationScope.launch {
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
                     
                     pageContent.invoke(newPage)
                     
                  }
               }
            }
         )
         
         
         // PAGER_END //
         
      }
      @Composable
      private fun SearchTopBar(){
         KeyboardAwareBox(
            modifier = Modifier
               .height(60.dp)
               .fillMaxWidth()
               .background(MyColor.SeparatorGray)
               .clipToBounds()
         ) {isImeVisible,imeInsets,navBarInset->
            
            val searchWord by ListFeedingVM.SortedSongs.searchWordFlow.collectAsState()
            
            val isSearchInProgress by remember {
               derivedStateOf {
                  !searchWord.isBlank()
               }
            }
            
            val focusRequester = remember { FocusRequester() }
            var focusState by remember { mutableStateOf<FocusState?>(null) }
            
            val focusManager = LocalFocusManager.current
            
            var shouldBeFocused by remember { mutableStateOf(false) }
            
            val isFocused by ListFeedingVM.SortedSongs.isSearching.collectAsState()
            
            
            LaunchedEffect(isSearchInProgress,  focusState?.hasFocus, shouldBeFocused) {
               ListFeedingVM.SortedSongs.setSearching(isSearchInProgress || focusState?.hasFocus?:false || shouldBeFocused)
            }
            
            
            LaunchedEffect(isImeVisible) {
               if(!isImeVisible) focusManager.clearFocus()
               shouldBeFocused = false
            }
            
            val iconModifier = Modifier
               .fillMaxHeight()
               .padding(vertical = 10.dp)
               .aspectRatio(1f, true)
            
            if(!isFocused) {
               Text(
                  text = stringResource(R.string.app_name),
                  fontSize = 25.sp,
                  modifier = Modifier
                     .align(Alignment.CenterStart)
                     .padding(horizontal = 5.dp)
               )
               
               IconButton(modifier = iconModifier.align(Alignment.CenterEnd),
                  imageVector = Icons.Default.Search,
                  onclick = {
                     shouldBeFocused = true
                     
                  }
               )
            }
            else {
               
               LaunchedEffect(isFocused) {
                  focusRequester.requestFocus()
               }
               
               TextField(
                  singleLine = true,
                  trailingIcon = {
                     IconButton(
                        modifier = iconModifier,
                        imageVector = Icons.Default.Close,
                        onclick = {
                           focusManager.clearFocus()
                           ListFeedingVM.SortedSongs.setSearchWord("")
                           shouldBeFocused = false
                        }
                     )
                  },
                  modifier = Modifier
                     .fillMaxSize()
                     .focusRequester(focusRequester)
                     .onFocusChanged {
                        focusState = it
                     },
                  value = searchWord,
                  onValueChange = {
                     ListFeedingVM.SortedSongs.setSearchWord(it)
                  },
                  placeholder = {
                     Text(text = "Search")
                  }
               )
            }
            
         }
      }
   }
   
   @Serializable
   data class PlaylistViewPage(val title: String) {
      
      companion object{
         @Composable
         fun invoke(title:String,content:@Composable (title:String)-> Unit){
         
         }
      }
   }
}