package Navigation.Destinations

import AppDatabase.SongRelated.Song
import ExoPlayer.ExoPlayerViewModel
import ExoPlayer.myExoPlayer
import MyViewModels.ListFeedingVM
import MyViewModels.PlaylistWatcher
import MyViewModels.RefreshState
import MyViewModels.SheetViewmodel
import MyViewModels.rememberBottomSheetInstance
import ui.components.CustomIndicator
import Presentation.Navigation.MyNavigation
import Presentation.NestedScrollingFront
import Presentation.SongsPage.MainSongsColumn
import Settings.MySettings
import Settings.SettingsRepository
import SrufComposeComponents.CustomPagerTab
import SrufComposeComponents.IconButton
import SrufComposeComponents.Text
import android.app.Activity
import android.content.SharedPreferences
import android.os.Parcelable
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.TabRow
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.content.edit
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.composables.core.HorizontalSeparator
import com.composables.core.SheetDetent
import com.composables.core.rememberBottomSheetState
import com.musichub.Classes.ActivityLifecycleCallbacks_Open
import com.musichub.Classes.ActivityLifecycleEffect
import com.musichub.Classes.MyIcons
import com.musichub.Classes.MyPrefs
import com.musichub.Classes.PersistentState
import com.musichub.Classes.SimpleDropdownMenuState
import com.musichub.Classes.getCursorValue
import com.musichub.Classes.putCursorValue
import com.musichub.Classes.rememberSimpleDropdownMenu
import com.musichub.MyApplication
import com.musichub.R
import com.musichub.Toast
import com.musichub.mapByOrderOf
import com.musichub.ui.theme.MyColor
import com.musichub.ui.theme.Theme
import com.musichub.ui.theme.isSystemInDarkTheme
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import ui.components.BottomSheets.Content.BottomSheet_SongData
import ui.components.BottomSheets.Content.PlaylistAddOrEdit_Content
import ui.components.BottomSheets.ResponsivePlayerBS
import ui.components.BottomSheets.rememberBottomSheetConfig
import ui.components.Overlay
import ui.components.Pages.PlaylistsPage
import ui.components.SongItem
import ui.components.Text.OutlinedTextFieldExpanded
import ui.components.ViewDrawerState
import kotlin.reflect.KClass

@Parcelize
@Serializable
sealed interface Destination : Parcelable {
   
   val routeClass: KClass<*>
   
   fun onNavigateTo(navController: NavHostController)
   /**
    * Main refers to the PlayerBottomSheetContainer
    * */
   sealed interface PlayerContainer{
      @Parcelize @Serializable
      data object MainPager :PlayerContainer, Destination {
         override val routeClass: KClass<*>
            get() = MainPager::class
         
         override fun onNavigateTo(navController: NavHostController) {
            navController.navigate(this) {
               popUpTo(MainPager) { inclusive = false }
               launchSingleTop = true
               restoreState = true
            }
         }
         
         @Composable
         fun SearchTopBar(drawerState: ViewDrawerState){
            Box(
               modifier = Modifier
                  .height(60.dp)
                  .fillMaxWidth()
                  .background(Theme.Sourcesets.AppbarBackground())
                  .clipToBounds()
            ) {
               
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
                  ListFeedingVM.SortedSongs.setSearching(isSearchInProgress || focusState?.hasFocus == true || shouldBeFocused)
               }
               
               
               val iconModifier = Modifier
                  .fillMaxHeight()
                  .padding(vertical = 10.dp)
                  .aspectRatio(1f, true)
               
               if(!isFocused) {
                  Row(
                     modifier = Modifier.align(Alignment.CenterStart),
                     verticalAlignment = Alignment.CenterVertically
                  ) {
                     IconButton(
                        modifier = iconModifier,
                        resId = R.drawable.options,
                     ){
                        drawerState.open()
                     }
                     Text(
                        text = stringResource(R.string.app_name),
                        fontSize = 25.sp,
                        modifier = Modifier.padding(horizontal = 5.dp)
                     )
                  }
                  
                  
                  IconButton(modifier = iconModifier.align(Alignment.CenterEnd),
                     imageVector = Icons.Default.Search,
                     onClick = {
                        shouldBeFocused = true
                        
                     }
                  )
               }
               else {
                  
                  LaunchedEffect(isFocused) {
                     focusRequester.requestFocus()
                  }
                  OutlinedTextFieldExpanded(
                     singleLine = true,
                     value = searchWord,
                     onValueChange = {
                        ListFeedingVM.SortedSongs.setSearchWord(it)
                     },
                     modifier = Modifier
                        .fillMaxSize()
                        .focusRequester(focusRequester)
                        .onFocusChanged {
                           focusState = it
                        },
                     placeholder = @Composable {
                        Text("Search")
                     },
                     trailingIcon = @Composable {
                        IconButton(
                           modifier = iconModifier,
                           imageVector = Icons.Default.Close,
                           onClick = {
                              focusManager.clearFocus()
                              ListFeedingVM.SortedSongs.setSearchWord("")
                              shouldBeFocused = false
                           }
                        )
                     }, textStyle = TextStyle(color = MyColor.TextColor())
                  )
                  
               }
               
            }
         }
         
         class MainPagerState(){
            val pages = listOf(
               "Songs",
               "Playlists"
            )
            
            var initialPage by PersistentState(
               key = PersistentState.GlobalStates.mainPagerSavedPage,
               defaultFactory = { 0 }
            )
         }
      }
      @Parcelize @Serializable
      data class PlaylistViewRoute(val title: String) :PlayerContainer, Destination{
         
         override val routeClass: KClass<*>
            get() = PlaylistViewRoute::class
         
         override fun onNavigateTo(navController: NavHostController) {
            navController.navigate(this){
               launchSingleTop = true
               restoreState = true
            }
         }
         companion object{
            @Composable
            fun Page(
               modifier:Modifier = Modifier.fillMaxSize(),
               lazyListState: LazyListState,
               playlistTitle: String,
               onBack:(()-> Unit)? = null,
               bottomPadding: Dp = 0.dp,
               thumbNailSize:Dp = 150.dp,
               onPlusClick:(()-> Unit)? =null,
               globalBottomSheet: SheetViewmodel,
               optionsMenu: SimpleDropdownMenuState? = null
            ) {
               Theme.RenderDefaultBackground()
               
               /**
                * Unused for now because Navigation is taking care of it.
                * */
               BackHandler(onBack!=null) {
                  onBack?.invoke()
               }
               
               
               var isSearching by rememberSaveable { mutableStateOf(false) }
               var searchWord by rememberSaveable { mutableStateOf("") }
               
               val contextRef = LocalContext.current
               
               val allSongs by ListFeedingVM.allSongs.collectAsState()
               
               
               /** Holds the playlist that is shown on this screen. */
               val onScreenPlaylistVM = remember {
                  PlaylistWatcher(
                     fileWatcher = MyApplication.playlistFolderWatcher
                  )
               }
               
               val playlist by onScreenPlaylistVM.playlist.collectAsState()
               
               val currentlyPlayingSong by myExoPlayer.currentlyPlayingSong.stateFlow.collectAsState()
               
               
               val const_PlaylistSongs by remember {
                  derivedStateOf {
                     // Searching is applied here
                     allSongs.mapByOrderOf(playlist?.songsIdList ?: emptyList()) { it.id }.filter { it.title.lowercase().contains(searchWord.lowercase()) }
                     
                  }
               }
               
               var playlistSongs by remember {
                  mutableStateOf(
                     allSongs.filter {
                        playlist?.songsIdList?.contains(it.id) == true
                     }.filter {
                        it.title.contains(searchWord)
                     }
                  )
               }
               
               // Get the playlist
               DisposableEffect(Unit) {
                  
                  /**
                   * Set current playlist. Listener is added and removed automatically on ViewModel creation and destruction.
                   * */
                  onScreenPlaylistVM.changePlaylist(playlistTitle,contextRef)
                  
                  onDispose {
                     /**
                      * Saves on decomposition.
                      * */
                     playlist?.save(contextRef)
                  }
               }
               
               
               
               
               
               
               
               
               
               /**
                * Update the list of songs like this if the real list updates because [playlistSongs] can be reordered
                * */
               LaunchedEffect(const_PlaylistSongs) {
                  
                  playlistSongs = const_PlaylistSongs
               }
               
               /**
                * State for the reorderable list.
                * */
               val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
                  playlistSongs = playlistSongs.toMutableList().let {
                     it.add(to.index-2, it.removeAt(from.index-2))
                     it
                  }
               }
               
               var songToSendToInfo by remember { mutableStateOf<Song?>(null) }
               val datasheet = rememberBottomSheetInstance {
                  BottomSheet_SongData(songToSendToInfo)
               }
               
               
               
               Box(modifier = modifier) {
                  
                  
                  val sHeaderTitle by remember {
                     derivedStateOf {
                        playlist?.title?.let { it + " (${playlistSongs.size})" } ?: "Error"
                     }
                  }
                  
                  val removeItemOnClickListener: (Song) -> Unit = remember {
                     { song ->
                        playlistSongs =
                           playlistSongs.toMutableList().let {
                              it.removeIf { it.id == song.id }
                              
                              it
                           }
                        if (myExoPlayer.currentlyPlayingSong.getValue?.id == song.id) {
                           myExoPlayer.setCurrentPlaylist(null)
                        }
                        playlist?.songsIdList?.clear()
                        playlist?.songsIdList?.addAll(playlistSongs.map { it.id })
                        
                        playlist?.save(contextRef)
                     }
                  }
                  if(isSearching) {
                     PlaylistSearchBar(
                        modifier = Modifier.zIndex(1f),
                        isSearching = isSearching,
                        searchWord = searchWord,
                        onSearchCanceled = {
                           searchWord = ""
                           isSearching = false
                        },
                        onSearchWordChanged = {
                           searchWord = it
                        },
                     )
                  }
                  
                  LazyColumn(
                     modifier = Modifier
                        .fillMaxSize(),
                     state = if (playlistSongs.isEmpty()) rememberLazyListState() else lazyListState,
                     contentPadding = PaddingValues(bottom = bottomPadding)
                  ) {
                     
                     
                     item(key = "PlaylistIcon") {
                        Box(
                           modifier = Modifier
                              .fillMaxWidth()
                              .wrapContentHeight()
                        ) {
                           Icon(
                              painter = painterResource(
                                 playlist?.thumbNail ?: R.drawable.exclamation_sign
                              ),
                              contentDescription = null,
                              tint = if (isSystemInDarkTheme()) Color.White else Color.Black,
                              modifier = Modifier
                                 .size(thumbNailSize)
                                 .align(Alignment.Center)
                                 .padding(20.dp)
                           )
                        }
                        
                     }
                     
                     
                     stickyHeader(key = "PlaylistStickyBar") {
                        Box(
                           modifier = Modifier
                              .fillMaxWidth()
                              .wrapContentHeight()
                              .background(Theme.Sourcesets.ToolbarBackground()),
                        ) {
                           
                           Text(
                              text = sHeaderTitle,
                              modifier = Modifier
                                 .align(Alignment.CenterStart)
                                 .padding(horizontal = 30.dp)
                           )
                           
                           Row(
                              modifier = Modifier.align(Alignment.BottomEnd),
                              verticalAlignment = Alignment.CenterVertically
                           ) {
                              
                              IconButton(
                                 imageVector = Icons.Default.Search,
                                 iconScale = Size(0.75f, 0.75f),
                                 modifier = Modifier.size(MyIcons.sizeMedium, MyIcons.sizeMedium)
                              ){
                                 isSearching = true
                              }
                              
                              // PLUS ICON
                              IconButton(
                                 resId = R.drawable.ic_add,
                                 iconScale = Size(0.75f, 0.75f),
                                 modifier = Modifier.size(MyIcons.sizeMedium, MyIcons.sizeMedium)
                              ) {
                                 onPlusClick?.invoke()
                              }
                              
                              // More options button
                              Box(
                                 contentAlignment = Alignment.Center
                              ) {
                                 
                                 IconButton(
                                    imageVector = Icons.Default.MoreVert,
                                    modifier = Modifier.size(MyIcons.sizeMedium, MyIcons.sizeMedium),
                                    iconScale = Size(0.75f, 0.75f)
                                 ) {
                                    optionsMenu?.open()
                                 }
                                 
                                 // Show the menu, hence a Composable
                                 optionsMenu?.RenderMenu()
                              }
                           }
                           
                           
                        }
                     }
                     
                     
                     items(
                        items = playlistSongs,
                        key = { it.id }
                     ) { song ->
                        
                        val textColor = if (currentlyPlayingSong?.id == song.id) {
                           MyColor.SelectedGold
                        } else {
                           MyColor.TextColor()
                        }
                        
                        
                        
                        ReorderableItem(reorderableLazyListState, key = song.id) { isDragging ->
                           val elevation by animateDpAsState(if (isDragging) 4.dp else 0.dp)
                           
                           Surface(shadowElevation = elevation, color = MyColor.Transparent) {
                              SongItem(
                                 song = song,
                                 textColor = textColor,
                                 modifier = Modifier
                                    .background(if (isDragging) MyColor.SemiTransparentDark else MyColor.Transparent)
                                    .clickable(
                                       onClick = {
                                          
                                          ExoPlayerViewModel.setSong(
                                             song = song,
                                             startOnPrepared = true,
                                             playlistTitle = playlist?.title,
                                          )
                                       }
                                    )
                                    .animateItem(),
                                 leftmostContent = {
                                    if (!isSearching) {
                                       IconButton(
                                          resId = R.drawable.ic_drag_handle,
                                          modifier = Modifier
                                             .fillMaxHeight(0.8f)
                                             .aspectRatio(1f, true)
                                             .draggableHandle(
                                                onDragStopped = {
                                                   
                                                   val auxList = playlistSongs.map { it -> it.id }
                                                   
                                                   playlist?.songsIdList?.clear()
                                                   
                                                   playlist?.songsIdList?.addAll(auxList)
                                                   
                                                   playlist?.save(contextRef)
                                                   
                                                   Toast(
                                                      contextRef,
                                                      "Playlist saved!",
                                                      Toast.LENGTH_SHORT
                                                   )
                                                }
                                             ),
                                          ripple = null,
                                          onClick = {
                                          
                                          }
                                       )
                                       
                                    }
                                 },
                                 optionsMenu = rememberSimpleDropdownMenu(
                                    listOf(
                                       "Remove" to {
                                          removeItemOnClickListener.invoke(song)
                                       },
                                       "Info" to {
                                          songToSendToInfo = song
                                          globalBottomSheet.addSheet(datasheet)
                                       }
                                    )
                                 )
                              )
                           }
                        }
                        
                     }
                     
                  }
               }
               
               
            }
            
            @Composable
            fun PlaylistSearchBar(
               modifier:Modifier = Modifier,
               isSearching:Boolean,
               searchWord: String,
               onSearchCanceled:()-> Unit,
               onSearchWordChanged:(String)->Unit,
            ){
               Box(
                  modifier = modifier
                     .height(60.dp)
                     .fillMaxWidth()
                     .background(Theme.Sourcesets.AppbarBackground())
                     .clipToBounds()
               ) {
                  
                  val isSearchInProgress by remember {
                     derivedStateOf {
                        !searchWord.isBlank()
                     }
                  }
                  
                  val focusRequester = remember { FocusRequester() }
                  var focusState by remember { mutableStateOf<FocusState?>(null) }
                  
                  val focusManager = LocalFocusManager.current
                  
                  var shouldBeFocused by remember { mutableStateOf(false) }
                  
                  LaunchedEffect(isSearchInProgress,  focusState?.hasFocus, shouldBeFocused) {
                     ListFeedingVM.SortedSongs.setSearching(isSearchInProgress || focusState?.hasFocus == true || shouldBeFocused)
                  }
                  
                  
                  val iconModifier = remember {
                     Modifier
                        .fillMaxHeight()
                        .padding(vertical = 10.dp)
                        .aspectRatio(1f, true)
                  }
                  
                  
                  
                  LaunchedEffect(isSearching) {
                     focusRequester.requestFocus()
                  }
                  OutlinedTextFieldExpanded(
                     singleLine = true,
                     value = searchWord,
                     onValueChange = {
                        onSearchWordChanged.invoke(it)
                     },
                     modifier = Modifier
                        .fillMaxSize()
                        .focusRequester(focusRequester)
                        .onFocusChanged {
                           focusState = it
                        },
                     placeholder = @Composable {
                        Text("Search")
                     },
                     trailingIcon = @Composable {
                        IconButton(
                           modifier = iconModifier,
                           imageVector = Icons.Default.Close,
                           onClick = {
                              focusManager.clearFocus()
                              shouldBeFocused = false
                              onSearchCanceled.invoke()
                           }
                        )
                     }, textStyle = TextStyle(color = MyColor.TextColor())
                  )
                     
                  
                  
               }
               
            }
         }
         
      }
   }
   @Parcelize @Serializable
   data object PlayerSheetContainerRoute : Destination{
      override val routeClass: KClass<*>
         get() = PlayerSheetContainerRoute::class
      
      override fun onNavigateTo(navController: NavHostController) {
         navController.navigate(this){
            launchSingleTop = true
            restoreState = true
         }
      }
      
      @Composable
      fun RenderBottomSheetContainer(
         prefs: SharedPreferences,
         localNavController: NavHostController,
         navigationViewModel: NavigationViewModel,
         refreshBoxState: PullToRefreshState,
         refreshState: RefreshState,
         globalBottomSheetVM: SheetViewmodel,
         drawerState: ViewDrawerState,
         onClickAddPlaylist:(PlayerContainer.PlaylistViewRoute)->Unit,
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
         val curSong by myExoPlayer.currentlyPlayingSong.stateFlow.collectAsState()
         
         
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
         
         
         
         MyNavigation(navController = localNavController, navigationViewModel = navigationViewModel){
            composable<PlayerContainer.MainPager> {
               val initialPage = remember { prefs.getCursorValue(MyPrefs._prefs_PagerPosition, "") }
               
               val contextRef = LocalContext.current
               val prefs = remember { MyPrefs.getPrefs(contextRef, MyPrefs.PrefDirs.VisualData) }
               
               val pageState = remember { PlayerContainer.MainPager.MainPagerState().apply {
                  initialPage.toIntOrNull()?.let {
                     this.initialPage = it
                  }
               }}
               
               
               var pagerState = rememberPagerState(
                  initialPage = pageState.initialPage,
                  pageCount = { pageState.pages.size }
               )
               
               
               
               
               // Save current page
               LaunchedEffect(pagerState.currentPage) {
                  
                  pageState.initialPage = pagerState.currentPage
                  
                  prefs.edit() {
                     putCursorValue(MyPrefs._prefs_PagerPosition,pagerState.currentPage.toString())
                  }
               }
               
               
               NestedScrollingFront(
                  appbar = {
                     PlayerContainer.MainPager.SearchTopBar(drawerState)
                  },
                  tabRow = {
                     TabRow(
                        modifier = Modifier.wrapContentHeight(),
                        indicator = {
                           CustomIndicator(it,pagerState)
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
                                       .padding(bottom = if (curSong != null) songBS_Config.bottomHeight else 0.dp),
                                    isRefreshing = refreshState.state,
                                    onRefresh = {
                                       refreshState.state = true
                                       refreshState.onRefresh.invoke()
                                    }
                                 ) {
                                    
                                    val songs by ListFeedingVM.SortedSongs.sortedAndFilteredSongsFlow.collectAsState()
                                    
                                    
                                    
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
                                       navigationViewModel.navigateTo(PlayerContainer.PlaylistViewRoute(it.title))
                                    },
                                    onPlusClick = {
                                       globalBottomSheetVM.addSheet(plusClickContent)
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
            
            composable<PlayerContainer.PlaylistViewRoute> {
               val args = it.toRoute<PlayerContainer.PlaylistViewRoute>()
               
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
               
               
               PlayerContainer.PlaylistViewRoute.Page(
                  playlistTitle = args.title,
                  lazyListState = playlistViewingLazyListState,
                  onBack = {
                     localNavController.navigateUp()
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
                        // TODO: Implement PrimaryNavigator again (to navigate to a speciffic song)
                     }
                  }
               )
            
            
         }
      }
      
      enum class MainPages{
         Home,
         Playlists;
         fun toInt(): Int {
            return toInt(this)
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
   }
   @Parcelize @Serializable
   data object EqualizerRoute: Destination{
      override val routeClass: KClass<*>
         get() = EqualizerRoute::class
      
      override fun onNavigateTo(navController: NavHostController) {
         navController.navigate(this){
            launchSingleTop = true
            restoreState = true
         }
      }
   }
   @Parcelize @Serializable
   data object SettingsRoute: Destination{
      override val routeClass: KClass<*>
         get() = SettingsRoute::class
      
      override fun onNavigateTo(navController: NavHostController) {
         navController.navigate(this){
            launchSingleTop = true
            restoreState = true
         }
      }
      
      private val settings: SettingsRepository?
         get() {return MySettings.settings}
      
      @Composable
      fun Page(){
         Box(Modifier.Companion.fillMaxSize()) {
            
            Theme.RenderDefaultBackground()
            
            val lazyListState = rememberLazyListState()
            
            LazyColumn(state = lazyListState, modifier = Modifier.Companion.fillMaxSize()) {
               stickyHeader {
                  Box(
                     modifier = Modifier.Companion
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .background(Theme.Sourcesets.ToolbarBackground())
                        .padding(10.dp),
                     contentAlignment = Alignment.Companion.CenterStart
                  ) {
                     Text("Settings", fontSize = 20.sp)
                  }
               }
               settings?.let { settings ->
                  
                  item("Shuffle") {
                     val value by settings.playerAllowShuffle.stateFlow.collectAsState()
                     SettingItem(
                        title = "Player Shuffle",
                        onClick = {
                           settings.playerAllowShuffle.setValue(!value)
                        }
                     ) {
                        Switch(value, {})
                     }
                  }
                  item("Orientation") {
                     HorizontalSeparator(MyColor.SeparatorGray)
                     val value by settings.orientation.stateFlow.collectAsState()
                     val dropdownState = rememberSimpleDropdownMenu(
                        listOf(
                           MySettings.ScreenOrientation.Auto.name to {
                              settings.orientation.setValue(MySettings.ScreenOrientation.Auto.value)
                           },
                           MySettings.ScreenOrientation.Portrait.name to {
                              settings.orientation.setValue(MySettings.ScreenOrientation.Portrait.value)
                           },
                           MySettings.ScreenOrientation.LandScape.name to {
                              settings.orientation.setValue(MySettings.ScreenOrientation.LandScape.value)
                           }
                        )
                     )
                     
                     SettingItem(
                        title = "Orientation",
                        onClick = {
                           dropdownState.open()
                        }
                     ) {
                        ChoiceButton(
                           text = MySettings.ScreenOrientation.fromInt(value).name,
                           dropdownState = dropdownState
                        )
                     }
                  }
                  item("Loop songs") {
                     HorizontalSeparator(MyColor.SeparatorGray)
                     val value by settings.playerAllowLoopSong.stateFlow.collectAsState()
                     SettingItem(
                        title = "Loop Songs",
                        onClick = {
                           settings.playerAllowLoopSong.setValue(!value)
                        }
                     ) {
                        Switch(value, {})
                     }
                  }
                  item("Loop at end") {
                     HorizontalSeparator(MyColor.SeparatorGray)
                     val value by settings.playerAllowLoopBack.stateFlow.collectAsState()
                     SettingItem(
                        title = "Loop Back",
                        onClick = {
                           settings.playerAllowLoopBack.setValue(!value)
                        }
                     ) {
                        Switch(value, {})
                     }
                  }
                  item("Loop around at beginning") {
                     HorizontalSeparator(MyColor.SeparatorGray)
                     val value by settings.playerAllowLoopAround.stateFlow.collectAsState()
                     SettingItem(
                        title = "Loop Back Around",
                        onClick = {
                           settings.playerAllowLoopAround.setValue(!value)
                        }
                     ) {
                        Switch(value, {})
                     }
                  }
                  
                  item("View tests") {
                     HorizontalSeparator(MyColor.SeparatorGray)
                     val value by settings.displayTests.stateFlow.collectAsState()
                     SettingItem(
                        title = "View tests",
                        onClick = {
                           settings.displayTests.setValue(!value)
                        }
                     ) {
                        Switch(value, {})
                     }
                  }
               }
            }
            
            
         }
      }
      
      @Composable
      fun SettingItem(
         title:String,
         onClick:()-> Unit,
         component: @Composable ()-> Unit
      ){
         Overlay(
            overlay = {
               Box(
                  Modifier.Companion.clickable(onClick = onClick)
               )
            }
         ) {
            Row(
               modifier = Modifier.Companion
                  .height(60.dp)
                  .fillMaxWidth()
                  .padding(horizontal = 10.dp),
               verticalAlignment = Alignment.Companion.CenterVertically,
               horizontalArrangement = Arrangement.SpaceBetween
            ) {
               Text(title)
               
               component.invoke()
            }
         }
      }
      private val shape = RoundedCornerShape(5.dp)
      @Composable
      fun ChoiceButton(
         text: String,
         dropdownState: SimpleDropdownMenuState
      ){
         
         Box(
            modifier = Modifier.Companion
               .wrapContentSize()
               .border(
                  2.dp,
                  Theme.Sourcesets.thumb,
                  shape
               )
               .background(Theme.background())
               .padding(vertical = 5.dp, horizontal = 8.dp),
            contentAlignment = Alignment.Companion.Center
         ) {
            Text(text = text)
            
            dropdownState.RenderMenu()
         }
         
      }
   }
   @Parcelize @Serializable
   data class PlaylistAddSongsRoute(val title:String):Destination{
      override val routeClass: KClass<*>
         get() = PlaylistAddSongsRoute::class
      
      override fun onNavigateTo(navController: NavHostController) {
         navController.navigate(this){
            launchSingleTop = true
            restoreState = true
         }
      }
      
   }
   
   /**
    * # !!! The FullScreen player is an activity of its own. !!!
    * */
   
   
   companion object{
      fun isAlreadyOn(navController: NavHostController, destination: Destination): Boolean {
         val current = navController.currentBackStackEntry?.destination ?: return false
         return current.hasRoute(destination.routeClass)
      }
      
   }
}
/**
 * ***MUST*** reflect [Destination]'s routes
 * */
fun NavController.toCurrentDestination(): Destination? {
   val dest = currentDestination ?: return null
   return when {
      dest.hasRoute(Destination.PlayerContainer.MainPager::class) -> {
         Destination.PlayerContainer.MainPager
      }
      dest.hasRoute(Destination.PlayerContainer.PlaylistViewRoute::class)->{
         currentBackStackEntry?.toRoute<Destination.PlayerContainer.PlaylistViewRoute>()?.let {
            Destination.PlayerContainer.PlaylistViewRoute(it.title)
         }
      }
      dest.hasRoute(Destination.PlayerSheetContainerRoute::class)->{
         Destination.PlayerSheetContainerRoute
      }
      dest.hasRoute(Destination.EqualizerRoute::class)->{
         Destination.EqualizerRoute
      }
      dest.hasRoute(Destination.SettingsRoute::class)->{
         Destination.SettingsRoute
      }
      dest.hasRoute(Destination.PlaylistAddSongsRoute::class)->{
         currentBackStackEntry?.toRoute<Destination.PlaylistAddSongsRoute>()?.let {
            Destination.PlaylistAddSongsRoute(it.title)
         }
      }
      // Example for data class instead of data object:
      //currentBackStackEntry?.toRoute<Destination.ProfileRoute>()?.let { Destination.ProfileRoute(it.userId) }
      else -> null
   }
}

