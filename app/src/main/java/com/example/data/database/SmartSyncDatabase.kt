package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.ConnectionEntity
import com.example.data.model.EventEntity
import com.example.data.model.NotificationEntity
import com.example.data.model.PreferencesEntity
import com.example.data.model.TaskEntity

@Database(
    entities = [
        EventEntity::class,
        TaskEntity::class,
        ConnectionEntity::class,
        NotificationEntity::class,
        PreferencesEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class SmartSyncDatabase : RoomDatabase() {

    abstract fun smartSyncDao(): SmartSyncDao

    companion object {
        @Volatile
        private var INSTANCE: SmartSyncDatabase? = null

        fun getDatabase(context: Context): SmartSyncDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SmartSyncDatabase::class.java,
                    "smartsync_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
