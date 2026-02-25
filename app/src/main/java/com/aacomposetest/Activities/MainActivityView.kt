package com.aacomposetest.Activities

import ExoPlayer.myExoPlayer
import MyViewModels.ListFeedingVM
import MyViewModels.RefreshState
import MyViewModels.SongsViewModel
import Navigation.Destinations.FullscreenPlayer
import Navigation.Destinations.MainPager
import Navigation.Destinations.PlayerBottomSheet_Frame
import Navigation.Destinations.PlaylistAddSongsPage
import Navigation.NavExtensions.safeNavigate
import Navigation.SafeDrawer
import Presentation.FullscreenPlayer.FullscreenPlayerPage
import Presentation.Navigation.DrawerStandardLayout
import Presentation.Navigation.MyNavigation
import SrufComposeComponents.Experimental.ColorPicker
import ui.components.MyModalBottomSheet
import ui.components.SheetViewmodel
import SrufComposeComponents.Text
import android.Manifest
import android.database.ContentObserver
import android.os.Bundle
import android.provider.MediaStore
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.EaseInOutElastic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.aacomposetest.ui.theme.SrufTheme
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope
import com.aacomposetest.Services.MusicService
import com.aacomposetest.ui.theme.MyColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.aacomposetest.Classes.ContentObserver_Impl
import com.aacomposetest.Classes.MyPrefs
import com.aacomposetest.Classes.createPermissionLauncher
import com.aacomposetest.Functions.map
import com.aacomposetest.Log
import com.aacomposetest.MyApplication
import com.aacomposetest.buildSystemBarsStyle
import com.aacomposetest.tryTo
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import kotlinx.coroutines.withContext
import kotlinx.serialization.InternalSerializationApi
import ui.components.BasicPopUp
import ui.components.Pages.AddSongsToPlaylistPage
import ui.components.ViewDrawerValue
import ui.components.rememberViewDrawerState

class MainActivityView : ComponentActivity() {
   
   private  var mediastoreObserver: ContentObserver? = null
   
   val songViewModel: SongsViewModel = SongsViewModel(this)
   
   val refreshState = RefreshState()
   
   val hasNotificationPermission = mutableStateOf(false)
   val notificationPermissionLauncher = createPermissionLauncher(
      permission = Manifest.permission.POST_NOTIFICATIONS,
      onFinish = {result->
         
         if(hasNotificationPermission.value != result) {
            hasNotificationPermission.value = result
         }
         
      }
   )
   
   
   val mediaPermissionLauncher = createPermissionLauncher(
      permission = Manifest.permission.READ_MEDIA_AUDIO,
      onGrant = {
         songViewModel.viewModelScope.launch {
            songViewModel.resyncALL_withMediaStore(
               context = this@MainActivityView,
               alsoPictures = true,
               forceUpdate = true,
               onFinish = {
                  if (refreshState.state == true) {
                     
                     withContext(Dispatchers.Main) {
                        Toast.makeText(
                           this@MainActivityView,
                           "Update successful!",
                           Toast.LENGTH_SHORT
                        ).show()
                     }
                     
                     refreshState.state = false
                  }
                  
               }
            )
            
         }
      }
   )
   
   private fun setupKeyboardPanMode(){
      window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
   }
   
   private fun setupNavBars(){
      val statusBarStyle = buildSystemBarsStyle()
      val navigationBarStyle = buildSystemBarsStyle(darkMode = MyColor.Black.toArgb())
      enableEdgeToEdge(statusBarStyle = statusBarStyle, navigationBarStyle = navigationBarStyle)
   }
   
   override fun onCreate(savedInstanceState: Bundle?) {
      super.onCreate(savedInstanceState)
      
      // No adjustment for text
      setupKeyboardPanMode()
      // Setup navbars colors
      setupNavBars()
      
      
      /**
       * Hook database to MediaStore database
       */
      mediastoreObserver = ContentObserver_Impl(
         onAnyChange = { selfChange->
            
            with(songViewModel){
               viewModelScope.launch {
                  resyncALL_withMediaStore(MyApplication.instance, alsoPictures = true, forceUpdate = false)
               }
            }
            
         }
      ).apply {
         /**
          * Register the created implementation for updates from MediaStore
          */
         contentResolver.registerContentObserver(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            true, // notify for descendants
            this
         )
      }
      
      /**
       * Remove cyclic refferencing by simply setting onRefresh later than the initialization of the state.
       * */
      refreshState.setOnRefreshListener {
         mediaPermissionLauncher.launch()
      }
      
      
      
      setContent {
         SrufTheme {
            Surface(modifier = Modifier.fillMaxSize()) {
               
               
               MainView()
               
            }
            
         }
      }
   }
   
   
   
   override fun onPause() {
      super.onPause()
   }
   
   override fun onResume() {
      super.onResume()
   }
   
   @OptIn(FlowPreview::class)
   @Composable
   private fun MainView(){
      
      val globalBottomSheetVM : SheetViewmodel = viewModel()
      
      val contextRef = LocalContext.current
      
      
      
      /**
       * This is where the service is started.
       * */
      LaunchedEffect(hasNotificationPermission.value) {
         
         notificationPermissionLauncher.launch()
         
         if(hasNotificationPermission.value && myExoPlayer.isInitialized.value) {
            
            MusicService.handleOnAppStart(contextRef)
            
         }
      }
      
      // Try running the query for the first time if i have the permissions
      LaunchedEffect(Unit) {
         try {
            songViewModel.resyncALL_withMediaStore(
               context = contextRef,
               alsoPictures = true,
               forceUpdate = false
            )
         }catch (ex: Exception){
            ex.printStackTrace()
         }
      }
      
      
      
      
      ParentContainer(
         globalBottomSheetVM = globalBottomSheetVM,
         refreshState = refreshState
      )
      MyModalBottomSheet(modifier = Modifier.fillMaxSize(),state = globalBottomSheetVM)
      
   }
}

@OptIn(ExperimentalMaterial3Api::class, InternalSerializationApi::class)
@Composable
private fun ParentContainer(
   globalBottomSheetVM: SheetViewmodel,
   refreshState: RefreshState
) {
   val contextRef = LocalContext.current
   
   /**
    * Container for EVERYTHING that can have a [Settings Sheet] in front of it. So basically the entire application
    *
    *
    * THIS EXCLUDES: Other Activities, Notifications, Widgets etc that are not inside the Main Activity View
    * */
   Surface(
      modifier = Modifier.fillMaxSize(),
      color = tempBackgroundColor
   ) {
      
      val drawerState = rememberViewDrawerState(initialValue = ViewDrawerValue.Closed)
      val allSongs by ListFeedingVM.allSongs.collectAsState()
      
      val navController = rememberNavController()
      
      val playerBS_mainContentNavController = rememberNavController()
      
      SafeDrawer(
         state = drawerState,
         drawerContent = {
            DrawerStandardLayout(
               modifier = Modifier
                  .fillMaxHeight()
                  .graphicsLayer {
                     this.alpha = drawerState.currentOffset
                  }
                  .background(tempDrawerColor)
                  .pointerInput(Unit) {
                     detectTapGestures()
                  }
                  .padding(
                     start = 10.dp,
                     top = 10.dp
                  )
            ) {
               items(
                  items = allSongs,
                  key = { item -> item.id }  // Only item, no index
               ) { song ->
                  // Your item content
                  Row() {
                     Text("${song.id}|")
                     
                     Text("${song.timesPlayed}|")
                     
                     Text(song.title)
                  }
               }
            }
         },
         mainContent = {
            
            Box(
               modifier = Modifier
                  .fillMaxSize()
                  .navigationBarsPadding()
                  .displayCutoutPadding()
                  .systemBarsPadding()
                  .background(MyColor.Background())
            ) {
               
               val prefs by lazy {
                  MyPrefs.getPrefs(contextRef, MyPrefs.PrefDirs.VisualData)
               }
               
               
               Box(
                  modifier = Modifier
                     .fillMaxSize()
                     .graphicsLayer {
                        shape = RoundedCornerShape(
                           size = 20.dp.times(drawerState.currentOffset)
                        )
                        clip = true
                        val scalar = drawerState.currentOffset.map(0f, 1f, 1f, 0.8f)
                        
                        scaleX = scalar
                        scaleY = scalar
                        transformOrigin = TransformOrigin.Center
                     }
               ) {
                  
                  
                  val refreshBoxState = rememberPullToRefreshState()
                  
                  
                  /**
                   * Manages navigation inside the [Main Pager] container.
                   * This container lives together with the bellow [Player Bottom Sheet] in the same parent container.
                   * */
                  
                  val contextRef = LocalContext.current
                  MyNavigation(
                     startDestination = {
                        PlayerBottomSheet_Frame
                     }.invoke(),
                     navController = navController
                  ) {
                     
                     
                     composable<PlayerBottomSheet_Frame> {
                        
                        PlayerBottomSheet_Frame.invoke(
                           startDestination = MainPager,
                           prefs = prefs,
                           localNavController = playerBS_mainContentNavController,
                           refreshBoxState = refreshBoxState,
                           refreshState = refreshState,
                           globalBottomSheetVM = globalBottomSheetVM,
                           onClickAddPlaylist = { args ->
                              navController.safeNavigate(PlaylistAddSongsPage(args.title))
                           },
                           onBottomSheetClick = {
                              
                              tryTo {
                                 navController.safeNavigate(FullscreenPlayer)
                              }
                           }
                        )
                     }
                     
                     
                     
                     composable<FullscreenPlayer>(
                        enterTransition = {
                           val duration = 250
                           slideIntoContainer(
                              AnimatedContentTransitionScope.SlideDirection.Up,
                              animationSpec = tween(duration, easing = EaseInOut)
                           )
                        },
                        popExitTransition = {
                           val duration = 250
                           slideOutOfContainer(
                              AnimatedContentTransitionScope.SlideDirection.Down,
                              animationSpec = tween(duration, easing = EaseInOut)
                           )
                        }
                     ) {
                        
                        FullscreenPlayerPage(
                           modifier = Modifier
                              .fillMaxSize()
                              .safeDrawingPadding()
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
                        
                        val addsongstoplaylistState =
                           rememberSaveable(saver = LazyListState.Saver) { LazyListState() }
                        
                        AddSongsToPlaylistPage(
                           lazyListState = addsongstoplaylistState,
                           playlistTitle = playlistTitle,
                           onBack = {
                              navController.popBackStack()
                           }
                        )
                     }
                     
                  }
                  
                  
               }
            }
            
         }
      )
      
      
   }
   
}




val tempBackgroundColor = MyColor.SemiTransparentLight

val tempDrawerColor = MyColor.SemiTransparentDark





