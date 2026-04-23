package com.smsbridge.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.smsbridge.data.db.dao.*
import com.smsbridge.data.db.entity.*

@Database(
    entities = [
        WebhookEntity::class,
        FilterRuleEntity::class,
        WebhookHeaderEntity::class,
        MessageEntity::class,
        DeliveryLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun webhookDao(): WebhookDao
    abstract fun filterRuleDao(): FilterRuleDao
    abstract fun webhookHeaderDao(): WebhookHeaderDao
    abstract fun messageDao(): MessageDao
    abstract fun deliveryLogDao(): DeliveryLogDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "smsbridge.db"
                ).build().also { INSTANCE = it }
            }
    }
}
