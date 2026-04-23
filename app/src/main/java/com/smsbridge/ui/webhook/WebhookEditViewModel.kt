package com.smsbridge.ui.webhook

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.smsbridge.SMSBridgeApp
import com.smsbridge.data.db.entity.FilterRuleEntity
import com.smsbridge.data.db.entity.WebhookEntity
import com.smsbridge.data.db.entity.WebhookHeaderEntity
import com.smsbridge.data.repository.WebhookRepository
import com.smsbridge.util.SecureStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

class WebhookEditViewModel(app: Application) : AndroidViewModel(app) {
    private val secureStorage = SecureStorage(app)
    private val repo = WebhookRepository(
        (app as SMSBridgeApp).database.webhookDao(),
        app.database.filterRuleDao(),
        app.database.webhookHeaderDao(),
        secureStorage
    )

    private val webhookIdFlow = MutableStateFlow(-1L)

    val filters = webhookIdFlow.flatMapLatest { id ->
        if (id > 0) repo.observeFilters(id) else flowOf(emptyList())
    }.asLiveData()

    val headers = webhookIdFlow.flatMapLatest { id ->
        if (id > 0) repo.observeHeaders(id) else flowOf(emptyList())
    }.asLiveData()

    val saved = MutableLiveData<Long>()

    fun load(webhookId: Long) { webhookIdFlow.value = webhookId }

    suspend fun getWebhook(id: Long): WebhookEntity? = repo.getById(id)

    fun saveWebhook(webhook: WebhookEntity) = viewModelScope.launch {
        val id = if (webhook.id > 0) { repo.updateWebhook(webhook); webhook.id }
                 else repo.saveWebhook(webhook)
        webhookIdFlow.value = id
        saved.value = id
    }

    fun addFilter(rule: FilterRuleEntity) = viewModelScope.launch { repo.saveFilter(rule) }

    fun deleteFilter(rule: FilterRuleEntity) = viewModelScope.launch { repo.deleteFilter(rule) }

    fun addHeader(webhookId: Long, key: String, value: String, isSecret: Boolean) = viewModelScope.launch {
        repo.saveHeaderWithEncryption(webhookId, key, value, isSecret)
    }

    fun deleteHeader(header: WebhookHeaderEntity) = viewModelScope.launch { repo.deleteHeader(header) }

    fun decryptHeader(encryptedValue: String): String = secureStorage.decrypt(encryptedValue)
}
