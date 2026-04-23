package com.smsbridge.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "filter_rules",
    foreignKeys = [ForeignKey(
        entity = WebhookEntity::class,
        parentColumns = ["id"],
        childColumns = ["webhookId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("webhookId")]
)
data class FilterRuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val webhookId: Long,
    /** SENDER or BODY */
    val field: String,
    /** CONTAINS, EXACT, REGEX, STARTS_WITH, ENDS_WITH */
    val matchType: String,
    val value: String,
    /** When true, the match result is inverted (NOT condition) */
    val negate: Boolean = false
)

object FilterField {
    const val SENDER = "SENDER"
    const val BODY = "BODY"
}

object MatchType {
    const val CONTAINS = "CONTAINS"
    const val EXACT = "EXACT"
    const val REGEX = "REGEX"
    const val STARTS_WITH = "STARTS_WITH"
    const val ENDS_WITH = "ENDS_WITH"
}
