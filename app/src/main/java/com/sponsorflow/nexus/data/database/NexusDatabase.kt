/*
 * SponsorFlow Nexus v2.4 - Room Database
 * Skill: Seguridad - Base de datos con Room
 */
package com.sponsorflow.nexus.data.database

import androidx.room.*
import com.sponsorflow.nexus.data.entity.*
import com.sponsorflow.nexus.data.dao.*

@Database(
    entities = [
        ContactEntity::class,
        TemplateEntity::class,
        ConversationEntity::class,
        ProductEntity::class,
        MetricEntity::class,
        SubscriptionEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class NexusDatabase : RoomDatabase() {
    abstract fun contactDao(): ContactDao
    abstract fun templateDao(): TemplateDao
    abstract fun conversationDao(): ConversationDao
    abstract fun productDao(): ProductDao
    abstract fun metricDao(): MetricDao
    abstract fun subscriptionDao(): SubscriptionDao
}