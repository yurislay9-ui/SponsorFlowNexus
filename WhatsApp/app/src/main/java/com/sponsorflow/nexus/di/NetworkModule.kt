/*
 * SponsorFlow Nexus v1.0 - Network Module (Hilt)
 * Uses NexusConfigManager - URLs loaded from GitHub (BRAIN)
 */
package com.sponsorflow.nexus.di

import android.content.Context
import com.sponsorflow.nexus.BuildConfig
import com.sponsorflow.nexus.config.NexusConfigManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.CertificatePinner
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideNexusConfigManager(
        @ApplicationContext context: Context
    ): NexusConfigManager {
        val manager = NexusConfigManager(context)
        manager.loadCachedConfig()
        return manager
    }


    @Provides
    @Singleton
    fun provideCertificatePinner(): CertificatePinner {
        return CertificatePinner.Builder()
            .build()
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        certificatePinner: CertificatePinner
    ): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.BASIC
            }
        }

        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .certificatePinner(certificatePinner)
            .addInterceptor(logging)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Accept", "application/json")
                    .build()
                chain.proceed(request)
            }
            .retryOnConnectionFailure(true)
            .build()
    }

    // TRON API - loaded from GitHub config
    @Provides
    @Singleton
    @Named("tron")
    fun provideTronRetrofit(
        okHttpClient: OkHttpClient,
        configManager: NexusConfigManager
    ): Retrofit {
        val baseUrl = configManager.getString("tron_api_url", "https://api.trongrid.io/")
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // Server API - loaded from GitHub config (BRAIN)
    @Provides
    @Singleton
    @Named("server")
    fun provideServerRetrofit(
        okHttpClient: OkHttpClient,
        configManager: NexusConfigManager
    ): Retrofit {
        val baseUrl = configManager.getApiBaseUrl().ifEmpty { "https://api.sponsorflow.com/" }
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // GitHub API - loaded from GitHub config
    @Provides
    @Singleton
    @Named("github")
    fun provideGitHubRetrofit(
        okHttpClient: OkHttpClient,
        configManager: NexusConfigManager
    ): Retrofit {
        val baseUrl = configManager.getString("github_api_url", "https://api.github.com/")
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
