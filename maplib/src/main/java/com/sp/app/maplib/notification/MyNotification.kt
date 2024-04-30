package com.sp.app.maplib.notification

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.sp.app.maplib.service.AppReceiver

class MyNotification{

    fun createPendingIntent(context:Context) : PendingIntent {
        val intent = Intent(context, AppReceiver::class.java)
        intent.action = INTENT_CANCEL

        return PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }


    fun createServiceNotification(context: Context): Notification{
        var text = ""

        var title = "ロボSync稼働中"
        //val button = context.resources.getString(R.string.notification_navi_button_cancel)
        val button = "アプリ終了"

        val notification: Notification

        if (Build.VERSION.SDK_INT >= 26) {
            val channelID = NotificationUtil.createNotificationChannel(
                context,
                notificationChannelID,
                notificationChannelNAME
            )
            notification = NotificationUtil.createNotification(context,channelID, title, text, button, createPendingIntent(context))
        } else if (Build.VERSION.SDK_INT >= 23) {
            notification = NotificationUtil.createNotification(context,title, text, button, createPendingIntent(context))
        } else {
            notification = NotificationUtil.createNotificationUnsupportedVersion(context,title, text, button, createPendingIntent(context))
        }
        return notification
    }

    companion object {
        const val TAG = "MyNotification"

        const val INTENT_CANCEL = "robo_intent_cancel"

        private val notificationChannelID = "navi_notification_channel"
        private val notificationChannelNAME = "navi_channel"



    }



}