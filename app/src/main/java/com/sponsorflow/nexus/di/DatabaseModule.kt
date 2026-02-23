/*
 * SponsorFlow Nexus v2.4 - Database Module (Hilt)
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
            // Ejemplo: agregar nueva tabla o columna
            // database.execSQL("ALTER TABLE products ADD COLUMN new_column TEXT")
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