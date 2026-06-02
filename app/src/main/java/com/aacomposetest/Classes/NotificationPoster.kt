package com.aacomposetest.Classes

import android.Manifest
import android.R
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresPermission
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationChannelGroupCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import com.aacomposetest.Log
import com.aacomposetest.MyApplication
import com.aacomposetest.hasPermission
import kotlin.random.Random

class NotificationPoster(
   context: Context,
   private val channelId: String,
   private val channelName: String,
   private val importance: Int = NotificationManagerCompat.IMPORTANCE_DEFAULT,
   private val pendingIntent: PendingIntent? = null,
) {
    private val manager = NotificationManagerCompat.from(context)

    private var group: NotificationChannelGroupCompat? = null
    private var channel: NotificationChannelCompat? = null

    init {
        createChannel()
    }

    private fun createChannel() {
        val channelBuilder = NotificationChannelCompat.Builder(channelId, importance)
            .setName(channelName)

        // If group is already set, apply it
        group?.let { channelBuilder.setGroup(it.id) }

        channel = channelBuilder.build()
        manager.createNotificationChannel(channel!!)
    }

    fun setGroup(name: String, groupId: String) {
        // Create the group
        group = NotificationChannelGroupCompat.Builder(groupId)
            .setName(name)
            .build()
        manager.createNotificationChannelGroup(group!!)

        // Recreate channel with the group
        createChannel()
    }
    fun generateId(min:Int = 0,max:Int = 10000):Int{
        return Random.Default.nextInt(min,max)
    }

    private val summaryId = generateId()
    /**
     * @param id the notification id. Throws [IllegalArgumentException] if it is equal to 80085. If not provided, it will be randomly generated
     * */
    @SuppressLint("MissingPermission")
    fun postNotification(
       id: Int? = null,
       title: String,
       message: String,
       pendingIntent: PendingIntent? = null,
       context: Context,
       @DrawableRes icon: Int = R.drawable.ic_dialog_info,
       color: Color = Color.Companion.Blue,
       showAsGroup: Boolean = true,
    ) {
        // Early return in case i don't have notifications permissions
        if(!context.hasPermission(MyApplication.Permissions.POST_NOTIFICATIONS)){
            return
        }
        
        // Ensure channel exists
        if (channel == null) {
            createChannel()
        }

        val realId = if(id==null){
            var tempId = 0
            while (manager.activeNotifications.map { it.id }.contains(tempId) || tempId == summaryId){
                tempId = generateId()
            }
            tempId
        }else{
            id
        }

        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setContentIntent(pendingIntent)
            .setSmallIcon(icon)
            .setColor(color.toArgb())
            .setAutoCancel(true)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))

        // Set group if available
        if (showAsGroup && group != null) {
            notificationBuilder
                .setGroup(group!!.id)
                .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_SUMMARY)
        }
        
        val notification = notificationBuilder.build()
        manager.notify(realId, notification)
        
        postGroupSummary(
            summaryId = summaryId,
            summaryTitle = "Alerts Summary",
            summaryText = "${manager.activeNotifications.size} allerts",
            context = context
        )

    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun postGroupSummary(
       summaryId: Int,
       summaryTitle: String,
       summaryText: String,
       context: Context,
       @DrawableRes icon: Int = R.drawable.ic_dialog_info,
    ) {
        if (group == null) return

        val summaryNotification = NotificationCompat.Builder(context, channelId)
            .setContentTitle(summaryTitle)
            .setContentText(summaryText)
            .setContentIntent(pendingIntent)
            .setSmallIcon(icon)
            .setGroup(group!!.id)
            .setGroupSummary(true)
            .setAutoCancel(false)
            .build()

        manager.notify(summaryId, summaryNotification)
    }

    fun clearNotification(id: Int) {
        manager.cancel(id)
    }

    fun clearAllNotifications() {
        manager.cancelAll()
    }
    
    
    companion object{
        
        fun openMediaStoreUriPendingIntent(context: Context, mediaStoreUri: Uri?): PendingIntent? {
            // Convert string to Uri
            if(mediaStoreUri == null){
                return null
            }
            
            val intent = Intent(Intent.ACTION_VIEW).apply {
                // Use the MediaStore URI directly
                
                setDataAndType(mediaStoreUri,getMimeTypeFromUri(context, mediaStoreUri))
                
                
                addFlags(
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            
            return PendingIntent.getActivity(
                context,
                mediaStoreUri.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
        
        private fun getMimeTypeFromUri(context: Context, uri: Uri): String? {
            return context.contentResolver.getType(uri)
        }
        
        
        
    }
}