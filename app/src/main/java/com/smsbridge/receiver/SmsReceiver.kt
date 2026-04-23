package com.smsbridge.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.smsbridge.service.SmsForegroundService

/**
 * Receives SMS_DELIVER — only fired when SMSBridge is the default SMS app.
 * Android guarantees this BroadcastReceiver fires even if the app process is killed.
 * Network work must be delegated to a Service (BroadcastReceiver has a short execution window).
 */
class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_DELIVER_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (messages.isNullOrEmpty()) return

        val sender = messages[0].originatingAddress ?: return
        val body = messages.joinToString("") { it.messageBody }

        SmsForegroundService.enqueueSms(context, sender, body)
    }
}
