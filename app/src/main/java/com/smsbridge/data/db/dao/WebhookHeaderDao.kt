package com.smsbridge.data.db.dao

import androidx.room.*
import com.smsbridge.data.db.entity.WebhookHeaderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WebhookHeaderDao {
    @Query("SELECT * FROM webhook_headers WHERE webhookId = :webhookId")
    fun observeByWebhook(webhookId: Long): Flow<List<WebhookHeaderEntity>>

    @Query("SELECT * FROM webhook_headers WHERE webhookId = :webhookId")
    suspend fun getByWebhook(webhookId: Long): List<WebhookHeaderEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(header: WebhookHeaderEntity): Long

    @Update
    suspend fun update(header: WebhookHeaderEntity)

    @Delete
    suspend fun delete(header: WebhookHeaderEntity)

    @Query("DELETE FROM webhook_headers WHERE webhookId = :webhookId")
    suspend fun deleteByWebhook(webhookId: Long)
}
