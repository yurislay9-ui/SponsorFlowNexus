/*
 * SponsorFlow Nexus v1.0 - Repository Module (Hilt)
 * Provee instancias de repositorios para inyección de dependencias
 */
package com.sponsorflow.nexus.di

import com.sponsorflow.nexus.data.repositories.ContactRepository
import com.sponsorflow.nexus.data.repositories.ConversationRepository
import com.sponsorflow.nexus.data.repositories.ProductRepository
import com.sponsorflow.nexus.data.repositories.SubscriptionRepository
import com.sponsorflow.nexus.data.dao.ContactDao
import com.sponsorflow.nexus.data.dao.ConversationDao
import com.sponsorflow.nexus.data.dao.ProductDao
import com.sponsorflow.nexus.data.dao.SubscriptionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo de inyección de dependencias para repositorios.
 * Proporciona instancias singleton de todos los repositorios del sistema.
 */
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    /**
     * Provee instancia de ContactRepository
     * @param contactDao DAO para operaciones de contactos
     * @return ContactRepository configurado
     */
    @Provides
    @Singleton
    fun provideContactRepository(contactDao: ContactDao): ContactRepository {
        return ContactRepository(contactDao)
    }

    /**
     * Provee instancia de ProductRepository
     * @param productDao DAO para operaciones de productos
     * @return ProductRepository configurado
     */
    @Provides
    @Singleton
    fun provideProductRepository(productDao: ProductDao): ProductRepository {
        return ProductRepository(productDao)
    }

    /**
     * Provee instancia de ConversationRepository
     * @param conversationDao DAO para operaciones de conversaciones
     * @return ConversationRepository configurado
     */
    @Provides
    @Singleton
    fun provideConversationRepository(conversationDao: ConversationDao): ConversationRepository {
        return ConversationRepository(conversationDao)
    }

    /**
     * Provee instancia de SubscriptionRepository
     * @param subscriptionDao DAO para operaciones de suscripciones
     * @return SubscriptionRepository configurado
     */
    @Provides
    @Singleton
    fun provideSubscriptionRepository(subscriptionDao: SubscriptionDao): SubscriptionRepository {
        return SubscriptionRepository(subscriptionDao)
    }
}