package com.aacomposetest

import AppDatabase.PlaylistRelated.Playlist
import ExoPlayer.myExoPlayer
import MyViewModels.FileWatcher
import MyViewModels.LyricsManager
import android.Manifest
import android.app.Activity
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Process
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.widget.TextView
import androidx.annotation.RequiresPermission
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import com.aacomposetest.MyApplication.MusicNotification.id
import com.aacomposetest.MyApplication.MusicNotification.name
import com.aacomposetest.NotificationPoster.Companion.openMediaStoreUriPendingIntent
import com.aacomposetest.Services.MusicService
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json

class MyApplication: Application(), ImageLoaderFactory {
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onCreate() {
        super.onCreate()

        instance = this
        myExoPlayer.initializePlayer(this)

        val musicNotificationChannel = NotificationChannel(
            id,
            name,
            NotificationManager.IMPORTANCE_DEFAULT
        )


        Log.i(Logs.RUNNING_INFO.name,"Notification channel created.")

        getSystemService(NotificationManager::class.java).createNotificationChannel(musicNotificationChannel)

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            // Log the crash
            Log(crashLogger.folder.name)
            
            val uri = crashLogger.writeCrashToFile(throwable,this)
            allertPoster.postNotification(
                title = "Error encountered",
                message = "Stacktrace was written to ${crashLogger.folder.path}",
                context = this,
                pendingIntent = openMediaStoreUriPendingIntent(this,uri)
            )
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
        lateinit var instance: Application
            private set

        val allertPoster by lazy {
            NotificationPoster(
                context = instance,
                channelId = "53453345432",
                channelName = "AllertPoster",
                //pendingIntent = PendingIntentCompat.
            ).apply {
                setGroup("Allerts","543885823")
            }
        }
        
        val playlistFolderWatcher:FileWatcher by lazy { FileWatcher(
            Playlist.getFolder(instance)
        ).apply {
            startWatching()
        } }
        
        val lyricsFolderWatcher:FileWatcher by lazy { FileWatcher(
            LyricsManager.getDir(instance)
        ).apply {
            startWatching()
        } }

        val crashLogger by lazy { CrashReporter(foldername = "CrashLogs_${instance.getString(R.string.app_name)}") }


        private var _serviceInstance: MusicService? = null
        val serviceInstance: MusicService?
            get() = _serviceInstance

        fun killProccess(){
            android.os.Process.killProcess(Process.myPid())
        }


        fun registerServiceInstance(service: MusicService) {
            _serviceInstance = service
        }

        fun unregisterServiceInstance() {
            _serviceInstance = null
        }
        
        fun showSystemAlert(
            activity: Activity,  // Any Activity, not just AppCompatActivity
            title: String,
            message: String,
            onOk: () -> Unit = {},
            onCancel:(()->Unit)? = null
        ) {
            android.app.AlertDialog.Builder(
                activity,
                android.R.style.Theme_DeviceDefault_Dialog_Alert
            )
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok) { dialog, _ ->
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

    object MusicNotification{
        const val id = "music channel"
        const val name = "Music Player"

    }
    
    
}

fun Log(message:String,type:String = "i"){
    when(type){
        "e"->{
            Log.e("WTF",message)
        }
        "i"->{
            Log.i("WTF",message)
        }
    }

}

fun tryTo(action:()->Unit){
    try {
       action.invoke()
    }catch (ex: Exception){}
}

