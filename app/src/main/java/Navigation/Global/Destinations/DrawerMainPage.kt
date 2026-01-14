package Navigation.Global.Destinations

import ExoPlayer.ExoPlayerViewModel
import ExoPlayer.myExoPlayer
import MyViewModels.ListFeedingVM
import MyViewModels.RefreshState
import MyViewModels.SongsViewModel
import MyViewModels.SongsViewModelFactory
import Navigation.Interior.Destinations.FullscreenPlayer
import Presentation.Navigation.Destinations.MainPager
import Presentation.Navigation.Destinations.PlaylistAddSongsPage
import Presentation.Navigation.Destinations.PlaylistViewingPage
import Presentation.Navigation.DrawerStandardLayout
import Presentation.Navigation.MyNavigation
import Presentation.SongsPage.MainSongsColumn
import SrufComposeComponents.MyModalBottomSheet
import SrufComposeComponents.Text
import androidx.activity.ComponentActivity
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.EaseInOutElastic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.aacomposetest.Activities.tempDrawerColor
import com.aacomposetest.Classes.MyPrefs
import com.aacomposetest.Classes.MyPrefs.getCursorValue
import com.aacomposetest.Classes.MyPrefs.putCursorValue
import com.aacomposetest.Functions.map
import com.aacomposetest.ui.theme.MyColor
import com.composables.core.BottomSheetState
import com.composables.core.SheetDetent
import com.composables.core.rememberBottomSheetState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import ui.components.BottomSheets.BottomSheetConfig
import ui.components.BottomSheets.Content.PlaylistAddOrEdit_Content
import ui.components.BottomSheets.Content.PlaylistDelete_Content
import ui.components.BottomSheets.ResponsivePlayerBS
import ui.components.BottomSheets.rememberBottomSheetConfig
import ui.components.Pages.AddSongsToPlaylistPage
import ui.components.Pages.PlaylistViewing
import ui.components.Pages.PlaylistsPage
import ui.components.ViewDrawer
import ui.components.ViewDrawerState
import ui.components.ViewDrawerValue
import ui.components.rememberViewDrawerState

@Serializable
object DrawerMainPage {
    @Composable
    fun invoke(
        activity: ComponentActivity,
        exoPlayerVM: ExoPlayerViewModel,
        globalBottomSheetVM: MyModalBottomSheet.ModalVM,
        refreshState: RefreshState,
        songPlayerBS_State: BottomSheetState, songBS_Config: BottomSheetConfig,
        openFullscreenSong:()-> Unit = {}
    ) {
        val systemBars = WindowInsets.systemBars
        val navBars = WindowInsets.navigationBars
        val displayCutout = WindowInsets.displayCutout

        val layoutDirection = LocalLayoutDirection.current

        val localDensity = LocalDensity.current

        val drawerState = rememberViewDrawerState(initialValue = ViewDrawerValue.Closed)

        val contextref = LocalContext.current

        val vm: SongsViewModel = viewModel(factory = SongsViewModelFactory(contextref))


        val allSongs by vm.getAllSongsFlow.collectAsState()

        ViewDrawer(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = with(localDensity) {
                        maxOf(
                            systemBars.getLeft(this, layoutDirection),
                            navBars.getLeft(this, layoutDirection),
                            displayCutout.getLeft(this, layoutDirection)
                        ).toDp()
                    }
                ),
            state = drawerState,
            //scrimColor = MyColor.Transparent.toArgb(),
            mainContent = {

                Box(modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
                    .displayCutoutPadding()
                    .systemBarsPadding()
                ) {
                    MainContent(
                        activity = activity,
                        drawerState = drawerState,
                        refreshState = refreshState,
                        globalBottomSheetVM = globalBottomSheetVM,
                        exoPlayerVM = exoPlayerVM,
                        songPlayerBS_State = songPlayerBS_State, songBS_Config = songBS_Config,
                        openFullscreenSong = openFullscreenSong,
                    )
                }

            },
            drawerContent = {

                DrawerStandardLayout(
                    modifier = Modifier
                        .fillMaxHeight()
                        .alpha(drawerState.currentOffset)
                        //.width(250.dp)
                        .background(tempDrawerColor)
                        .pointerInput(Unit) {
                            detectTapGestures()
                        }
                        .padding(
                            start = 10.dp,
                            top = 10.dp
                        ),
                    header = {

                    }
                ) {
                    LazyColumn {
                        items(
                            items = allSongs,
                            key = { item -> item.id }  // Only item, no index
                        ) { song ->
                            // Your item content
                            Row(){
                                Text("${song.id}|")

                                Text("${song.timesPlayed}|")

                                Text(song.title)
                            }
                        }
                    }
                }


            }
        )
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun MainContent(
        activity: ComponentActivity,
        drawerState: ViewDrawerState,
        refreshState: RefreshState,
        globalBottomSheetVM: MyModalBottomSheet.ModalVM,
        exoPlayerVM: ExoPlayerViewModel,
        songPlayerBS_State: BottomSheetState, songBS_Config: BottomSheetConfig,
        openFullscreenSong:()-> Unit
    ){


        val contextRef = LocalContext.current

        val navController = rememberNavController()

        val allsongs by ListFeedingVM.allSongs.map { list -> list.map { it.copy() } }.collectAsStateWithLifecycle(initialValue = emptyList())
        val sortedAllSongs by ListFeedingVM.SortedSongs.sortedAndFilteredSongsFlow.map { list -> list.map { it.copy() } }.collectAsStateWithLifecycle(initialValue = emptyList())
        val allPlaylists by ListFeedingVM.SortedPlaylists.filteredPlaylists.collectAsState(emptyList())



        val prefs by lazy {
            MyPrefs.getPrefs(activity)
        }

        val songsListState = rememberSaveable(saver = LazyListState.Saver) { LazyListState(
            firstVisibleItemIndex = prefs.getInt(MyPrefs._prefs_songListstate_FirstItem.name,0),
            firstVisibleItemScrollOffset = prefs.getInt(MyPrefs._prefs_songListState_Offset.name,0)
        ) }
        val playlistsListState = rememberSaveable(saver = LazyListState.Saver) { LazyListState(
            firstVisibleItemIndex = prefs.getInt(MyPrefs._prefs_playlistsListState_FirstItem.name,0),
            firstVisibleItemScrollOffset = prefs.getInt(MyPrefs._prefs_playlistsListState_Offset.name,0))
        }

        val contentScale by remember {
            derivedStateOf {
                drawerState.currentOffset.map(0f,1f,1f,0.8f)
            }
        }

        val contentShapeCorner by remember {
            derivedStateOf {
                20.dp.times(drawerState.currentOffset )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = contentScale
                    scaleY = contentScale
                    transformOrigin = TransformOrigin.Center
                }
                .clip(shape = RoundedCornerShape(size = contentShapeCorner))
                .background(MyColor.Background())
        ) {
            val scope = rememberCoroutineScope()


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


            val refreshBoxState = rememberPullToRefreshState()

            /**
             * Manages navigation inside the [Main Pager] container.
             * This container lives together with the bellow [Player Bottom Sheet] in the same parent container.
             * */
            MyNavigation(
                startDestination = MainPager,
                navController = navController
            ) {

                composable<MainPager> {
                    val initialPage =
                        remember { prefs.getCursorValue(MyPrefs._prefs_PagerPosition, 0) }


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
                                        navController.navigate(PlaylistViewingPage(it.title)) {
                                            launchSingleTop = true
                                            restoreState = true
                                        }
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

                composable<PlaylistViewingPage> {
                    val args = it.toRoute<PlaylistViewingPage>()

                    val playlistViewingLazyListState =
                        rememberSaveable(saver = LazyListState.Saver) { LazyListState() }



                    PlaylistViewing(
                        playlistTitle = args.title ,
                        lazyListState = playlistViewingLazyListState,
                        exoPlayerVM = exoPlayerVM,
                        onBack = {
                            navController.popBackStack()
                        },
                        bottomPadding = songBS_Config.bottomHeight,
                        plusButtonClick = {
                            navController.navigate(PlaylistAddSongsPage(args.title)) {
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                    )
                }

                composable<PlaylistAddSongsPage>(
                    enterTransition = {

                        slideIntoContainer(
                            AnimatedContentTransitionScope.SlideDirection.Up,
                            animationSpec = tween(500, easing = EaseInOutElastic)
                        )
                    },// page is navigated TO normally
                    popExitTransition = {

                        slideOutOfContainer(
                            AnimatedContentTransitionScope.SlideDirection.Down,
                            animationSpec = tween(500)
                        )
                    }
                ) { backStack ->
                    val args = backStack.toRoute<PlaylistAddSongsPage>()

                    val playlistTitle = args.title

                    LaunchedEffect(Unit) {
                        scope.launch {
                            songBS_Config.alpha.animateTo(
                                0f,
                                animationSpec = tween(500)
                            )
                        }
                    }

                    val addsongstoplaylistState =
                        rememberSaveable(saver = LazyListState.Saver) { LazyListState() }

                    val onBack = {
                        scope.launch(Dispatchers.Main) {
                            songBS_Config.alpha.animateTo(
                                1f,
                                animationSpec = tween(500)
                            )
                        }
                        navController.popBackStack()
                    }

                    AddSongsToPlaylistPage(
                        lazyListState = addsongstoplaylistState,
                        playlistTitle = playlistTitle,
                        onBack = {
                            onBack.invoke()
                        }
                    )
                }

            }



            /**
             * Bottom sheet can be "Enabled" or "Disabled" by controlling this value, which is also attached to its alpha for propper animation
             * */


            if (songBS_Config.alpha.value > 0f) {

                val curSong by myExoPlayer.currentlyPlayingSong.collectAsState()

                ResponsivePlayerBS(
                    state = songPlayerBS_State,
                    currentSong = curSong,
                    onBackgroundClick = {
                        openFullscreenSong.invoke()
                    }
                )



            }
        }
    }





}