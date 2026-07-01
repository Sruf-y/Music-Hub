package com.aacomposetest

import AppDatabase.AndroidDatabase
import AppDatabase.MyDatabaseProvider
import AppDatabase.PlaylistRelated.Playlist
import ExoPlayer.myExoPlayer
import MyViewModels.FileWatcher
import MyViewModels.SongsViewModel
import Settings.MySettings
import android.Manifest
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.os.Process
import android.provider.MediaStore
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import com.aacomposetest.Activities.MainActivityView
import com.aacomposetest.Classes.NotificationPoster
import com.aacomposetest.MyApplication.MusicNotification.id
import com.aacomposetest.MyApplication.MusicNotification.name
import com.aacomposetest.Classes.NotificationPoster.Companion.openMediaStoreUriPendingIntent
import com.aacomposetest.Services.MusicService
import com.aacomposetest.Services.ServiceBindManager
import com.google.android.material.snackbar.BaseTransientBottomBar
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json

class MyApplication: Application(), ImageLoaderFactory {
    
    
    lateinit var songViewModel: SongsViewModel
        private set
    
    var mediastoreObserver: ContentObserver? = null
        private set
    
    
    override fun onCreate() {
        super.onCreate()

        instance = this
        myExoPlayer.initializePlayer(this)
        songViewModel = SongsViewModel(this)

        val musicNotificationChannel = NotificationChannel(
            id,
            name,
            NotificationManager.IMPORTANCE_DEFAULT
        )
        Log.i(Logs.RUNNING_INFO.name,"Notification channel created.")
        getSystemService(NotificationManager::class.java).createNotificationChannel(musicNotificationChannel)
        
        MyDatabaseProvider.getDatabase(this)
        /**
         * Hook database to MediaStore database
         */
        mediastoreObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                super.onChange(selfChange)
                
                with(songViewModel){
                    viewModelScope.launch {
                        resyncALL_withMediaStore(this@MyApplication, alsoPictures = true, forceUpdate = false){
                        }
                    }
                }
            }
        }.apply {
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
         * Lazily wake service for the first time
         * */
        val musicServiceBinder = ServiceBindManager(MusicService::class, this)
            .apply {
                this.onServiceReady = {
                    serviceInstance = it as MusicService
                }
                
                this.onServiceFailed = {
                    serviceInstance = null
                }
            }
        MySettings.initialize(this)
        
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            // Log the crash
            Log("Logged crash to ${crashLogger.folder.name}")
            
            val uri = crashLogger.writeCrashToFile(throwable,this)
            allertPoster.postNotification(
                title = "Error encountered",
                message = "Stacktrace was written to ${crashLogger.folder.path}",
                context = this,
                pendingIntent = openMediaStoreUriPendingIntent(this,uri)
            )
        }
        
        /**
         * Try running the query for the first time if i have the permissions
         */
        songViewModel.viewModelScope.launch {
            try {
                songViewModel.resyncALL_withMediaStore(
                    context = this@MyApplication,
                    alsoPictures = true,
                    forceUpdate = true
                )
            }catch (ex: Exception){
                
                MyApplication.crashLogger.writeCrashToFile(
                    throwable = ex,
                    context = this@MyApplication,
                    headerDetails = "Resync with mediaStore failed when called from MainActivityView's onCreate"
                )
            }
        }
        
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader(this).newBuilder()
            .memoryCachePolicy(CachePolicy.ENABLED)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.1)
                    .strongReferencesEnabled(true)
                    .weakReferencesEnabled(true)
                    .build()
            }
            .diskCachePolicy(CachePolicy.ENABLED)
            .diskCache {
                DiskCache.Builder()
                    .maxSizePercent(0.03)
                    .directory(cacheDir)
                    .build()
            }
            //.logger(DebugLogger()) // ONLY for when i NEED a logger otherwise it clogs the logs.
            .build()
    }
    
    companion object{
        lateinit var instance: MyApplication
            private set

        val allertPoster by lazy {
            NotificationPoster(
                context = instance,
                channelId = "AlertPosterChannelId#0001",
                channelName = "AllertPoster",
                //pendingIntent = PendingIntentCompat.
            ).apply {
                setGroup("Allerts","AlertPosterGroupId#0001")
            }
        }
        
        val playlistFolderWatcher:FileWatcher by lazy { FileWatcher(
            Playlist.getFolder(instance)
        ).apply {
            startWatching()
        } }

        val crashLogger by lazy { CrashReporter(foldername = "CrashLogs_${instance.getString(R.string.app_name)}") }
        
        
        
        
        var serviceInstance: MusicService? = null
            private set
        
        fun killProccess(){
            android.os.Process.killProcess(Process.myPid())
        }
        
        fun showSystemAlert(
            parentContext: Context,  // Any Activity, not just AppCompatActivity
            title: String,
            message: String,
            onOkTitle:String = "Ok",
            onOk: () -> Unit = {},
            onCancel:(()->Unit)? = null
        ) {
            android.app.AlertDialog.Builder(
                parentContext,
                android.R.style.Theme_DeviceDefault_Dialog_Alert
            )
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(onOkTitle) { dialog, _ ->
                    dialog.dismiss()
                    onOk()
                }.let {
                    if(onCancel!=null){
                        it.setNegativeButton(android.R.string.cancel) { dialog,_->
                            dialog.dismiss()
                            onCancel()
                        }
                    }else{
                        it
                    }
                }
                .setCancelable(false)
                .create()
                .apply {
                    // System dialog styling
                    window?.attributes?.width = WindowManager.LayoutParams.MATCH_PARENT
                }
                .show()
            
        }
        
        @OptIn(ExperimentalSerializationApi::class)
        val JsonFormat = Json {
            prettyPrint = true
            prettyPrintIndent = "   "
        }
    }
    
    object Permissions {
        val POST_NOTIFICATIONS = Manifest.permission.POST_NOTIFICATIONS
        val READ_MEDIA_AUDIO = Manifest.permission.READ_MEDIA_AUDIO
    }

    object MusicNotification{
        const val id = "music channel"
        const val name = "Music Player"

    }
    
    
}

private fun handleInternalLog(message:String, tag:String, type:String){
    when(type){
        "e"->{
            Log.e(tag,message)
        }
        "i"->{
            Log.i(tag,message)
        }
        "d"->{
            Log.d(tag,message)
        }
    }
}
fun Log(
    message: String,
    tag: Any = "WTF",  // Accept either String or Logs enum
    type: String = "e"
) {
    val tagString = when (tag) {
        is String -> tag
        is Logs -> tag.name
        else -> "WTF"
    }
    handleInternalLog(message, tagString, type)
}
fun Toast(
    context: Context,
    text:String,
    @BaseTransientBottomBar.Duration duration:Int = Toast.LENGTH_SHORT,
    showInProd:Boolean = false
){
    if(showInProd) {
        Toast.makeText(
            context,
            text,
            duration
        ).show()
    }
}
/**
 * @param throwAnyway will first throw the exception anyway THEN call onError. This is to ensure the error is first NOT handled. Just for testing purposes this can be enabled
 * */
inline fun tryTo(
    onError: (Exception) -> Unit = {
        it.printStackTrace()
    },
    throwAnyway:Boolean = false,
    action: () -> Unit,
) {
    if(!throwAnyway){
        try {
            action.invoke()
        }catch (ex: Exception){
            onError.invoke(ex)
        }
    }else{
        try {
            action.invoke()
        }catch (ex: Exception){
            throw ex
            onError.invoke(ex)
        }
       
    }
    
}

inline fun <T>tryValue(
    onError: (Exception) -> T,
    action: () -> T
):T{
    return try {
        action.invoke()
    }catch (ex: Exception){
        onError.invoke(ex)
    }
}

