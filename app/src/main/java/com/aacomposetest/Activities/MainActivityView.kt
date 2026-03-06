package com.aacomposetest.Activities

import ExoPlayer.myExoPlayer
import MyViewModels.ListFeedingVM
import MyViewModels.RefreshState
import MyViewModels.SongsViewModel
import Navigation.Destinations.FullscreenPlayer
import Navigation.Destinations.MainPager
import Navigation.Destinations.PlayerBottomSheetParent
import Navigation.Destinations.PlaylistAddSongsPage
import Navigation.NavExtensions.safeNavigate
import Navigation.SafeDrawer
import Presentation.FullscreenPlayer.FullscreenPlayerPage
import Presentation.Navigation.DrawerStandardLayout
import Presentation.Navigation.MyNavigation
import SrufComposeComponents.Text
import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.IntentSanitizer
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.aacomposetest.Classes.CustomComponentActivity
import com.aacomposetest.Classes.MyPrefs
import com.aacomposetest.Classes.createPermissionLauncher
import com.aacomposetest.Functions.map
import com.aacomposetest.Log
import com.aacomposetest.MyApplication
import com.aacomposetest.Services.MusicService
import com.aacomposetest.tryTo
import com.aacomposetest.ui.theme.MyColor
import com.aacomposetest.ui.theme.SrufTheme
import com.aacomposetest.ui.theme.Theme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.InternalSerializationApi
import ui.components.BottomSheets.renderDefaultBackground
import ui.components.MyModalBottomSheet
import ui.components.Pages.AddSongsToPlaylistPage
import ui.components.SheetViewmodel
import ui.components.ViewDrawerValue
import ui.components.rememberViewDrawerState


class MainActivityView : CustomComponentActivity() {
   
   val songViewModel: SongsViewModel
      get() {
         return MyApplication.instance.songViewModel
      }
   
   val refreshState = RefreshState()
   
   val hasNotificationPermission by lazy { mutableStateOf(hasPermission(Manifest.permission.POST_NOTIFICATIONS))  }
   
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
   
   
   
   
   val intentSanitizer = IntentSanitizer.Builder()
      .allowAnyComponent()
      .allowAction {
      it == Intent.ACTION_VIEW
   }.allowType {
      it == "audio/*"
   }.build()
   
   
   private suspend fun handleReceivedAudioIntent(intent:Intent){
      
      
      val audioUri = intent.data
      
      val mimeType = intent.type
      
      // You can also get additional flags
      val flags = intent.flags
      Log("Received URI - [${audioUri.toString()}]")
      if (audioUri != null) {
         
         // Create the intent and pass the data to it
         var fullScreenPageIntent = Intent(this, FullScreenPlayerActivity::class.java).apply {
            setDataAndType(audioUri,mimeType)
            setFlags(flags)
         }
         
         // Sanitize the intent since the data is coming from outside
         fullScreenPageIntent = intentSanitizer.sanitize(
            fullScreenPageIntent,
            {
               if (false)
                  TODO()
            }
         )
         // Since this uses data from outside, it is prone to errors and mistakes.
         tryTo {
            val song = songViewModel.getSongFromUri(audioUri,this)
            myExoPlayer.setSong(song)
            startActivity(fullScreenPageIntent)
         }
         
      }
   }
   
   override fun onNewIntent(intent: Intent) {
      super.onNewIntent(intent)
      Log("New Intent received!")
      CoroutineScope(Dispatchers.Main).launch {
         handleReceivedAudioIntent(intent)
      }
   }
   override fun onCreate(savedInstanceState: Bundle?) {
      super.onCreate(savedInstanceState)
      
      
      
      // Setup navbars colors
      defaultSetupNavBars()
      
      /**
       * Try running the query for the first time if i have the permissions
      */
      songViewModel.viewModelScope.launch {
         try {
            songViewModel.resyncALL_withMediaStore(
               context = this@MainActivityView,
               alsoPictures = true,
               forceUpdate = false
            )
         }catch (ex: Exception){
            MyApplication.crashLogger.writeCrashToFile(
               throwable = ex,
               context = this@MainActivityView,
               headerDetails = "Resync with mediaStore failed when called from MainActivityView's onCreate"
            )
         }
      }
      
      /**
       * Remove cyclic referencing by simply setting onRefresh later than the initialization of the state.
       * */
      refreshState.setOnRefreshListener {
         mediaPermissionLauncher.launch()
      }
      
      
      
      setContent {
         SrufTheme {
            renderDefaultBackground()
               
            MainView()
            
         }
      }
      activityScope.launch {
         handleReceivedAudioIntent(intent)
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
       *
       * This is NOT infinitely recursive as State doesn't re-post if the value set is the same, meaning this doesn't rerun if the second launch() comes out as true as well.
       * */
      LaunchedEffect(hasNotificationPermission.value) {
         
         notificationPermissionLauncher.launch()
         
         if(hasNotificationPermission.value ) {
            
            MusicService.handleOnAppStart(contextRef)
            
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
   refreshState: RefreshState,
) {
   val contextRef = LocalContext.current
   
   /**
    * Container for EVERYTHING that can have a [Settings Sheet] in front of it. So basically the entire application
    *
    *
    * THIS EXCLUDES: Other Activities, Notifications, Widgets etc that are not inside the Main Activity View
    * */
   Box(
      modifier = Modifier.fillMaxSize()
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
                  .background(Theme.Sourcesets.SongsBackground())
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
               
               val prefs by lazy {
                  MyPrefs.getPrefs(contextRef, MyPrefs.PrefDirs.VisualData)
               }
               val activity = LocalActivity.current
               
               val refreshBoxState = rememberPullToRefreshState()
               
               
               /**
                * Manages navigation inside the [Main Pager] container.
                * This container lives together with the bellow [Player Bottom Sheet] in the same parent container.
                * */
               MyNavigation(
                  startDestination = {
                     PlayerBottomSheetParent
                  }.invoke(),
                  navController = navController
               ) {
                  
                  
                  composable<PlayerBottomSheetParent> {
                     
                     PlayerBottomSheetParent.invoke(
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
                           
                           val intent = Intent(contextRef, FullScreenPlayerActivity::class.java)
                           activity?.startActivity(intent)
                        }
                     )
                  }
                  
                  // DO NOT RENDER THIS because i moved the FullScreenPlayerPage to its own activity
                  if(false)
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
      
      
      )
      
      
   }
   
}


val tempDrawerColor = MyColor.SemiTransparentDark





