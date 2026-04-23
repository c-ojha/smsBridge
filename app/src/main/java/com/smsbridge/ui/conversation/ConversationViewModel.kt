package com.smsbridge.ui.conversation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import com.smsbridge.SMSBridgeApp
import com.smsbridge.data.repository.SmsRepository
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ConversationViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = SmsRepository(
        (app as SMSBridgeApp).database.messageDao(),
        app.database.deliveryLogDao()
    )
    private val _address = MutableLiveData<String>()

    val messages = _address.switchMap { addr ->
        repo.observeConversation(addr).asLiveData()
    }

    fun setAddress(address: String) {
        _address.value = address
        viewModelScope.launch(Dispatchers.IO) { repo.markRead(address) }
    }
}
