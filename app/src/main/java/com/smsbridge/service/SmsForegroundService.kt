package com.smsbridge.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import com.smsbridge.SMSBridgeApp
import com.smsbridge.data.db.entity.MessageEntity
import com.smsbridge.data.db.entity.MessageType
import com.smsbridge.data.repository.SmsRepository
import com.smsbridge.util.NotificationHelper
import kotlinx.coroutines.*

/**
 * Foreground service that:
 *  - Keeps the process alive (foreground priority, same as visible UI)
 *  - Processes incoming SMS: saves to local DB, dispatches to webhooks
 *  - Uses START_REDELIVER_INTENT so OS re-delivers SMS data if killed mid-flight
 */
class SmsForegroundService : Service() {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var dispatcher: WebhookDispatcher
    private lateinit var smsRepo: SmsRepository

    override fun onCreate() {
        super.onCreate()
        val app = applicationContext as SMSBridgeApp
        dispatcher = WebhookDispatcher(this)
        smsRepo = SmsRepository(app.database.messageDao(), app.database.deliveryLogDao())
        startForeground(
            NotificationHelper.NOTIF_FOREGROUND_ID,
            NotificationHelper.buildForegroundNotification(this)
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val sender = intent?.getStringExtra(EXTRA_SENDER)
        val body = intent?.getStringExtra(EXTRA_BODY)

        if (sender != null && body != null) {
            scope.launch {
                // Save to local inbox first
                smsRepo.insertMessage(
                    MessageEntity(
                        address = sender,
                        body = body,
                        type = MessageType.INBOX,
                        read = false
                    )
                )
                // Show incoming notification
                NotificationHelper.showIncomingMessage(
                    this@SmsForegroundService, sender, body,
                    NotificationHelper.NOTIF_INCOMING_BASE_ID + sender.hashCode()
                )
                // Forward to matching webhooks
                dispatcher.dispatch(sender, body)
            }
        }

        return START_REDELIVER_INTENT
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    companion object {
        private const val EXTRA_SENDER = "sender"
        private const val EXTRA_BODY = "body"
        private const val ACTION_ENQUEUE = "com.smsbridge.action.ENQUEUE_SMS"
        private const val ACTION_START = "com.smsbridge.action.START_PERSISTENT"

        fun enqueueSms(context: Context, sender: String, body: String) {
            val intent = Intent(context, SmsForegroundService::class.java).apply {
                action = ACTION_ENQUEUE
                putExtra(EXTRA_SENDER, sender)
                putExtra(EXTRA_BODY, body)
            }
            context.startForegroundService(intent)
        }

        fun startPersistent(context: Context) {
            val intent = Intent(context, SmsForegroundService::class.java).apply {
                action = ACTION_START
            }
            context.startForegroundService(intent)
        }
    }
}
