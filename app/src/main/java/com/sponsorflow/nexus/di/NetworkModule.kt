/*
 * SponsorFlow Nexus v2.4 - Network Module (Hilt)
 * CORREGIDO: Múltiples Retrofit con diferentes base URLs
 */
package com.sponsorflow.nexus.di

import com.sponsorflow.nexus.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.CertificatePinner
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json {
        return Json {
            ignoreUnknownKeys = true
            isLenient = true
            encodeDefaults = true
            prettyPrint = false
        }
    }

    @Provides
    @Singleton
    fun provideCertificatePinner(): CertificatePinner {
        // Certificate Pinning para TRON API
        return CertificatePinner.Builder()
            .add("api.trongrid.io", "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=")
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

    // Retrofit para TRON API
    @Provides
    @Singleton
    @Named("tron")
    fun provideTronRetrofit(
        okHttpClient: OkHttpClient
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.trongrid.io/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // Retrofit para el servidor personalizado (configurable)
    @Provides
    @Singleton
    @Named("server")
    fun provideServerRetrofit(
        okHttpClient: OkHttpClient
    ): Retrofit {
        // URL base del servidor desde config
        val baseUrl = BuildConfig.SERVER_URL.ifEmpty { "https://api.example.com/" }
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // Retrofit para GitHub API
    @Provides
    @Singleton
    @Named("github")
    fun provideGitHubRetrofit(
        okHttpClient: OkHttpClient
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.github.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
