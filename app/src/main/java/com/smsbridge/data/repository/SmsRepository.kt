package com.smsbridge.data.repository

import com.smsbridge.data.db.dao.DeliveryLogDao
import com.smsbridge.data.db.dao.MessageDao
import com.smsbridge.data.db.entity.DeliveryLogEntity
import com.smsbridge.data.db.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

class SmsRepository(
    private val messageDao: MessageDao,
    private val deliveryLogDao: DeliveryLogDao
) {
    fun observeThreads(): Flow<List<MessageEntity>> = messageDao.observeThreads()

    fun observeConversation(address: String): Flow<List<MessageEntity>> =
        messageDao.observeConversation(address)

    fun observeUnreadCount(): Flow<Int> = messageDao.observeUnreadCount()

    fun observeDeliveryLogs(): Flow<List<DeliveryLogEntity>> = deliveryLogDao.observeRecent()

    fun observeLogsByWebhook(webhookId: Long): Flow<List<DeliveryLogEntity>> =
        deliveryLogDao.observeByWebhook(webhookId)

    suspend fun insertMessage(message: MessageEntity): Long = messageDao.insert(message)

    suspend fun markRead(address: String) = messageDao.markConversationRead(address)

    suspend fun deleteConversation(address: String) = messageDao.deleteConversation(address)

    suspend fun insertLog(log: DeliveryLogEntity): Long = deliveryLogDao.insert(log)

    suspend fun updateLog(log: DeliveryLogEntity) = deliveryLogDao.update(log)

    suspend fun getPendingLogs(): List<DeliveryLogEntity> = deliveryLogDao.getPending()

    suspend fun pruneOldLogs(keepDays: Int = 30) {
        val cutoff = System.currentTimeMillis() - (keepDays * 24 * 60 * 60 * 1000L)
        deliveryLogDao.pruneOlderThan(cutoff)
    }
}
