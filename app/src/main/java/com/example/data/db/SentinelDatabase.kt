package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [AuditLogEntity::class], version = 1, exportSchema = false)
abstract class SentinelDatabase : RoomDatabase() {
    abstract fun auditLogDao(): AuditLogDao

    companion object {
        @Volatile
        private var INSTANCE: SentinelDatabase? = null

        fun getInstance(context: Context): SentinelDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SentinelDatabase::class.java,
                    "sentinel_audit.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
