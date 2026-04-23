package com.smsbridge.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "delivery_logs",
    foreignKeys = [ForeignKey(
        entity = WebhookEntity::class,
        parentColumns = ["id"],
        childColumns = ["webhookId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("webhookId"), Index("createdAt")]
)
data class DeliveryLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val webhookId: Long,
    val webhookName: String,
    val sender: String,
    val bodyPreview: String,
    val status: String = DeliveryStatus.PENDING,
    val attempts: Int = 0,
    val responseCode: Int = 0,
    val errorMessage: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val lastAttemptAt: Long = 0L
)

object DeliveryStatus {
    const val PENDING = "PENDING"
    const val SUCCESS = "SUCCESS"
    const val RETRYING = "RETRYING"
    const val FAILED = "FAILED"
}
