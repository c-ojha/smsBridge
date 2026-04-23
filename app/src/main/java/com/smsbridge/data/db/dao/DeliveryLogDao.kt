package com.smsbridge.data.db.dao

import androidx.room.*
import com.smsbridge.data.db.entity.DeliveryLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DeliveryLogDao {
    @Query("SELECT * FROM delivery_logs ORDER BY createdAt DESC LIMIT 200")
    fun observeRecent(): Flow<List<DeliveryLogEntity>>

    @Query("SELECT * FROM delivery_logs WHERE webhookId = :webhookId ORDER BY createdAt DESC LIMIT 100")
    fun observeByWebhook(webhookId: Long): Flow<List<DeliveryLogEntity>>

    @Query("SELECT * FROM delivery_logs WHERE status = 'RETRYING' OR status = 'PENDING'")
    suspend fun getPending(): List<DeliveryLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: DeliveryLogEntity): Long

    @Update
    suspend fun update(log: DeliveryLogEntity)

    @Query("DELETE FROM delivery_logs WHERE createdAt < :olderThan")
    suspend fun pruneOlderThan(olderThan: Long)
}
