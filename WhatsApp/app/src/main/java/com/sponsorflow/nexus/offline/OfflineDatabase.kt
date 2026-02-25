/*
 * SponsorFlow Nexus v1.0 - Offline Database
 */
package com.sponsorflow.nexus.offline

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [OfflineQueueEntity::class],
    version = 1,
    exportSchema = false
)
abstract class OfflineDatabase : RoomDatabase() {
    
    abstract fun offlineQueueDao(): OfflineQueueDao
    
    // CORREGIDO: Eliminado el companion object singleton
    // La creación de la base de datos ahora se maneja en DatabaseModule.kt con Hilt
}
