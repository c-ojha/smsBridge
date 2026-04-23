package com.smsbridge.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "webhooks")
data class WebhookEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val url: String,
    val enabled: Boolean = true,
    /** AND = SMS must match ALL filter rules; OR = any one match triggers forward */
    val filterOperator: String = "AND",
    val retryCount: Int = 3,
    val retryDelayMs: Long = 5000L,
    val createdAt: Long = System.currentTimeMillis()
)
