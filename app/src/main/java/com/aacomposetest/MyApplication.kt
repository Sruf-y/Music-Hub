package com.aacomposetest

import android.Manifest
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.os.Environment
import android.os.Process
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.PendingIntentCompat
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import com.aacomposetest.MyApplication.MusicNotification.id
import com.aacomposetest.MyApplication.MusicNotification.name
import com.aacomposetest.Services.MusicService
import java.io.File

class MyApplication: Application(), ImageLoaderFactory {
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onCreate() {
        super.onCreate()

        instance = this

        val channel = NotificationChannel(
            id,
            name,
            NotificationManager.IMPORTANCE_DEFAULT
        )


        Log.i(Logs.RUNNING_INFO.name,"Notification channel created.")

        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            // Log the crash
            crashLogger.writeCrashToFile(throwable,this)
            allertPoster.postNotification(
                title = "Error encountered",
                message = "Stacktrace was written to ${crashLogger.folder}",
                context = this
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