package com.smsbridge.data.repository

import com.smsbridge.data.db.dao.*
import com.smsbridge.data.db.entity.*
import com.smsbridge.util.SecureStorage
import kotlinx.coroutines.flow.Flow

class WebhookRepository(
    private val webhookDao: WebhookDao,
    private val filterRuleDao: FilterRuleDao,
    private val headerDao: WebhookHeaderDao,
    private val secureStorage: SecureStorage
) {
    fun observeAll(): Flow<List<WebhookEntity>> = webhookDao.observeAll()

    suspend fun getById(id: Long): WebhookEntity? = webhookDao.getById(id)

    suspend fun getEnabledWebhooks(): List<WebhookEntity> = webhookDao.getEnabledWebhooks()

    fun observeFilters(webhookId: Long): Flow<List<FilterRuleEntity>> =
        filterRuleDao.observeByWebhook(webhookId)

    suspend fun getFilters(webhookId: Long): List<FilterRuleEntity> =
        filterRuleDao.getByWebhook(webhookId)

    fun observeHeaders(webhookId: Long): Flow<List<WebhookHeaderEntity>> =
        headerDao.observeByWebhook(webhookId)

    suspend fun getHeaders(webhookId: Long): List<WebhookHeaderEntity> =
        headerDao.getByWebhook(webhookId)

    /** Returns decrypted header value for use in HTTP requests */
    fun decryptHeaderValue(encryptedValue: String): String =
        secureStorage.decrypt(encryptedValue)

    suspend fun saveWebhook(webhook: WebhookEntity): Long =
        webhookDao.insert(webhook)

    suspend fun updateWebhook(webhook: WebhookEntity) =
        webhookDao.update(webhook)

    suspend fun deleteWebhook(webhook: WebhookEntity) =
        webhookDao.delete(webhook)

    suspend fun setEnabled(id: Long, enabled: Boolean) =
        webhookDao.setEnabled(id, enabled)

    suspend fun saveFilter(rule: FilterRuleEntity): Long =
        filterRuleDao.insert(rule)

    suspend fun deleteFilter(rule: FilterRuleEntity) =
        filterRuleDao.delete(rule)

    suspend fun deleteAllFilters(webhookId: Long) =
        filterRuleDao.deleteByWebhook(webhookId)

    suspend fun saveHeader(header: WebhookHeaderEntity): Long =
        headerDao.insert(header)

    suspend fun deleteHeader(header: WebhookHeaderEntity) =
        headerDao.delete(header)

    suspend fun deleteAllHeaders(webhookId: Long) =
        headerDao.deleteByWebhook(webhookId)

    /** Encrypts and saves a header. Returns the saved entity with encrypted value. */
    suspend fun saveHeaderWithEncryption(
        webhookId: Long,
        key: String,
        rawValue: String,
        isSecret: Boolean
    ): Long {
        val encrypted = secureStorage.encrypt(rawValue)
        return headerDao.insert(WebhookHeaderEntity(webhookId = webhookId, key = key, encryptedValue = encrypted, isSecret = isSecret))
    }
}
