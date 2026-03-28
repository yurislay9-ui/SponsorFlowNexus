/*
 * SponsorFlow Nexus v1.0 - Offline Database
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
        @Volatile
        private var INSTANCE: OfflineDatabase? = null

        fun getInstance(context: Context): OfflineDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    OfflineDatabase::class.java,
                    "offline_queue_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
