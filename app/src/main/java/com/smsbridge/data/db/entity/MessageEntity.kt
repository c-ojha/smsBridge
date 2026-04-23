package com.smsbridge.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "messages",
    indices = [Index("address"), Index("threadId")]
)
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val address: String,
    val body: String,
    val timestamp: Long = System.currentTimeMillis(),
    /** 1 = inbox, 2 = sent */
    val type: Int = 1,
    val read: Boolean = false,
    val threadId: Long = 0L
)

object MessageType {
    const val INBOX = 1
    const val SENT = 2
}
