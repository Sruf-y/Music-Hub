package com.aacomposetest.Activities

import ExoPlayer.myExoPlayer
import MyViewModels.RefreshState
import MyViewModels.SongsViewModel
import Navigation.Destinations.EqualizerDest
import Navigation.Destinations.MainPager
import Navigation.Destinations.PlayerBottomSheetContainer
import Navigation.Destinations.PlaylistAddSongsPage
import Navigation.NavExtensions.safeNavigate
import Navigation.SafeDrawer
import Presentation.Navigation.DrawerStandardLayout
import Presentation.Navigation.MyNavigation
import Presentation.Settings.EqualizerPage
import SrufComposeComponents.Text
import TestLayouts.TestActivityCore
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.EaseInOutElastic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import MyViewModels.MyModalBottomSheet
import ui.components.Pages.AddSongsToPlaylistPage
import MyViewModels.SheetViewmodel
import Navigation.Destinations.SettingsDest
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Icon
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.sp
import com.aacomposetest.R
import com.aacomposetest.hasPermission
import com.aacomposetest.toDpSize
import ui.components.ViewDrawerValue
import ui.components.rememberViewDrawerState


class MainActivityView : CustomComponentActivity() {
   
   
   
   
   
   val songViewModel: SongsViewModel
      get() {
         return MyApplication.instance.songViewModel
      }
   
   val refreshState = RefreshState()
   
   val hasNotificationPermission by lazy { mutableStateOf(hasPermission(MyApplication.Permissions.POST_NOTIFICATIONS))  }
   
   val notificationPermissionLauncher = createPermissionLauncher(
      permission = MyApplication.Permissions.POST_NOTIFICATIONS,
      onFinish = {result->
         if(hasNotificationPermission.value != result) {
            hasNotificationPermission.value = result
         }
      }
   )
   
   
   val mediaPermissionLauncher = createPermissionLauncher(
      permission = MyApplication.Permissions.READ_MEDIA_AUDIO,
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
      this.defaultSetupNavBars()
      
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
            
            Theme.RenderDefaultBackground()
            
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
         
         if(hasNotificationPermission.value) {
            
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
      // Set background
      
      
      val drawerState = rememberViewDrawerState(initialValue = ViewDrawerValue.Closed)
      //val allSongs by ListFeedingVM.allSongs.collectAsState()
      
      val navController = rememberNavController()
      
      val playerBS_mainContentNavController = rememberNavController()
      
      SafeDrawer(
         state = drawerState,
         drawerContent = {
            Box(Modifier.wrapContentSize()){
               var intsize by remember { mutableStateOf(IntSize(0,0)) }
               val density = LocalDensity.current
               
               Theme.RenderDefaultBackground(
                  Modifier
                     .size(intsize.toDpSize(density = density))
                     .graphicsLayer {
                        this.alpha = drawerState.currentOffset
                     },
                  alpha = 0f
               )
               
               DrawerStandardLayout(
                  modifier = Modifier
                     .fillMaxHeight()
                     .wrapContentWidth()
                     .onSizeChanged{
                        intsize = it
                     }
                     .graphicsLayer {
                        this.alpha = drawerState.currentOffset
                     }
                     .pointerInput(Unit) {
                        detectTapGestures() // to not close the drawer on any normal tap inside it
                     }
                     .padding(
                        start = 10.dp,
                        top = 10.dp,
                        end = 10.dp
                     )
                     
                     
               ) {
                  
                  stickyHeader {
                     Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(painterResource(
                           id = R.drawable.ic_appicon),
                           tint = Theme.IconTint(),
                           contentDescription = null,
                           modifier = Modifier.size(60.dp)
                        )
                        Text(stringResource(R.string.app_name), fontSize = 36.sp, fontWeight = FontWeight.Bold)
                     }
                  }
                  item(key = "settings"){
                     Button({
                        navController.safeNavigate(SettingsDest)
                        drawerState.close()
                     }) {
                        Text("Settings")
                     }
                  }
                  item(key = "equalizer"){
                     // Your item content
                     Button({
                        navController.safeNavigate(EqualizerDest)
                        drawerState.close()
                     }) {
                        Text("Open Equalizer")
                     }
                  }
                  
                  item(key = "Tests") {
                     Button({
                        val intent = Intent(contextRef, TestActivityCore::class.java)
                        contextRef.startActivity(intent)
                     }){
                        Text("Tests")
                     }
                     
                  }
               }
            }
            
            
         },
         mainContent = {
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
                  .navigationBarsPadding()
                  .displayCutoutPadding()
                  .systemBarsPadding()
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
                     PlayerBottomSheetContainer
                  }.invoke(),
                  navController = navController
               ) {
                  
                  
                  composable<PlayerBottomSheetContainer> {
                     
                     PlayerBottomSheetContainer.RenderBottomSheetContainer(
                        startDestination = MainPager,
                        prefs = prefs,
                        localNavController = playerBS_mainContentNavController,
                        refreshBoxState = refreshBoxState,
                        refreshState = refreshState,
                        globalBottomSheetVM = globalBottomSheetVM,
                        drawerState = drawerState,
                        onClickAddPlaylist = { args ->
                           navController.safeNavigate(PlaylistAddSongsPage(args.title))
                        },
                        onBottomSheetClick = {
                           val intent = Intent(contextRef, FullScreenPlayerActivity::class.java)
                           activity?.startActivity(intent)
                        }
                     )
                  }
                  
                  composable<SettingsDest>{
                     SettingsDest.Page()
                  }
                  
                  composable<EqualizerDest>(
                     enterTransition = {
                        val duration = 250
                        slideIntoContainer(
                           AnimatedContentTransitionScope.SlideDirection.Left,
                           animationSpec = tween(duration, easing = EaseInOut)
                        )
                     },
                     popExitTransition = {
                        val duration = 250
                        slideOutOfContainer(
                           AnimatedContentTransitionScope.SlideDirection.Right,
                           animationSpec = tween(duration, easing = EaseInOut)
                        )
                     }
                  ) {
                     if(myExoPlayer.equalizerController!=null)
                        EqualizerPage(
                           equalizer = myExoPlayer.equalizerController!!
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





