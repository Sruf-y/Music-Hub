package com.aacomposetest.Activities

import ExoPlayer.ExoPlayerViewModel
import ExoPlayer.myExoPlayer
import MyViewModels.RefreshState
import MyViewModels.SongsViewModel
import MyViewModels.rememberRefreshState
import Navigation.Global.Destinations.DrawerMainPage
import Navigation.Interior.Destinations.FullscreenPlayer
import Presentation.Navigation.MyNavigation
import SrufComposeComponents.MyModalBottomSheet
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
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.aacomposetest.ui.theme.SrufTheme
import com.composables.core.SheetDetent
import com.composables.core.rememberBottomSheetState
import kotlinx.coroutines.launch
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.aacomposetest.Classes.MyPermissionLauncher
import com.aacomposetest.Logs
import com.aacomposetest.Services.MusicService
import com.aacomposetest.ui.theme.MyColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.withContext
import ui.components.BottomSheets.rememberBottomSheetConfig

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

        val globalBottomSheetVM :MyModalBottomSheet.ModalVM = viewModel()
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
            globalBottomSheetVM = globalBottomSheetVM,
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
            viewModel = globalBottomSheetVM,
            sheetState = modalSettingsSheetState,
            bottomSheetContent = globalBottomSheetVM.content.value
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
    val globalNavigationController = rememberNavController()
    /**
     * Container for EVERYTHING that can have a [Settings Sheet] in front of it. So basically the entire application
     *
     *
     * THIS EXCLUDES: Other Activities, Notifications, Widgets etc that are not inside the Main Activity View
     * */
    Surface(
        modifier = Modifier.fillMaxSize(), color = tempBackgroundColor
    ) {

        val songBS_Config = rememberBottomSheetConfig()

        val songPlayerBS_State = rememberBottomSheetState(
            initialDetent = songBS_Config.peekDetent?: SheetDetent.Hidden,
            detents = songBS_Config.detents
        )

        MyNavigation(
            startDestination = DrawerMainPage,
            navController = globalNavigationController
        ) {
            composable<DrawerMainPage>() {
                DrawerMainPage.invoke(
                    activity = activity,
                    exoPlayerVM = exoPlayerVM,
                    globalBottomSheetVM = globalBottomSheetVM,
                    refreshState = refreshState,
                    songPlayerBS_State = songPlayerBS_State, songBS_Config = songBS_Config,
                    openFullscreenSong = {
                        globalNavigationController.navigate(FullscreenPlayer)
                    }
                )
            }

            composable<FullscreenPlayer>(
                enterTransition = {

                    slideIntoContainer(
                        AnimatedContentTransitionScope.SlideDirection.Up,
                        animationSpec = tween(250, easing = EaseInOut)
                    )
                }, exitTransition = {
                    slideOutOfContainer(
                        AnimatedContentTransitionScope.SlideDirection.Down,
                        animationSpec = tween(250)
                    )
                },
                popExitTransition = {
                    slideOutOfContainer(
                        AnimatedContentTransitionScope.SlideDirection.Down,
                        animationSpec = tween(250)
                    )
                }
            ) {
                Box(modifier = Modifier.fillMaxSize().safeDrawingPadding()){
                    FullscreenPlayer.invoke()
                }

            }

        }



    }
}




val tempBackgroundColor = MyColor.SemiTransparentLight

val tempDrawerColor = MyColor.SemiTransparentDark





