# Documento 1: Correcciones Críticas y de Alta Severidad

## SponsorFlowNexus - Módulo WhatsApp
**Fecha:** 4 de Marzo 2026  
**Prioridad:** CRÍTICA - Errores que causan fallos de compilación inmediatos

---

## Resumen Ejecutivo

Este documento contiene las correcciones urgentes para los errores de configuración que están causando fallos de compilación y problemas de seguridad en producción. Estos errores deben ser corregidos **inmediatamente** antes de cualquier otro trabajo de desarrollo.

---

## Errores Críticos (Prioridad 1)

### ERR-001: Versión incompatible del Compose Compiler
**Archivo:** `libs.versions.toml`  
**Ubicación:** `[versions] composeCompiler = "1.5.8"`  
**Severidad:** CRÍTICA  
**Impacto:** Fallo de compilación inmediato

```toml
# CONFIGURACIÓN ACTUAL (ERRONEA)
composeCompiler = "1.5.8"

# CONFIGURACIÓN CORREGIDA
composeCompiler = "1.5.10"
```

**Explicación:** La versión 1.5.8 del Compose Compiler corresponde a Kotlin 1.9.20, pero el proyecto usa Kotlin 1.9.22 que requiere la versión 1.5.10 del compilador según la tabla oficial de compatibilidad de AndroidX.

**Error de compilación actual:**
```
This version (1.5.8) of the Compose Compiler requires Kotlin version 1.9.20 but 1.9.22 was found
```

---

### ERR-002: Versión incorrecta para androidx.test.ext:junit
**Archivo:** `libs.versions.toml`  
**Ubicación:** `[versions] androidxTest = "1.5.0"`  
**Severidad:** CRÍTICA  
**Impacto:** Fallo de resolución de dependencias

```toml
# CONFIGURACIÓN ACTUAL (ERRONEA)
androidxTest = "1.5.0"

# CONFIGURACIÓN CORREGIDA
androidxTestExtJunit = "1.1.5"
androidxTest = "1.1.5"
```

**Explicación:** La versión '1.5.0' corresponde al artefacto 'androidx.test:runner', NO a 'androidx.test.ext:junit'. La versión correcta y estable de 'androidx.test.ext:junit' es '1.1.5'.

**Error de compilación actual:**
```
Could not find androidx.test.ext:junit:1.5.0
```

---

### ERR-003: Propiedad duplicada org.gradle.jvmargs
**Archivo:** `gradle.properties`  
**Ubicación:** Líneas 3 y 19  
**Severidad:** ALTA  
**Impacto:** Comportamiento inconsistente entre entornos

```properties
# CONFIGURACIÓN ACTUAL (ERRONEA)
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
...
org.gradle.jvmargs=-Xmx4096m -XX:+HeapDumpOnOutOfMemoryError -Dfile.encoding=UTF-8

# CONFIGURACIÓN CORREGIDA
org.gradle.jvmargs=-Xmx4096m -XX:+HeapDumpOnOutOfMemoryError -Dfile.encoding=UTF-8
```

**Explicación:** Gradle procesa las propiedades de forma secuencial y la segunda definición sobrescribe la primera de manera impredecible. Esto causa comportamientos inconsistentes entre entornos locales y CI.

---

### ERR-004: Plugin KSP sin versión centralizada
**Archivo:** `libs.versions.toml`  
**Ubicación:** `[plugins] ksp = { id = "com.google.devtools.ksp", version = "1.9.22-1.0.17" }`  
**Severidad:** ALTA  
**Impacto:** Inconsistencia de versiones entre módulos

```toml
# CONFIGURACIÓN ACTUAL (ERRONEA)
ksp = { id = "com.google.devtools.ksp", version = "1.9.22-1.0.17" }

# CONFIGURACIÓN CORREGIDA
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }

# Y agregar en [versions]:
ksp = "1.9.22-1.0.17"
```

**Explicación:** La versión de KSP está hardcodeada directamente en la sección [plugins] pero aparece también hardcodeada en root build.gradle y settings.gradle. Si se actualiza en un lugar no se actualiza en los otros, generando inconsistencias.

---

### ERR-005: Plugin AGP sin versión centralizada
**Archivo:** `libs.versions.toml`  
**Ubicación:** `[plugins] android-application = { id = "com.android.application", version = "8.2.2" }`  
**Severidad:** ALTA  
**Impacto:** Falta plugin android-library en el catálogo

```toml
# CONFIGURACIÓN ACTUAL (ERRONEA)
android-application = { id = "com.android.application", version = "8.2.2" }

# CONFIGURACIÓN CORREGIDA
android-library = { id = "com.android.library", version.ref = "agp" }
android-application = { id = "com.android.application", version.ref = "agp" }

# Y agregar en [versions]:
agp = "8.2.2"
```

**Explicación:** La versión de AGP está hardcodeada directamente en la declaración del plugin en [plugins] sin referenciar [versions]. Además, el plugin android-library no existe en la sección [plugins] del TOML.

---

### ERR-006: Bloque plugins duplicado en settings.gradle
**Archivo:** `settings.gradle.kts`  
**Ubicación:** `pluginManagement { plugins { ... } }`  
**Severidad:** ALTA  
**Impacto:** Conflictos de resolución de versiones

```kotlin
// CONFIGURACIÓN ACTUAL (ERRONEA)
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    
    plugins {
        id("com.android.application") version "8.2.2"
        id("com.android.library") version "8.2.2"
        id("org.jetbrains.kotlin.android") version "1.9.22"
        id("com.google.devtools.ksp") version "1.9.22-1.0.17"
        id("com.google.dagger.hilt.android") version "2.50"
        id("org.jetbrains.kotlin.plugin.serialization") version "1.9.22"
    }
}

// CONFIGURACIÓN CORREGIDA
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
```

**Explicación:** Cuando se usa un Version Catalog (libs.versions.toml) junto con el bloque plugins{} en root build.gradle, definir nuevamente los plugins con versiones explícitas en el bloque pluginManagement { plugins {} } de settings.gradle crea una triple declaración de versiones que puede causar conflictos.

---

### ERR-007: isMinifyEnabled = false en release build
**Archivo:** `app/build.gradle.kts`  
**Ubicación:** `buildTypes { release { isMinifyEnabled = false } }`  
**Severidad:** ALTA  
**Impacto:** Problemas de seguridad en producción

```kotlin
// CONFIGURACIÓN ACTUAL (ERRONEA)
release {
    isMinifyEnabled = false
    proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro"
    )
}

// CONFIGURACIÓN CORREGIDA
release {
    isMinifyEnabled = true
    isShrinkResources = true
    proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro"
    )
}
```

**Explicación:** Con isMinifyEnabled = false la app de producción no tiene ofuscación ni minificación. Esto es un problema crítico de seguridad en una app que usa autenticación y maneja URLs de servidor en BuildConfig. Además, las reglas de ProGuard configuradas son completamente ignoradas cuando isMinifyEnabled es false.

---

### ERR-008: Falta testInstrumentationRunner
**Archivo:** `app/build.gradle.kts`  
**Ubicación:** `defaultConfig { }`  
**Severidad:** ALTA  
**Impacto:** No se pueden ejecutar tests instrumentados

```kotlin
// CONFIGURACIÓN ACTUAL (ERRONEA)
defaultConfig {
    applicationId = "com.sponsorflow.nexus"
    minSdk = 26
    targetSdk = 34
    versionCode = 24
    versionName = "2.4.0"
    // No hay testInstrumentationRunner
}

// CONFIGURACIÓN CORREGIDA
defaultConfig {
    applicationId = "com.sponsorflow.nexus"
    minSdk = 26
    targetSdk = 34
    versionCode = 24
    versionName = "2.4.0"
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
}
```

**Explicación:** El proyecto tiene dependencias de androidTest (espresso, junit ext) pero no declara 'testInstrumentationRunner' en defaultConfig. Sin esta propiedad, los tests instrumentados no pueden ejecutarse.

---

## Errores de Alta Severidad (Prioridad 2)

### ERR-009: Dependencia sqlite-ktx hardcodeada
**Archivo:** `app/build.gradle.kts`  
**Ubicación:** `implementation("androidx.sqlite:sqlite-ktx:2.4.0")`  
**Severidad:** MEDIA  
**Impacto:** Falta de gestión centralizada de versiones

```kotlin
// CONFIGURACIÓN ACTUAL (ERRONEA)
implementation("androidx.sqlite:sqlite-ktx:2.4.0")

// CONFIGURACIÓN CORREGIDA
// En libs.versions.toml [versions]:
sqliteKtx = "2.4.0"

// En libs.versions.toml [libraries]:
androidx-sqlite-ktx = { module = "androidx.sqlite:sqlite-ktx", version.ref = "sqliteKtx" }

// En app/build.gradle.kts:
implementation(libs.androidx.sqlite.ktx)
```

---

### ERR-010: Versión alpha de security-crypto en producción
**Archivo:** `libs.versions.toml`  
**Ubicación:** `[versions] security = "1.1.0-alpha06"`  
**Severidad:** MEDIA  
**Impacto:** Inestabilidad en producción

```toml
# CONFIGURACIÓN ACTUAL (ERRONEA)
security = "1.1.0-alpha06"

# CONFIGURACIÓN CORREGIDA
security = "1.0.0"
```

**Explicación:** La versión 1.1.0-alpha06 de androidx.security:security-crypto es una versión alpha inestable. La versión estable actual es 1.0.0.

---

## Acciones Inmediatas Requeridas

1. **Corregir ERR-001 y ERR-002** - Estos son errores críticos que causan fallos de compilación
2. **Corregir ERR-003** - Eliminar propiedad duplicada en gradle.properties
3. **Corregir ERR-006** - Eliminar bloque plugins duplicado en settings.gradle
4. **Corregir ERR-007** - Habilitar minificación en release build
5. **Corregir ERR-008** - Agregar testInstrumentationRunner

---

## Verificación Post-Corrección

Después de aplicar estas correcciones, ejecutar:

```bash
./gradlew clean
./gradlew build
./gradlew assembleRelease
```

Todos los comandos deben completarse exitosamente sin errores de compilación.

---

**Nota:** Este documento contiene solo las correcciones críticas. Las optimizaciones y mejoras de arquitectura se detallan en los Documentos 2 y 3.