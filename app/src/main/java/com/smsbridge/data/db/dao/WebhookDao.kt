package com.smsbridge.data.db.dao

import androidx.room.*
import com.smsbridge.data.db.entity.WebhookEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WebhookDao {
    @Query("SELECT * FROM webhooks ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<WebhookEntity>>

    @Query("SELECT * FROM webhooks WHERE enabled = 1")
    suspend fun getEnabledWebhooks(): List<WebhookEntity>

    @Query("SELECT * FROM webhooks WHERE id = :id")
    suspend fun getById(id: Long): WebhookEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(webhook: WebhookEntity): Long

    @Update
    suspend fun update(webhook: WebhookEntity)

    @Delete
    suspend fun delete(webhook: WebhookEntity)

    @Query("UPDATE webhooks SET enabled = :enabled WHERE id = :id")
    suspend fun setEnabled(id: Long, enabled: Boolean)
}
