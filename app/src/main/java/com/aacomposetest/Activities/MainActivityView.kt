package com.aacomposetest.Activities

import AppDatabase.PlaylistRelated.Playlist
import ExoPlayer.ExoPlayerViewModel
import ExoPlayer.myExoPlayer
import MyViewModels.ListFeedingVM
import MyViewModels.PlaylistViewModel
import MyViewModels.RefreshState
import MyViewModels.SongsViewModel
import MyViewModels.rememberRefreshState
import Navigation.MainPage
import Navigation.PlaylistAddSongsPage
import Navigation.PlaylistPage
import Navigation.ViewDrawer
import Navigation.ViewDrawerState
import Navigation.ViewDrawerValue
import Navigation.rememberViewDrawerState
import SrufComposeComponents.IconButton
import SrufComposeComponents.KeyboardAwareBox
import SrufComposeComponents.MyModalBottomSheet
import SrufComposeComponents.Text
import android.Manifest
import android.database.ContentObserver
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOutElastic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.TextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.aacomposetest.ui.theme.ComposeTestTheme
import com.composables.core.SheetDetent
import com.composables.core.rememberBottomSheetState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import ui.components.Pages.PlaylistsPage
import ui.components.Pages.PlaylistViewing
import kotlin.getValue
import androidx.core.content.edit
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.compose.NavHost
import com.aacomposetest.Classes.MyPermissionLauncher
import com.aacomposetest.Classes.MyPrefs
import com.aacomposetest.Classes.MyPrefs.putCursorValue
import com.aacomposetest.Logs
import com.aacomposetest.R
import com.aacomposetest.Services.MusicService
import com.aacomposetest.ui.theme.MyColor
import com.aacomposetest.ui.theme.Theme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.map
import ui.components.BottomSheets.Content.PlaylistAddOrEdit_Content
import ui.components.BottomSheets.Content.PlaylistDelete_Content
import ui.components.BottomSheets.SongPlayingBottomSheet
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.toRoute
import com.aacomposetest.Classes.MyPrefs.getCursorValue
import com.aacomposetest.Functions.map
import kotlinx.coroutines.withContext
import ui.components.Pages.AddSongsToPlaylistPage
import ui.components.Pages.SongsPage
import kotlin.collections.map

class MainActivityView : ComponentActivity() {

    private  var mediastoreObserver: ContentObserver? = null

    val exoPlayerVM = ExoPlayerViewModel().apply { progressVM.delayMillis = 250 }

    lateinit var songViewModel: SongsViewModel




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        val contextRef = this
        songViewModel = SongsViewModel(this)


        val statusBarStyle = SystemBarStyle.auto(lightScrim = Color.Transparent.value.toInt(), darkScrim = Color.Transparent.value.toInt())
        enableEdgeToEdge(statusBarStyle = statusBarStyle)

        /**
         * Hook database to MediaStore database
         */
        mediastoreObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                super.onChange(selfChange)
                // Trigger a database update here
                songViewModel.viewModelScope.launch {
                    songViewModel.resyncWithMediastore(contextRef, alsoPictures = true, forceUpdate = false)
                }

            }
        }
        mediastoreObserver?.apply {
            contentResolver.registerContentObserver(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                true, // notify for descendants
                this
            )
        }


        myExoPlayer.initializePlayer(this)



        setContent {
            ComposeTestTheme {
                Surface(modifier = Modifier.fillMaxSize()) {

                    MainView()
                }

            }
        }
    }



    override fun onPause() {


        exoPlayerVM.progressVM.stopUpdates()

        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        exoPlayerVM.progressVM.startUpdates()
    }



    @OptIn(FlowPreview::class)
    @Composable
    private fun MainView(){

        val bottomSheetViewmodel :MyModalBottomSheet.ModalVM = viewModel()
        val modalSettingsSheetState = rememberBottomSheetState(initialDetent = SheetDetent.Hidden)

        val contextRef = LocalContext.current

        val refreshState = rememberRefreshState(false)

        val mediaPermLauncherAuto = MyPermissionLauncher.rememberLauncher(
            onGrant = {
                songViewModel.viewModelScope.launch {
                    songViewModel.resyncWithMediastore(
                        context = contextRef,
                        alsoPictures = true,
                        forceUpdate = false,
                        onFinish = {
                            if (refreshState.state == true) {
                                // Toast can only be done on Main thread
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(
                                        contextRef,
                                        "Update successful!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }

                                refreshState.state = false
                            }
                        }
                    )

                }
            },
            onDeny = {
                Toast.makeText(contextRef, "Media permission denied", Toast.LENGTH_SHORT)
                    .show()

                refreshState.state = false
            }
        )

        val mediaPermLauncherManual = MyPermissionLauncher.rememberLauncher(
            onGrant = {
                songViewModel.viewModelScope.launch {
                    songViewModel.resyncWithMediastore(
                        context = contextRef,
                        alsoPictures = true,
                        forceUpdate = true,
                        onFinish = {
                            if (refreshState.state == true) {

                                withContext(Dispatchers.Main) {
                                    Toast.makeText(
                                        contextRef,
                                        "Update successful!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }

                                refreshState.state = false
                            }

                        }
                    )

                }
            },
            onDeny = {
                Toast.makeText(contextRef, "Media permission denied", Toast.LENGTH_SHORT)
                    .show()

                refreshState.state = false
            }
        )

        var hasNotificationPermission by remember { mutableStateOf(false) }
        val notificationPermissionLauncher = MyPermissionLauncher.rememberLauncher(
            onFinish = {granted->
                Log.i(Logs.PERMISSIONS.name,"Called notificationPermissionLauncher and received ${granted}")
                // safety against recursive calling
                if(hasNotificationPermission != granted) {
                    hasNotificationPermission = granted
                }
            }
        )


        val isPlaying by exoPlayerVM.isPlaying.collectAsStateWithLifecycle()


        /**
         * This is where the service is started.
         * */
        LaunchedEffect(hasNotificationPermission, isPlaying) {

            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)


            if(hasNotificationPermission && myExoPlayer.isInitialized.value) {

                MusicService.handleOnAppStart(contextRef)
            }
        }

        // Requet permissions
        LaunchedEffect(Unit) {
            mediaPermLauncherAuto.launch(Manifest.permission.READ_MEDIA_AUDIO)
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        // Try running the query for the first time if i have the permissions
        LaunchedEffect(Unit) {
            try {
                songViewModel.resyncWithMediastore(
                    context = contextRef,
                    alsoPictures = true,
                    forceUpdate = false
                )
            }catch (ex: Exception){
                ex.printStackTrace()
            }
        }







        ParentContainer(
            activity = this@MainActivityView,
            exoPlayerVM = exoPlayerVM,
            globalBottomSheetVM = bottomSheetViewmodel,
            refreshState = refreshState.apply {
                onRefresh = {
                    mediaPermLauncherManual.launch(
                        Manifest.permission.READ_MEDIA_AUDIO,
                    )
                }
            }
        )



        /**
         * Global settings BottomSheet
         *
         * i HAVE to change this to a stack of bottom sheets moderated by the ViewModel later
         * */
        MyModalBottomSheet(
            viewModel = bottomSheetViewmodel,
            sheetState = modalSettingsSheetState,
            bottomSheetContent = bottomSheetViewmodel.content.value
        )

    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ParentContainer(
    activity: ComponentActivity,
    exoPlayerVM: ExoPlayerViewModel,
    globalBottomSheetVM: MyModalBottomSheet.ModalVM,
    refreshState: RefreshState
) {


    val drawerState = rememberViewDrawerState(initialValue = ViewDrawerValue.Closed)



    /**
     * Container for EVERYTHING that can have a [Settings Sheet] in front of it. So basically the entire application
     *
     * THIS EXCLUDES: Other Activities, Notifications, Widgets etc that are not inside the Main Activity View
     * */
    Surface(
        modifier = Modifier.fillMaxSize(), color = MyColor.WorkInProgress
    ) {
        val systemBars = WindowInsets.systemBars
        val navBars = WindowInsets.navigationBars
        val displayCutout = WindowInsets.displayCutout

        val layoutDirection = LocalLayoutDirection.current

        val localDensity = LocalDensity.current

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
                    )
                }

            },
            drawerContent = {


                Scaffold(
                    modifier = Modifier
                        .fillMaxHeight()
                        .alpha(drawerState.currentOffset)
                        .width(250.dp)
                        .background(Color.Cyan)
                        .pointerInput(Unit) {
                            detectTapGestures()
                        },
                    containerColor = MyColor.Transparent
                ) { innerPadding ->
                    Button(modifier = Modifier.padding(innerPadding), onClick = {}) {
                        Text("Hello there!")
                    }
                }


            }
        )


    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContent(activity: ComponentActivity, drawerState: ViewDrawerState,refreshState: RefreshState,globalBottomSheetVM: MyModalBottomSheet.ModalVM,exoPlayerVM: ExoPlayerViewModel){


    val contextRef = LocalContext.current

    val playlistViewmodel by remember { mutableStateOf(PlaylistViewModel(contextRef)) }

    val navController = rememberNavController()

    val allsongs by ListFeedingVM.allSongs.map { list -> list.map { it.copy() } }.collectAsStateWithLifecycle(initialValue = emptyList())
    val sortedAllSongs by ListFeedingVM.SortedSongs.sortedAndFilteredSongsFlow.map { list -> list.map { it.copy() } }.collectAsStateWithLifecycle(initialValue = emptyList())
    val allPlaylists by ListFeedingVM.SortedPlaylists.filteredPlaylists.collectAsStateWithLifecycle(initialValue = emptyList())



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


        val songPlayerBottomSheetAlpha = remember { Animatable(1f) }
        val SongBottomSheetPeekHeight = 60.dp
        val SongBottomSheetPeek = SheetDetent("Peek") { containerHeight, sheetHeight ->
            //containerHeight*0.2f

            SongBottomSheetPeekHeight
        }

        val songPlayerBottomSheetState = rememberBottomSheetState(
            initialDetent = SongBottomSheetPeek,
            detents = listOf(SongBottomSheetPeek, SheetDetent.FullyExpanded)
        )
        val refreshBoxState = rememberPullToRefreshState()

        /**
         * Manages navigation inside the [Main Pager] container.
         * This container lives together with the bellow [Player Bottom Sheet] in the same parent container.
         * */
        NavHost(
            modifier = Modifier.fillMaxSize(),
            navController = navController,
            startDestination = MainPage,
            enterTransition = {

                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(300)
                )
            },// page is navigated TO normally
            popExitTransition = {

                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(300)
                )
            },// page is navigated FROM by popBackStack() (returning FROM the page to the previous one)
            popEnterTransition = {

                EnterTransition.None
            },// page is being returned to by the above page animating away
            exitTransition = {

                ExitTransition.None
            },// page stays back while another page is animated above

        ) {

            composable<MainPage> {
                val initialPage =
                    remember { prefs.getCursorValue(MyPrefs._prefs_PagerPosition, 0) }



                MainPage.invoke(initialPage) { newPage ->
                    when (newPage) {

                        0 -> {




                            PullToRefreshBox(
                                state = refreshBoxState,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(bottom = SongBottomSheetPeekHeight),
                                isRefreshing = refreshState.state,
                                onRefresh = {
                                    refreshState.state = true
                                    refreshState.onRefresh.invoke()
                                }
                            ) {
                                SongsPage(
                                    modifier = Modifier
                                        .fillMaxSize(),
                                    settingsBottomSheetVM = globalBottomSheetVM,
                                    songs = allsongs,
                                    exoPlayerVM = exoPlayerVM,
                                    onOptionsClick = null,
                                    state = songsListState
                                )
                            }
                        }

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
                                    navController.navigate(PlaylistPage(it.title)) {
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
                                                            bottomSheetVM = globalBottomSheetVM,
                                                            playlistViewModel = playlistViewmodel
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

            composable<PlaylistPage> {
                val args = it.toRoute<PlaylistPage>()

                val playlistViewingLazyListState =
                    rememberSaveable(saver = LazyListState.Saver) { LazyListState() }


                PlaylistViewing(
                    playlistTitle = args.title,
                    lazyListState = playlistViewingLazyListState,
                    playlistVM = playlistViewmodel,
                    exoPlayerVM = exoPlayerVM,
                    onBack = {
                        navController.popBackStack()
                    },
                    songs = allsongs,
                    bottomPadding = SongBottomSheetPeekHeight,
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

                val playlistTitle =
                    backStack.arguments?.getString(
                        args.title
                    )
                        ?: ""

                LaunchedEffect(Unit) {
                    scope.launch {
                        songPlayerBottomSheetAlpha.animateTo(
                            0f,
                            animationSpec = tween(500)
                        )
                    }
                }

                val addsongstoplaylistState =
                    rememberSaveable(saver = LazyListState.Saver) { LazyListState() }

                val onBack = {
                    scope.launch(Dispatchers.Main) {
                        songPlayerBottomSheetAlpha.animateTo(
                            1f,
                            animationSpec = tween(500)
                        )
                    }
                    navController.popBackStack()
                }

                AddSongsToPlaylistPage(
                    lazyListState = addsongstoplaylistState,
                    playlistTitle = playlistTitle,
                    allSongs = allsongs,
                    navController = navController,
                    onConfirm = { finalList ->

                        scope.launch(Dispatchers.IO) {
                            val playlist =
                                Playlist.getPlaylist(contextRef, playlistTitle)

                            playlist?.songsIdList?.apply {


                                removeAll(finalList.filter { it -> !it.isChecked.value }
                                    .map { it -> it.item.id })

                                finalList.filter { it -> it.isChecked.value }
                                    .map { it -> it.item.id }.let { mappedList ->


                                        addAll(mappedList.filter { it ->
                                            !this.contains(
                                                it
                                            )
                                        })
                                    }

                            }



                            playlist?.save(contextRef) {
                                onBack.invoke()
                            }


                        }

                    },
                    onBack = {
                        onBack.invoke()
                    }
                )
            }
        }


        val expandedHeight = 250.dp
        /**
         * Bottom sheet can be "Enabled" or "Disabled" by controlling this value, which is also attached to its alpha for propper animation
         * */
        if (songPlayerBottomSheetAlpha.value > 0f) {
            SongPlayingBottomSheet(
                modifier = Modifier
                    .alpha(songPlayerBottomSheetAlpha.value)
                    .fillMaxWidth()
                    .height(expandedHeight),
                sheetstate = songPlayerBottomSheetState,
                backgroundColor = Theme.colorScheme().onBackground,
                shape = RoundedCornerShape(topStart = 15.dp, topEnd = 15.dp),
                expandedHeight = expandedHeight,
                peekHeight = SongBottomSheetPeekHeight,
                exoPlayerVM = exoPlayerVM,

                )
        }
    }
}



@Composable
fun SearchTopBar(){
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
                text = LocalContext.current.getString(R.string.app_name),
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








