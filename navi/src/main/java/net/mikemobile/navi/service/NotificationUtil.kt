package net.mikemobile.navi.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.graphics.Color
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import net.mikemobile.navi.R

class NotificationUtil {

    companion object{

        fun createNotificationChannel(context:Context, channelId: String, channelName: String): String{
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (notificationManager.getNotificationChannel(channelId) == null) {

                    val channel = NotificationChannel(
                        channelId,
                        channelName,
                        NotificationManager.IMPORTANCE_HIGH
                    )
                    channel.apply {
                        lightColor = Color.BLUE
                        lockscreenVisibility = Notification.VISIBILITY_PRIVATE
                        notificationManager.createNotificationChannel(channel)
                    }
                }
            }
            return channelId
        }

        fun createNotification(context:Context, channelId: String, contentTitle: String, contentText: String, contentButton:String, paddingntent: PendingIntent): Notification{
            return NotificationCompat.Builder(context, channelId).apply {
                setSmallIcon(R.drawable.ic_notification)
                setContentTitle(contentTitle)
                setContentText(contentText)
                addAction(R.drawable.ic_launcher_foreground, contentButton,paddingntent)
            }.build()
        }

        fun createNotification(context:Context, contentTitle: String, contentText: String, contentButton:String, paddingntent: PendingIntent): Notification{
            return NotificationCompat.Builder(context, "none").apply {
                setSmallIcon(R.drawable.ic_notification)
                setContentTitle(contentTitle)
                setContentText(contentText)
                addAction(android.R.drawable.ic_notification_clear_all, contentButton, paddingntent)
            }.build()
        }

        fun createNotificationUnsupportedVersion(context:Context, contentTitle: String, contentText: String, contentButton:String, paddingntent: PendingIntent): Notification{
            return NotificationCompat.Builder(context, "none").apply {
                setSmallIcon(R.drawable.ic_notification)
                setContentTitle(contentTitle)
                setContentText(contentText)
                addAction(R.drawable.ic_launcher_background, contentButton, paddingntent)
            }.build()

        }
    }
}