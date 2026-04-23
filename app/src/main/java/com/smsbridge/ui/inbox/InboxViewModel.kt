package com.smsbridge.ui.inbox

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import com.smsbridge.SMSBridgeApp
import com.smsbridge.data.repository.SmsRepository

class InboxViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = SmsRepository(
        (app as SMSBridgeApp).database.messageDao(),
        app.database.deliveryLogDao()
    )
    val threads = repo.observeThreads().asLiveData()
    val unreadCount = repo.observeUnreadCount().asLiveData()
}
