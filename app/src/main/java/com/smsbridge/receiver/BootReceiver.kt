package com.smsbridge.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.smsbridge.service.SmsForegroundService

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            "android.intent.action.LOCKED_BOOT_COMPLETED",
            Intent.ACTION_MY_PACKAGE_REPLACED -> {
                SmsForegroundService.startPersistent(context)
            }
        }
    }
}
