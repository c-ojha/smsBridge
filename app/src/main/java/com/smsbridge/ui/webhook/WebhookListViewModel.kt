package com.smsbridge.ui.webhook

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.smsbridge.SMSBridgeApp
import com.smsbridge.data.db.entity.WebhookEntity
import com.smsbridge.data.repository.WebhookRepository
import com.smsbridge.util.SecureStorage
import kotlinx.coroutines.launch

class WebhookListViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = WebhookRepository(
        (app as SMSBridgeApp).database.webhookDao(),
        app.database.filterRuleDao(),
        app.database.webhookHeaderDao(),
        SecureStorage(app)
    )

    val webhooks = repo.observeAll().asLiveData()

    fun toggleEnabled(webhook: WebhookEntity, enabled: Boolean) = viewModelScope.launch {
        repo.setEnabled(webhook.id, enabled)
    }

    fun delete(webhook: WebhookEntity) = viewModelScope.launch {
        repo.deleteWebhook(webhook)
    }
}
