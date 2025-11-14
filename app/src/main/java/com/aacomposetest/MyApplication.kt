package com.aacomposetest

import android.Manifest
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.core.app.ActivityCompat
import com.aacomposetest.MyApplication.MusicNotification.id
import com.aacomposetest.MyApplication.MusicNotification.name
import com.aacomposetest.Services.MusicService

class MyApplication: Application() {
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