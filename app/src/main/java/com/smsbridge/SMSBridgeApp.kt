package com.smsbridge

import android.app.Application
import com.smsbridge.data.db.AppDatabase
import com.smsbridge.util.NotificationHelper

class SMSBridgeApp : Application() {

    val database by lazy { AppDatabase.getInstance(this) }

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createChannels(this)
    }
}
