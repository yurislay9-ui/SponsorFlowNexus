# SponsorFlow Nexus v1.0 - ProGuard Rules
# Reglas de ofuscación para mantener funcionalidad crítica

# ------------------------------
# Configuración General
# ------------------------------

# Mantener atributos de firma y anotaciones
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod

# Mantener nombres de clases y métodos para debugging
-keepattributes SourceFile,LineNumberTable

# ------------------------------
# Gson (Serialización JSON)
# ------------------------------

# Reglas para Gson - mantener clases de datos
-keep class com.sponsorflow.nexus.data.** { *; }
-keep class com.sponsorflow.nexus.model.** { *; }
-keep class com.sponsorflow.nexus.network.** { *; }
-keep class com.sponsorflow.nexus.subscription.** { *; }
-keep class com.sponsorflow.nexus.inventory.** { *; }
-keep class com.sponsorflow.nexus.sentiment.** { *; }
-keep class com.sponsorflow.nexus.plugin.** { *; }

# Reglas específicas para Gson
-keep class com.google.gson.** { *; }
-keep class com.google.gson.stream.** { *; }
-keep class com.google.gson.annotations.** { *; }

# Mantener constructores sin argumentos para clases de datos
-keepclassmembers class * {
    public <init>();
}

# Mantener métodos getter y setter
-keepclassmembers class * {
    public <fields>;
    public <methods>;
}

# ------------------------------
# Gson (Serialización JSON)
# ------------------------------

# Reglas para Gson - mantener clases de datos
-keep class com.sponsorflow.nexus.data.** { *; }
-keep class com.sponsorflow.nexus.model.** { *; }
-keep class com.sponsorflow.nexus.network.** { *; }
-keep class com.sponsorflow.nexus.subscription.** { *; }
-keep class com.sponsorflow.nexus.inventory.** { *; }
-keep class com.sponsorflow.nexus.sentiment.** { *; }
-keep class com.sponsorflow.nexus.plugin.** { *; }

# Reglas específicas para Gson
-keep class com.google.gson.** { *; }
-keep class com.google.gson.stream.** { *; }
-keep class com.google.gson.annotations.** { *; }

# Mantener constructores sin argumentos para clases de datos
-keepclassmembers class * {
    public <init>();
}

# Mantener métodos getter y setter
-keepclassmembers class * {
    public <fields>;
    public <methods>;
}

# ------------------------------
# Room Database
# ------------------------------

# Mantener clases de Room
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class *
-keep class * extends androidx.room.Entity { *; }

# Mantener DAOs
-keep interface * extends androidx.room.Dao { *; }

# Excluir warnings de Room
-dontwarn androidx.room.paging.**
-dontwarn androidx.room.CoroutinesRoom

# ------------------------------
# Android Components
# ------------------------------

# Mantener Application class
-keep class com.sponsorflow.nexus.SponsorFlowNexusApplication { *; }

# Mantener Activities
-keep class * extends android.app.Activity { *; }
-keep class * extends androidx.appcompat.app.AppCompatActivity { *; }

# Mantener Services
-keep class * extends android.app.Service { *; }
-keep class * extends androidx.core.app.JobIntentService { *; }

# Mantener BroadcastReceivers
-keep class * extends android.content.BroadcastReceiver { *; }

# Mantener ContentProviders
-keep class * extends android.content.ContentProvider { *; }

# ------------------------------
# WorkManager
# ------------------------------

# Mantener WorkManager classes
-keep class * extends androidx.work.Worker { *; }
-keep class * extends androidx.work.ListenableWorker { *; }

# ------------------------------
# Compose UI
# ------------------------------

# Mantener clases de Compose
-keep class androidx.compose.** { *; }
-keep class com.sponsorflow.nexus.ui.** { *; }

# ------------------------------
# Networking (OkHttp, Retrofit)
# ------------------------------

# Mantener clases de networking
-keep class com.sponsorflow.nexus.network.** { *; }
-keep class com.squareup.okhttp3.** { *; }
-keep class retrofit2.** { *; }
-keep class okhttp3.** { *; }

# ------------------------------
# Security & Encryption
# ------------------------------

# Mantener clases de seguridad
-keep class com.sponsorflow.nexus.security.** { *; }
-keep class androidx.security.crypto.** { *; }
-keep class android.security.** { *; }
-keep class java.security.** { *; }
-keep class javax.crypto.** { *; }

# ------------------------------
# Firebase (Analytics, Crashlytics)
# ------------------------------

# Mantener Firebase classes
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

# ------------------------------
# Reflection & Dynamic Loading
# ------------------------------

# Mantener clases usadas por reflexión
-keep class com.sponsorflow.nexus.plugin.** { *; }
-keep class com.sponsorflow.nexus.ai.** { *; }
-keep class com.sponsorflow.nexus.antidetection.** { *; }

# ------------------------------
# Third-party Libraries
# ------------------------------

# Mantener librerías externas críticas
-keep class org.json.** { *; }
-keep class com.google.gson.** { *; }
-keep class com.squareup.okhttp3.** { *; }
-keep class retrofit2.** { *; }
-keep class androidx.lifecycle.** { *; }
-keep class androidx.room.** { *; }
-keep class androidx.work.** { *; }
-keep class androidx.compose.** { *; }

# ------------------------------
# Debug & Development
# ------------------------------

# En modo debug, mantener más información
-if class ** {
    @androidx.annotation.Keep <methods>;
}
-keep,allowobfuscation @interface androidx.annotation.Keep

-keep,allowobfuscation class * {
    @androidx.annotation.Keep *;
}

# ------------------------------
# Optimizations
# ------------------------------

# Desactivar optimizaciones que puedan romper funcionalidad
-dontoptimize
-dontpreverify
-dontshrink

# ------------------------------
# Logging
# ------------------------------

# Mantener logging para debugging
-keep class com.sponsorflow.nexus.NexusLogger { *; }
-keep class android.util.Log { *; }
-keep class timber.log.Timber { *; }