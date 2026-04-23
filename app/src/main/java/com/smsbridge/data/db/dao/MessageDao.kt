package com.smsbridge.data.db.dao

import androidx.room.*
import com.smsbridge.data.db.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Query("""
        SELECT * FROM messages
        WHERE id IN (
            SELECT MAX(id) FROM messages GROUP BY address
        )
        ORDER BY timestamp DESC
    """)
    fun observeThreads(): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE address = :address ORDER BY timestamp ASC")
    fun observeConversation(address: String): Flow<List<MessageEntity>>

    @Query("SELECT COUNT(*) FROM messages WHERE read = 0 AND type = 1")
    fun observeUnreadCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(message: MessageEntity): Long

    @Query("UPDATE messages SET read = 1 WHERE address = :address")
    suspend fun markConversationRead(address: String)

    @Query("DELETE FROM messages WHERE address = :address")
    suspend fun deleteConversation(address: String)
}
