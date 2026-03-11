/*
 * SponsorFlow Nexus v1.0 - Security Module (Hilt)
 * EncryptedSharedPreferences, Play Integrity, Certificate Pinning
 */
package com.sponsorflow.nexus.di

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.android.play.integrity.IntegrityManager
import com.google.android.play.integrity.IntegrityManagerFactory
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
    ): android.content.SharedPreferences {
        return EncryptedSharedPreferences.create(
            context,
            "nexus_encrypted_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    @Provides
    @Singleton
    fun provideSharedPreferences(
        encryptedSharedPreferences: EncryptedSharedPreferences
    ): android.content.SharedPreferences {
        return encryptedSharedPreferences
    }

    @Provides
    @Singleton
    fun provideIntegrityManager(@ApplicationContext context: Context): IntegrityManager {
        return IntegrityManagerFactory.create(context)
    }

}