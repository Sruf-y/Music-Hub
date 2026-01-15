package com.aacomposetest

import android.Manifest
import android.R
import android.app.PendingIntent
import android.content.Context
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.annotation.RequiresPermission
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationChannelGroupCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.media3.common.util.NotificationUtil
import com.aacomposetest.ui.theme.MyColor
import kotlin.random.Random
import kotlin.random.nextInt

class NotificationPoster(
    context: Context,
    private val channelId: String,
    private val channelName: String,
    private val importance: Int = NotificationManagerCompat.IMPORTANCE_DEFAULT,
    private val pendingIntent: PendingIntent? = null
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
        return Random.nextInt(min,max)
    }

    private val summaryId = generateId()
    /**
     * @param id the notification id. Throws [IllegalArgumentException] if it is equal to 80085. If not provided, it will be randomly generated
     * */
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun postNotification(
        id: Int? = null,
        title: String,
        message: String,
        context:Context,
        @DrawableRes icon: Int = R.drawable.ic_dialog_info,
        color: Color = Color.Blue,
        showAsGroup: Boolean = true
    ) {
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
    fun postGroupSummary(
        summaryId: Int,
        summaryTitle:String,
        summaryText: String,
        context: Context,
        @DrawableRes icon: Int = R.drawable.ic_dialog_info
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
}