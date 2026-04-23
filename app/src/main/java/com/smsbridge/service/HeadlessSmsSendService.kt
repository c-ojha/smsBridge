package com.smsbridge.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.telephony.SmsMessage
import com.smsbridge.util.SmsUtils

/**
 * Required service for default SMS app role.
 * Handles RESPOND_VIA_MESSAGE intent (e.g., quick reply from a phone call screen).
 */
class HeadlessSmsSendService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val uri = intent?.data
        val message = intent?.getStringExtra(Intent.EXTRA_TEXT)

        if (uri != null && !message.isNullOrBlank()) {
            val address = uri.schemeSpecificPart?.trimStart('/')
            if (!address.isNullOrBlank()) {
                try {
                    SmsUtils.sendSms(this, address, message)
                } catch (e: Exception) {
                    // Silently fail — best effort for headless send
                }
            }
        }

        stopSelf(startId)
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
