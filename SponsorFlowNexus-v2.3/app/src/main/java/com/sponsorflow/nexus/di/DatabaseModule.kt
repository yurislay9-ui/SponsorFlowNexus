/*
 * SponsorFlow Nexus v2.4 - Database Module (Hilt)
 * Proporciona instancias de Room y DAOs
 */
package com.sponsorflow.nexus.di

import android.content.Context
import androidx.room.Room
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

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): NexusDatabase {
        return Room.databaseBuilder(
            context,
            NexusDatabase::class.java,
            "nexus_database.db"
        )
            .fallbackToDestructiveMigration()
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