package com.aacomposetest.Services

import ExoPlayer.ExoPlayerViewModel
import ExoPlayer.myExoPlayer
import android.app.ActivityManager
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.aacomposetest.MyApplication
import com.aacomposetest.R
import androidx.media.app.NotificationCompat.MediaStyle
import com.aacomposetest.Activities.MainActivityView
import com.aacomposetest.Classes.AutoStateFlow
import com.aacomposetest.Functions
import com.aacomposetest.Logs
import kotlinx.coroutines.launch

class MusicService: LifecycleService() {

    lateinit var mediaSession: MediaSessionCompat

    val exoPlalayerVM = ExoPlayerViewModel()


    override fun onBind(intent: Intent): IBinder? {
        return super.onBind(intent)
    }

    var startId:Int = 0
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        this.startId = startId
        Log.i(Logs.SERVICE.name,"Called onStartCommand.")

        when(intent?.action){
            Actions.START.name->{

                start()

                return START_NOT_STICKY
            }

            Actions.KILLSERVICE.name->{
                stopSelf()
            }

            else -> {

                Log.e(Logs.SERVICE.name,"Service onStartCommand was passed an Action which is either INVALID or not handled.")
            }
        }
        return START_NOT_STICKY

    }

    override fun onCreate() {
        super.onCreate()

        exoPlalayerVM.progressVM.startUpdates()

        MyApplication.registerServiceInstance(this)


        val actionsCallback = object: MediaSessionCompat.Callback(){
            override fun onCustomAction(action: String, extras: Bundle?) {

                when(action){
                    Actions.STOP.name->{

                        // If task is not on the tasktray, stop the service, marking the proccess for deletion
                       if(!isAppTaskStillExists()){

                           myExoPlayer.destroy()

                           // when its paused, it becomes a background service. StopForeground will not work on a background service. I need to manually cancel the notification.
                           val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                           notificationManager.cancel(notification_id)

                           stopSelf()
                       }else{
                           stopForeground(STOP_FOREGROUND_REMOVE)
                       }

                    }
                    Actions.FAVORITE.name->{

                        // logic for making the song in the database be favorited, and thus also adding it to Favorites playlist
                        // does nothing really for now

                    }
                }

            }

            override fun onPlay() {
                exoPlalayerVM.toggle()
            }

            override fun onPause() {
                exoPlalayerVM.toggle()
            }

            override fun onSeekTo(pos: Long) {
                exoPlalayerVM.seekTo(pos)
            }

            override fun onSkipToNext() {
                exoPlalayerVM.playNext()
            }

            override fun onSkipToPrevious() {
                exoPlalayerVM.playPrevious()
            }

            override fun onRewind() {
                exoPlalayerVM.rewind()
            }

        }

        mediaSession = MediaSessionCompat(this,mediaSessionTag).apply {
            setCallback(actionsCallback)
            isActive = true
        }


        // Launch observer
        lifecycleScope.launch {

            Log.i(Logs.SERVICE.name, "Launched an observer for exoplayer.isPlaying.")

            exoPlalayerVM.isPlaying.collect {

                Log.i(Logs.SERVICE.name, "isPlaying value is ${it}")

                if (it) exoPlalayerVM.progressVM.startUpdates() else exoPlalayerVM.progressVM.stopUpdates()


                playPauseLogic(it,this@MusicService)

            }

        }

        Log.i(Logs.SERVICE.name,"Called onCreate.")
    }



    override fun onDestroy() {
        super.onDestroy()
        exoPlalayerVM.progressVM.stopUpdates()

        notificationState.setValue(NotificationState.Dead)

        MyApplication.unregisterServiceInstance()


    }



    fun CreateNotification(): Notification {



        val favoriteDrawable =
            if (false) R.drawable.baseline_favorite_24 else R.drawable.baseline_favorite_border_24

        val mainActivityIntent = Intent(this@MusicService, MainActivityView::class.java).apply {
            // Clear out any existing task and reuse it
            flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            this@MusicService,
            0,
            mainActivityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )


        val song_art = {
            val file = myExoPlayer.currentlyPlayingSong.value?.tryGetThumbnail(this@MusicService)

            if (file?.exists() == true) {
                val bytes = file.readBytes()
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            } else {
                Functions.Images.getBitmapFromDrawable(
                    this@MusicService,
                    R.drawable.ic_gray_musical_note
                )
            }
        }





        val mediaSessionMetadata = MediaMetadataCompat.Builder()
            .putString(
                MediaMetadataCompat.METADATA_KEY_TITLE,
                exoPlalayerVM.currentSong.value?.title ?: "<unknown>"
            )
            .putString(
                MediaMetadataCompat.METADATA_KEY_ARTIST,
                exoPlalayerVM.currentSong.value?.artistName ?: "<unknown>"
            )
            .putLong(
                MediaMetadataCompat.METADATA_KEY_DURATION,
                exoPlalayerVM.currentSong.value?.duration ?: 0
            )
            .putBitmap(
                MediaMetadataCompat.METADATA_KEY_ART,
                song_art()
            )
            .build()

        mediaSession.setPlaybackState(
            PlaybackStateCompat.Builder()
                .setState(
                    if (exoPlalayerVM.isPlaying.value) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED,
                    myExoPlayer.getCurrentPosition(),
                    1.0f
                )

                .setActions(PlaybackStateCompat.ACTION_SKIP_TO_NEXT)
                .addCustomAction(
                    Actions.FAVORITE.name,
                    "favorite_button",
                    favoriteDrawable
                )
                .setActions(
                    PlaybackStateCompat.ACTION_SEEK_TO or
                            PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                            PlaybackStateCompat.ACTION_PLAY_PAUSE or
                            PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                )
                .addCustomAction(
                    Actions.STOP.name,
                    "x_button",
                    R.drawable.x
                )
                .build()
        )
        mediaSession.setMetadata(mediaSessionMetadata)

        val mediaStyle = MediaStyle()
            .setMediaSession(mediaSession.sessionToken)
            .setShowActionsInCompactView(0, 1, 2)



        return NotificationCompat
            .Builder(this@MusicService, MyApplication.MusicNotification.id)
            .setSmallIcon(R.drawable.ic_appicon)
            .setStyle(mediaStyle)
            .setOngoing(true)
            .setContentIntent(openAppPendingIntent)
            .setContentTitle("Title")
            .setContentText("Content text")
            .build()





    }


    fun start(onFinish: () -> Unit = {}){
        // Redundant initialization of the player just in case
        myExoPlayer.initializePlayer(this)

        StartForegroundNotification(CreateNotification())
    }

    fun StartForegroundNotification(notification:Notification){
        ServiceCompat.startForeground(this@MusicService,
            notification_id,
            notification,
            ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
        )
    }

    fun UpdateNotification(notification:Notification){
        val notifManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notifManager.notify(notification_id,notification)
    }






    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)

        Log.i(Logs.SERVICE.name,"Task removed")



        if(!exoPlalayerVM.isPlaying.value)
            sendToBackground(this)

    }


    enum class Actions{
        START,
        STOP, // only used in notification button
        KILLSERVICE, // unused
        FAVORITE
    }


    var notificationState = AutoStateFlow<NotificationState>(NotificationState.Dead)

    fun playPauseLogic(isPlaying:Boolean = myExoPlayer.isPlaying.value,context:Context = MyApplication.instance){

        if (isPlaying || isAppTaskStillExists()) {
            sendToForeground(context)
        } else {
            sendToBackground(context)
        }

        UpdateNotification(CreateNotification())
    }

    fun sendToForeground(context: Context){
        start()

        notificationState.setValue(NotificationState.Foreground)
    }

    fun sendToBackground(context: Context){
        if(notificationState.stateFlow.value == NotificationState.Foreground){

            stopForeground(STOP_FOREGROUND_DETACH)

            notificationState.setValue(NotificationState.Background)
        }else{
            Log.e(Logs.SERVICE.name,"Attempted to send service to background in invalid state [${notificationState.stateFlow.value.name}]")
        }
    }



    companion object{
        enum class NotificationState {
            Foreground,
            Background,
            Dead
        }

        fun handleOnAppStart(context: Context){
            if(MyApplication.serviceInstance == null){
                val intent = Intent(context, MusicService::class.java).apply {
                    action = MusicService.Actions.START.toString()
                }
                context.startForegroundService(intent)
            }else if(MyApplication.serviceInstance!!.notificationState.stateFlow.value == NotificationState.Background){
                MyApplication.serviceInstance!!.sendToForeground(context)
            }
        }


        fun isAppTaskStillExists(): Boolean {
            val activityManager = getSystemService(MyApplication.instance, ActivityManager::class.java) as ActivityManager
            val appTasks = activityManager.appTasks

//            appTasks.forEach {
//                Log.i("WTF",it.taskInfo.topActivity?.className?:"Nothing.")
//            }
            return appTasks.isNotEmpty()
        }


        const val notification_id = 106
        const val mediaSessionTag = "MusicPlayer"
    }

}