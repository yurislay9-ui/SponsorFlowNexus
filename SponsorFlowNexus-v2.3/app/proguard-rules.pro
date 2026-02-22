# SponsorFlow Nexus v2.4 - ProGuard Rules
# Skill: Seguridad - Mantener clases críticas

# Room
-keep class com.sponsorflow.nexus.data.entity.** { *; }
-keep class com.sponsorflow.nexus.data.dao.** { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }

# llama.cpp JNI
-keep class com.sponsorflow.nexus.ai.LlamaBridge { *; }
-keepclasseswithmembernames class * { native <methods>; }

# Plugins
-keep interface com.sponsorflow.nexus.plugin.IPluginContract { *; }
-keep class com.sponsorflow.nexus.plugin.PluginContext { *; }
-keep class com.sponsorflow.nexus.plugin.PluginOutput { *; }
-keep class com.sponsorflow.nexus.plugin.PluginManager { *; }
-keep class com.sponsorflow.nexus.plugin.** { *; }

# Admin Control
-keep class com.sponsorflow.nexus.admin.** { *; }

# Integration
-keep class com.sponsorflow.nexus.integration.** { *; }

# Gson
-keepattributes Signature
-keep @com.google.gson.annotations.SerializedName class * { *; }

# SQLCipher
-dontwarn net.sqlcipher.**
-keep class net.sqlcipher.** { *; }

# OkHttp
-dontwarn okhttp3.**
-keep class okhttp3.** { *; }

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}

# Retrofit (si se usa)
-keep class retrofit2.** { *; }
-keep interface retrofit2.** { *; }
-keepclassmembernames class * {
    @retrofit2.http.* <methods>;
}

# Data Classes para Gson
-keep class com.sponsorflow.nexus.**.model.** { *; }
-keep class com.sponsorflow.nexus.**.entity.** { *; }
-keep class com.sponsorflow.nexus.**.dto.** { *; }

# Offline Queue
-keep class com.sponsorflow.nexus.offline.** { *; }

# Account & Auth
-keep class com.sponsorflow.nexus.account.** { *; }

# Config
-keep class com.sponsorflow.nexus.config.** { *; }

# Inventory
-keep class com.sponsorflow.nexus.inventory.** { *; }

# WorkManager
-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.CoroutineWorker

# Mantener todas las data classes
-keep class **$$serializer { *; }
-keepclassmembers class * {
    *** Companion;
}
