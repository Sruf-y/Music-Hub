package com.musichub.Services

import ExoPlayer.ExoPlayerViewModel
import ExoPlayer.MyPlayerListener
import ExoPlayer.myExoPlayer
import android.app.ActivityManager
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.BitmapFactory
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat.getSystemService
import com.musichub.MyApplication
import com.musichub.R
import androidx.media.app.NotificationCompat.MediaStyle
import com.musichub.Activities.MainActivityView
import com.musichub.Classes.NotificationPoster.Companion.openMediaStoreUriPendingIntent
import com.musichub.Functions
import com.musichub.Log
import com.musichub.Logs
import com.musichub.MyApplication.Companion.crashLogger
import com.musichub.tryTo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MusicService: Service() {
   
   val scope = CoroutineScope(Dispatchers.Main)
   
   private val binder = MusicBinder()
   
   inner class MusicBinder : Binder() {
      fun getService(): MusicService = this@MusicService
   }
   
   override fun onBind(intent: Intent): IBinder? {
      return binder
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
         
      }
      
      override fun onStart() {
         
         if(currentLayerState != LayerState.Foreground){
            sendToForeground()
         }
         
         UpdateNotification(CreateNotification())
      }
      
      override fun onPause() {
         
         // if app is NOT active and the current state is Foreground
         if(!isAppTaskStillExists() && currentLayerState == LayerState.Foreground){
            sendToBackground()
         }
         
         UpdateNotification(CreateNotification())
      }
      
      override fun onPreSongSet() {
      
      }
      
      override fun onSongSet() {
      
      }
      
      override fun onSeek() {
         
         UpdateNotification(CreateNotification())
      }
      
   }
   /**
    * **Note:**
    * A [GlobalScope] is launched in onCreate.
    * */
   override fun onCreate() {
      super.onCreate()
      
      
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
      
      
      scope.launch {
         myExoPlayer.isPlaying.collect {
            if(it && currentLayerState != LayerState.Foreground){
               sendToForeground()
            }
            else
               UpdateNotification(CreateNotification())
         }
      }
      
      Log.i(Logs.SERVICE.name,"Called onCreate.")
   }
   
   override fun onTaskRemoved(rootIntent: Intent?) {
      super.onTaskRemoved(rootIntent)
      
      Log("Service noticed: [Task removed]",Logs.SERVICE.name)
      
      if(!myExoPlayer.isPlaying.value) {
         sendToBackground()
      }
      
   }
   
   override fun onDestroy() {
      super.onDestroy()
      removeNotification()
      
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
      /** It seems after testing that this now passes without throwing an exception, but keeping the exception
       * handling is preffered as one can still be thrown at a random time.
       *
       * TODO: Set throwAnyway to false. This is here as true for testing because otherwise it will silently fail. And even if it does, now it shouldn't have any issue anymore
       *    due to app UI restarting the service + the proccess having a bind to the service which on fail allows for restart.
       * */
      tryTo(
         throwAnyway = true,
         onError = {
            val uri = MyApplication.crashLogger.writeCrashToFile(
               throwable = it,
               context = this,
               headerDetails = "Thrown from StartForegroundNotification. Reason: probably tried to start foreground at a bad time. RETARDED error since i track this state manually."
            )
            
            try {
               MyApplication.allertPoster.postNotification(
                  title = "Foreground error again",
                  message = "Stacktrace was written to ${crashLogger.folder.path}",
                  context = this,
                  pendingIntent = openMediaStoreUriPendingIntent(this,uri)
               )
            }catch (ex: SecurityException){
               ex.printStackTrace()
            }
            
         }
      ) {
         ServiceCompat.startForeground(this@MusicService,
            notification_id,
            notification,
            ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
         )
      }
      
   }
   
   fun UpdateNotification(notification:Notification){
      val notifManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
      notifManager.notify(notification_id,notification)
   }
   
   fun sendToForeground(){
      Log("sendToForeground")
      StartForegroundNotification(CreateNotification())
      currentLayerState = LayerState.Foreground
   }
   
   fun sendToBackground(){
      Log("sendToBackground")
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
         
         //myExoPlayer.destroy()
         myExoPlayer.onSaveLogic()
         
         // when its paused, it becomes a background service. StopForeground will not work on a background service. I need to manually cancel the notification.
         removeNotification()
         currentLayerState = LayerState.NONE
         stopSelf()
         MyApplication.killProccess()
      }else{ // When trying to close the notification itself
         Log("Just notification closed")
         
         removeNotification()
         stopForeground(STOP_FOREGROUND_REMOVE)
         currentLayerState = LayerState.NONE
      }
   }
   
   enum class PlayerCustomActions{
      FAVORITE,
      CLOSE
   }
   
   private var currentLayerState = LayerState.NONE
   
   companion object{
      
      private enum class LayerState{
         Foreground,
         BackGround,
         NONE
      }
      
      
      fun handleOnAppStart(context: Context){
         
         val intent = Intent(context, MusicService::class.java).apply {
            action = MusicService.Actions.START.name
         }
         context.startForegroundService(intent)
        
      }
      
      
      
      // Checks if the task is still in the tray
      fun isAppTaskStillExists(): Boolean {
         val activityManager = getSystemService(MyApplication.instance, ActivityManager::class.java) as ActivityManager
         val appTasks = activityManager.appTasks
         
         
         return appTasks.any { it.taskInfo.isRunning }
      }
      
      
      const val notification_id = 106
      const val mediaSessionTag = "MusicPlayer"
      
   }
   
}