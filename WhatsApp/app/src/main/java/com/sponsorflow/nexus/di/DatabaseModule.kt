/*
 * SponsorFlow Nexus v1.0 - Database Module (Hilt)
 * CORREGIDO: Migraciones en lugar de fallbackToDestructiveMigration
 */
package com.sponsorflow.nexus.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import com.sponsorflow.nexus.data.dao.*
import com.sponsorflow.nexus.data.database.NexusDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    // Migración de ejemplo para versión 1 -> 2
    // Agregar más migraciones según sea necesario
    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
            // CORREGIDO: Implementación real de migración
            // Agregar columna de fecha de creación a la tabla de productos
            database.execSQL("ALTER TABLE products ADD COLUMN created_at INTEGER DEFAULT 0")
            
            // Agregar columna de estado a la tabla de contactos
            database.execSQL("ALTER TABLE contacts ADD COLUMN status TEXT DEFAULT 'ACTIVE'")
            
            // Agregar índice para búsquedas rápidas
            database.execSQL("CREATE INDEX IF NOT EXISTS index_products_category ON products(category)")
        }
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): NexusDatabase {
        return Room.databaseBuilder(
            context,
            NexusDatabase::class.java,
            "nexus_database.db"
        )
            // CORREGIDO: Usar addMigrations en lugar de fallbackToDestructiveMigration
            .addMigrations(MIGRATION_1_2)
            // Solo usar como último recurso si no hay migraciones
            .fallbackToDestructiveMigrationOnDowngrade()
            .build()
    }

    @Provides
    fun provideContactDao(database: NexusDatabase): ContactDao = database.contactDao()

    @Provides
    fun provideTemplateDao(database: NexusDatabase): TemplateDao = database.templateDao()

    @Provides
    fun provideConversationDao(database: NexusDatabase): ConversationDao = database.conversationDao()

    @Provides
    fun provideProductDao(database: NexusDatabase): ProductDao = database.productDao()

    @Provides
    fun provideMetricDao(database: NexusDatabase): MetricDao = database.metricDao()

    @Provides
    fun provideSubscriptionDao(database: NexusDatabase): SubscriptionDao = database.subscriptionDao()
}