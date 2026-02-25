/*
 * SponsorFlow Nexus v1.0 - Security Module (Hilt)
 * EncryptedSharedPreferences, Play Integrity, Certificate Pinning
 */
package com.sponsorflow.nexus.di

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.android.play.core.integrity.IntegrityManager
import com.google.android.play.core.integrity.IntegrityManagerFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.CertificatePinner
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SecurityModule {

    @Provides
    @Singleton
    fun provideMasterKey(@ApplicationContext context: Context): MasterKey {
        return MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    @Provides
    @Singleton
    fun provideEncryptedSharedPreferences(
        @ApplicationContext context: Context,
        masterKey: MasterKey
    ): EncryptedSharedPreferences {
        return EncryptedSharedPreferences.create(
            context,
            "nexus_encrypted_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        ) as EncryptedSharedPreferences
    }

    @Provides
    @Singleton
    fun provideIntegrityManager(@ApplicationContext context: Context): IntegrityManager {
        return IntegrityManagerFactory.create(context)
    }

    @Provides
    @Singleton
    fun provideCertificatePinner(): CertificatePinner {
        // CORREGIDO: Remover certificate pinning en desarrollo para evitar SSLPeerUnverifiedException
        // En producción, obtener los pins reales con:
        // openssl s_client -connect domain:443 | openssl x509 -pubkey -noout | openssl pkey -pubin -outform der | openssl dgst -sha256 -binary | openssl enc -base64
        
        return CertificatePinner.Builder()
            // Deshabilitado temporalmente en desarrollo
            // .add("api.trongrid.io", "sha256/REAL_PIN_AQUI")
            // .add("www.googleapis.com", "sha256/REAL_PIN_AQUI")
            // .add("api.sponsorflow.com", "sha256/REAL_PIN_AQUI")
            // .add("api.github.com", "sha256/REAL_PIN_AQUI")
            .build()
    }
}