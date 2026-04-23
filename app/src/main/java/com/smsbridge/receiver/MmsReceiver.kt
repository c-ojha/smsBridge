package com.smsbridge.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Required stub for default SMS app registration.
 * MMS forwarding can be added in a future version.
 */
class MmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // MMS handling stub — required by Android for default SMS app role
    }
}
