/*
 * SponsorFlow Nexus v2.4 - Offline Database
 */
package com.sponsorflow.nexus.offline

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [OfflineQueueEntity::class],
    version = 1,
    exportSchema = false
)
abstract class OfflineDatabase : RoomDatabase() {
    
    abstract fun offlineQueueDao(): OfflineQueueDao
    
    companion object {
        private const val DB_NAME = "nexus_offline_queue.db"
        
        @Volatile
        private var INSTANCE: OfflineDatabase? = null
        
        fun getInstance(context: Context): OfflineDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }
        }
        
        private fun buildDatabase(context: Context): OfflineDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                OfflineDatabase::class.java,
                DB_NAME
            )
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}