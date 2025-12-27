package com.aacomposetest

import android.Manifest
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.core.app.ActivityCompat
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import coil.util.DebugLogger
import com.aacomposetest.MyApplication.MusicNotification.id
import com.aacomposetest.MyApplication.MusicNotification.name
import com.aacomposetest.Services.MusicService

class MyApplication: Application(), ImageLoaderFactory {
    override fun onCreate() {
        super.onCreate()


        val channel = NotificationChannel(
            id,
            name,
            NotificationManager.IMPORTANCE_DEFAULT
        )


        Log.i(Logs.RUNNING_INFO.name,"Notification channel created.")

        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)

        instance = this
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


        private var _serviceInstance: MusicService? = null
        val serviceInstance: MusicService?
            get() = _serviceInstance

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