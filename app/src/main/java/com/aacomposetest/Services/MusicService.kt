package com.aacomposetest.Services

import ExoPlayer.ExoPlayerViewModel
import ExoPlayer.MyPlayerListener
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
import com.aacomposetest.Functions
import com.aacomposetest.Logs
import kotlinx.coroutines.launch

class MusicService: LifecycleService() {
   
   override fun onBind(intent: Intent): IBinder? {
      return super.onBind(intent)
   }
   
   lateinit var mediaSession: MediaSessionCompat
   
   
   override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
      super.onStartCommand(intent, flags, startId)
      
      
      Log.i(Logs.SERVICE.name,"Called onStartCommand.")
      
      when(intent?.action){
         Actions.START.name->{
            sendToForeground()
         }
         Actions.STOP.name->{
            
            onKillInvoked()
            
         }
         else -> {
            MyApplication.crashLogger.writeTextToFile(
               text  ="""
                        Service onStartCommand was passed an [Action] which is either INVALID or not handled.
                    """.trimIndent(),
               filename = "Service OnStart error",
               context = this
            )
         }
      }
      return START_NOT_STICKY
      
   }
   
   val playerListener = object : MyPlayerListener{
      override fun onToggle(value: Boolean) {
         playPauseLogic(value)
      }
      
      override fun onStart() {
      
      }
      
      override fun onPause() {
      
      }
      
      override fun onSongSet() {
      
      }
      
      override fun onSeek() {
         UpdateNotification(CreateNotification())
      }
      
   }
   
   override fun onCreate() {
      super.onCreate()
      
      MyApplication.registerServiceInstance(this)
      
      myExoPlayer.registerListener(playerListener)
      
      
      val actionsCallback = object: MediaSessionCompat.Callback(){
         override fun onCustomAction(action: String, extras: Bundle?) {
            
            when(action){
               PlayerCustomActions.CLOSE.name->{
                  
                  onKillInvoked()
                  
               }
               PlayerCustomActions.FAVORITE.name->{
                  
                  // logic for making the song in the database be favorited, and thus also adding it to Favorites playlist
                  // does nothing really for now
                  
                  UpdateNotification(CreateNotification())
               }
            }
            
         }
         
         override fun onPlay() {
            ExoPlayerViewModel.toggle()
         }
         
         override fun onPause() {
            ExoPlayerViewModel.toggle()
         }
         
         override fun onSeekTo(pos: Long) {
            ExoPlayerViewModel.seekTo(pos)
         }
         
         override fun onSkipToNext() {
            ExoPlayerViewModel.playNext()
         }
         
         override fun onSkipToPrevious() {
            ExoPlayerViewModel.playPrevious()
         }
         
         override fun onRewind() {
            ExoPlayerViewModel.rewind()
         }
         
      }
      
      mediaSession = MediaSessionCompat(this,mediaSessionTag).apply {
         setCallback(actionsCallback)
         isActive = true
      }
      
      
      
      Log.i(Logs.SERVICE.name,"Called onCreate.")
   }
   
   override fun onTaskRemoved(rootIntent: Intent?) {
      super.onTaskRemoved(rootIntent)
      
      Log.i(Logs.SERVICE.name,"Task removed")
      
      
      
      if(!myExoPlayer.isPlaying.value)
         sendToBackground()
      
   }
   
   override fun onDestroy() {
      super.onDestroy()
      removeNotification()
      
      MyApplication.unregisterServiceInstance()
      
      myExoPlayer.unRegisterListener(playerListener)
      
   }
   
   
   
   
   fun removeNotification(){
      val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
      notificationManager.cancel(notification_id)
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
         val file = myExoPlayer.currentlyPlayingSong.stateFlow.value?.tryGetThumbnail(this@MusicService)
         
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
            myExoPlayer.currentlyPlayingSong.getValue?.title ?: "<unknown>"
         )
         .putString(
            MediaMetadataCompat.METADATA_KEY_ARTIST,
            myExoPlayer.currentlyPlayingSong.getValue?.artistName ?: "<unknown>"
         )
         .putLong(
            MediaMetadataCompat.METADATA_KEY_DURATION,
            myExoPlayer.currentlyPlayingSong.getValue?.duration ?: 0
         )
         .putBitmap(
            MediaMetadataCompat.METADATA_KEY_ART,
            song_art()
         )
         .build()
      
      mediaSession.setPlaybackState(
         PlaybackStateCompat.Builder()
            .setState(
               if (myExoPlayer.isPlaying.value) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED,
               myExoPlayer.getCurrentPosition(),
               1.0f
            )
            
            .setActions(PlaybackStateCompat.ACTION_SKIP_TO_NEXT)
            .addCustomAction(
               PlayerCustomActions.FAVORITE.name,
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
               PlayerCustomActions.CLOSE.name,
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
   
   
   
   
   
   
   
   
   
   fun playPauseLogic(isPlaying:Boolean = myExoPlayer.isPlaying.value){
      
      if ((isPlaying || isAppTaskStillExists()) && currentLayerState!= LayerState.Foreground) {
         sendToForeground()
      } else if(currentLayerState == LayerState.Foreground) {
         sendToBackground()
      }
      
      UpdateNotification(CreateNotification())
   }
   
   fun sendToForeground(){
      StartForegroundNotification(CreateNotification())
      currentLayerState = LayerState.Foreground
   }
   
   fun sendToBackground(){
      stopForeground(STOP_FOREGROUND_DETACH)
      currentLayerState = LayerState.BackGround
   }
   
   enum class Actions{
      START,
      STOP
   }
   private fun onKillInvoked(){
      // If task is not on the tasktray, stop the service, marking the proccess for deletion
      if(!isAppTaskStillExists()){
         
         myExoPlayer.destroy()
         
         // when its paused, it becomes a background service. StopForeground will not work on a background service. I need to manually cancel the notification.
         removeNotification()
         
         stopSelf()
         MyApplication.killProccess()
      }else{
         stopForeground(STOP_FOREGROUND_REMOVE)
      }
   }
   
   enum class PlayerCustomActions{
      FAVORITE,
      CLOSE
   }
   
   companion object{
      
      private enum class LayerState{
         Foreground,
         BackGround,
         NONE
      }
      private var currentLayerState = LayerState.NONE
      
      fun handleOnAppStart(context: Context){
         if(currentLayerState == LayerState.NONE) {
            val intent = Intent(context, MusicService::class.java).apply {
               action = MusicService.Actions.START.name
            }
            context.startForegroundService(intent)
         }else
            if(currentLayerState == LayerState.BackGround){
               MyApplication.serviceInstance?.sendToForeground()
            }
         
      }
      
      
      
      // Checks if the task is still in the tray
      fun isAppTaskStillExists(): Boolean {
         val activityManager = getSystemService(MyApplication.instance, ActivityManager::class.java) as ActivityManager
         val appTasks = activityManager.appTasks

//            appTasks.forEach {
//                Log(it.taskInfo.topActivity?.className?:"Nothing.")
//            }
         return appTasks.isNotEmpty()
      }
      
      
      const val notification_id = 106
      const val mediaSessionTag = "MusicPlayer"
      
      
      private fun selfIsActive():Boolean = MyApplication.serviceInstance!=null
   }
   
}