package com.aacomposetest.Services

import ExoPlayer.ExoPlayerViewModel
import ExoPlayer.myExoPlayer
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
import androidx.core.content.edit
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.aacomposetest.MyApplication
import com.aacomposetest.R
import androidx.media.app.NotificationCompat.MediaStyle
import com.aacomposetest.Activities.MainActivityView
import com.aacomposetest.Functions
import com.aacomposetest.Logs
import kotlinx.coroutines.launch

class MusicService: LifecycleService() {

    lateinit var mediaSession: MediaSessionCompat

    val exoPlalayerVM = ExoPlayerViewModel()



    override fun onBind(intent: Intent): IBinder? {
        return super.onBind(intent)
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        Log.i(Logs.SERVICE.name,"Called onStartCommand.")

        when(intent?.action){
            Actions.START.toString()->{
                start()
                return START_REDELIVER_INTENT
            }
            Actions.START_BACKGROUND.name.toString()->{

                UpdateNotification(CreateNotification())
                //stopForeground(STOP_FOREGROUND_DETACH)


                return START_NOT_STICKY
            }
            Actions.STOP.toString()->{
                // If task is not present, kill service
                stopNotification(killService = !taskIsPresent)

            }

            Actions.KILLSERVICE.toString()->{
                stopSelf()
            }

            else -> {

            }
        }
        return START_NOT_STICKY

    }

    override fun onCreate() {
        super.onCreate()

        exoPlalayerVM.progressVM.startUpdates()

        taskIsPresent = true



        val actionsCallback = object: MediaSessionCompat.Callback(){
            override fun onCustomAction(action: String, extras: Bundle?) {

                when(action){
                    Actions.STOP.name->{
                        stopNotification(killService = !taskIsPresent)
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

                if(!taskIsPresent)
                    stopForeground(STOP_FOREGROUND_DETACH)
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

                if(isStarted) {
                    if (it) {
                        val intent = Intent(this@MusicService, MusicService::class.java).apply {
                            action = MusicService.Actions.START.toString()
                        }
                        this@MusicService.startForegroundService(intent)
                    } else {
                        val intent = Intent(this@MusicService, MusicService::class.java).apply {
                            action = MusicService.Actions.START_BACKGROUND.toString()
                        }
                        this@MusicService.startForegroundService(intent)
                    }
                }

            }

        }


        Log.i(Logs.SERVICE.name,"Called onCreate.")
    }



    override fun onDestroy() {

        exoPlalayerVM.progressVM.stopUpdates()

        super.onDestroy()
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

        isStarted = true


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




    fun stopNotification(killService:Boolean = false){


        if(killService){
            isStarted = false

            StartForegroundNotification(CreateNotification())
            stopForeground(STOP_FOREGROUND_REMOVE)

            if (!taskIsPresent)
                exoPlalayerVM.destroyPlayer()

            stopSelf()
        }
        else{

            stopForeground(STOP_FOREGROUND_REMOVE)
        }


    }



    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)

        Log.i(Logs.SERVICE.name,"Task removed")

        taskIsPresent = false



        if(!exoPlalayerVM.isPlaying.value)
            stopForeground(STOP_FOREGROUND_DETACH)

    }


    enum class Actions{
        START,
        START_BACKGROUND,
        STOP,
        KILLSERVICE,
        FAVORITE
    }

    companion object{
        var isStarted = false

        // This service can ONLY be launched from the task
        var taskIsPresent = true

        val notification_id = 106
        val mediaSessionTag = "MusicPlayer"
    }

}