package com.smsbridge.data.db.dao

import androidx.room.*
import com.smsbridge.data.db.entity.FilterRuleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FilterRuleDao {
    @Query("SELECT * FROM filter_rules WHERE webhookId = :webhookId")
    fun observeByWebhook(webhookId: Long): Flow<List<FilterRuleEntity>>

    @Query("SELECT * FROM filter_rules WHERE webhookId = :webhookId")
    suspend fun getByWebhook(webhookId: Long): List<FilterRuleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(rule: FilterRuleEntity): Long

    @Update
    suspend fun update(rule: FilterRuleEntity)

    @Delete
    suspend fun delete(rule: FilterRuleEntity)

    @Query("DELETE FROM filter_rules WHERE webhookId = :webhookId")
    suspend fun deleteByWebhook(webhookId: Long)
}
