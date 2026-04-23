package com.smsbridge.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "webhook_headers",
    foreignKeys = [ForeignKey(
        entity = WebhookEntity::class,
        parentColumns = ["id"],
        childColumns = ["webhookId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("webhookId")]
)
data class WebhookHeaderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val webhookId: Long,
    val key: String,
    /** Encrypted value stored via SecureStorage; raw value never persisted in DB */
    val encryptedValue: String,
    val isSecret: Boolean = false
)
