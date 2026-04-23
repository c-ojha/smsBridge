package com.smsbridge.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.smsbridge.R
import com.smsbridge.ui.MainActivity

object NotificationHelper {

    const val CHANNEL_FOREGROUND = "smsbridge_foreground"
    const val CHANNEL_INCOMING = "smsbridge_incoming"
    const val CHANNEL_FORWARD_STATUS = "smsbridge_forward_status"

    const val NOTIF_FOREGROUND_ID = 1001
    const val NOTIF_INCOMING_BASE_ID = 2000
    const val NOTIF_FAILURE_BASE_ID = 3000

    fun createChannels(context: Context) {
        val nm = context.getSystemService(NotificationManager::class.java)
        nm.createNotificationChannels(listOf(
            NotificationChannel(CHANNEL_FOREGROUND, "Service Running", NotificationManager.IMPORTANCE_LOW).apply {
                description = "Keeps SMSBridge active to receive and forward messages"
                setShowBadge(false)
            },
            NotificationChannel(CHANNEL_INCOMING, "New Messages", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Notifications for incoming SMS"
            },
            NotificationChannel(CHANNEL_FORWARD_STATUS, "Forward Status", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Webhook delivery success and failure alerts"
            }
        ))
    }

    fun buildForegroundNotification(context: Context): Notification {
        val intent = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(context, CHANNEL_FOREGROUND)
            .setContentTitle("SMSBridge Active")
            .setContentText("Monitoring messages and forwarding to webhooks")
            .setSmallIcon(R.drawable.ic_sms_notify)
            .setOngoing(true)
            .setContentIntent(intent)
            .build()
    }

    fun showIncomingMessage(context: Context, sender: String, body: String, notifId: Int) {
        // Use a unique requestCode per sender to prevent PendingIntent reuse across different senders
        val requestCode = (NOTIF_INCOMING_BASE_ID.toLong() + Math.abs(sender.hashCode().toLong())).toInt()
        val intent = PendingIntent.getActivity(
            context, requestCode,
            Intent(context, MainActivity::class.java).apply {
                putExtra("address", sender)
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notif = NotificationCompat.Builder(context, CHANNEL_INCOMING)
            .setContentTitle(sender)
            .setContentText(body)
            .setSmallIcon(R.drawable.ic_sms_notify)
            .setAutoCancel(true)
            .setContentIntent(intent)
            .build()
        context.getSystemService(NotificationManager::class.java).notify(notifId, notif)
    }

    fun showForwardFailure(context: Context, webhookName: String, sender: String, notifId: Int) {
        val notif = NotificationCompat.Builder(context, CHANNEL_FORWARD_STATUS)
            .setContentTitle("Forward failed: $webhookName")
            .setContentText("Could not forward SMS from $sender after all retries")
            .setSmallIcon(R.drawable.ic_sms_notify)
            .setAutoCancel(true)
            .build()
        context.getSystemService(NotificationManager::class.java).notify(notifId, notif)
    }
}
