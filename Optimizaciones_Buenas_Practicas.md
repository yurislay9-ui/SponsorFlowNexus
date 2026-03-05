# Documento 2: Optimizaciones y Buenas Prácticas

## SponsorFlowNexus - Módulo WhatsApp
**Fecha:** 4 de Marzo 2026  
**Prioridad:** MEDIA - Mejoras de rendimiento y mantenibilidad

---

## Resumen Ejecutivo

Este documento contiene optimizaciones y mejoras de buenas prácticas que no causan fallos de compilación inmediatos, pero que son importantes para la mantenibilidad, rendimiento y estabilidad del proyecto a largo plazo.

---

## Optimizaciones de Versiones

### OPT-001: Actualizar Compose BOM
**Archivo:** `libs.versions.toml`  
**Ubicación:** `[versions] compose = "2024.02.00"`  
**Severidad:** MEDIA  

```toml
# CONFIGURACIÓN ACTUAL
compose = "2024.02.00"

# CONFIGURACIÓN RECOMENDADA
compose = "2024.02.02"

# O la versión LTS más reciente:
compose = "2024.04.01"
```

**Explicación:** La versión 2024.02.00 del BOM de Compose fue la primera release de febrero 2024. Google publicó rápidamente 2024.02.02 con correcciones importantes de bugs. La versión 2024.04.01 es la LTS recomendada.

---

### OPT-002: Actualizar Retrofit para compatibilidad con OkHttp
**Archivo:** `libs.versions.toml`  
**Ubicación:** `[versions] retrofit = "2.9.0"`  
**Severidad:** MEDIA  

```toml
# CONFIGURACIÓN ACTUAL
okhttp = "4.12.0"
retrofit = "2.9.0"

# CONFIGURACIÓN RECOMENDADA
okhttp = "4.12.0"
retrofit = "2.11.0"
```

**Explicación:** Retrofit 2.9.0 fue publicado en 2022 y tiene dependencia sobre OkHttp 4.x, pero la combinación específica con OkHttp 4.12.0 puede presentar problemas de compatibilidad. Retrofit 2.11.0 (2024) está diseñado y probado con OkHttp 4.12.x.

---

### OPT-003: Resolver colisión de alias 'hilt-android'
**Archivo:** `libs.versions.toml`  
**Ubicación:** `[plugins] hilt-android` y `[libraries] hilt-android`  
**Severidad:** MEDIA  

```toml
# CONFIGURACIÓN ACTUAL (AMBIGUA)
[plugins]
hilt-android = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }

[libraries]
hilt-android = { module = "com.google.dagger:hilt-android", version.ref = "hilt" }

# CONFIGURACIÓN RECOMENDADA
[plugins]
hilt-gradle-plugin = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }

[libraries]
hilt-android = { module = "com.google.dagger:hilt-android", version.ref = "hilt" }
```

**Explicación:** Tener 'hilt-android' definido tanto en [plugins] como en [libraries] puede causar confusión en el IDE y errores de resolución de accessor en Kotlin DSL.

---

## Optimizaciones de Dependencias

### OPT-004: Agregar androidx.activity:activity-ktx
**Archivo:** `libs.versions.toml`  
**Ubicación:** `[libraries]` - entrada faltante  
**Severidad:** BAJA  

```toml
# Agregar en [versions]:
activityKtx = "1.8.2"

# Agregar en [libraries]:
androidx-activity-ktx = { module = "androidx.activity:activity-ktx", version.ref = "activityKtx" }
```

**Explicación:** El proyecto usa navigation-fragment-ktx y navigation-compose simultáneamente. La librería activity-ktx es necesaria para usar `by viewModels()`, `activityViewModels()`, y el manejo correcto del ciclo de vida en actividades con soporte Kotlin.

---

### OPT-005: Agregar dependencia androidx.test:runner
**Archivo:** `libs.versions.toml`  
**Ubicación:** `[libraries]` - entrada faltante  
**Severidad:** ALTA  

```toml
# Agregar en [versions]:
androidxTestRunner = "1.5.2"

# Agregar en [libraries]:
androidx-test-runner = { module = "androidx.test:runner", version.ref = "androidxTestRunner" }
```

**Explicación:** Se requiere para ejecutar tests instrumentados. Debe agregarse como dependencia androidTestImplementation.

---

### OPT-006: Evaluar uso de material-icons-extended
**Archivo:** `libs.versions.toml` / `app/build.gradle.kts`  
**Ubicación:** dependencia material-icons-extended  
**Severidad:** MEDIA  

```toml
# CONFIGURACIÓN ACTUAL (+17MB al APK)
androidx-compose-material-icons = { module = "androidx.compose.material:material-icons-extended" }

# CONFIGURACIÓN RECOMENDADA (iconos esenciales)
androidx-compose-material-icons-core = { module = "androidx.compose.material:material-icons-core" }
```

**Explicación:** El artefacto 'material-icons-extended' contiene TODOS los iconos de Material Design (+900) y agrega aproximadamente 17-25MB al APK final. Dado que isMinifyEnabled=false en release, estos iconos no serán eliminados por tree-shaking.

**Recomendación:**
- Si se usan pocos iconos específicos, usar `material-icons-core` + importar solo los iconos necesarios
- Si se habilita minificación (R8), los iconos no usados serán eliminados automáticamente

---

## Optimizaciones de Configuración

### OPT-007: Configurar incremental para KSP
**Archivo:** `gradle.properties`  
**Ubicación:** configuración de KSP  
**Severidad:** BAJA  

```properties
# Agregar estas líneas para mejorar rendimiento de build:
kotlin.incremental=true
ksp.incremental=true
ksp.incremental.log=false

# Si hay problemas con configuration cache:
org.gradle.configuration-cache.problems=warn
```

**Explicación:** Hilt 2.50 con KSP y configuration cache de Gradle tiene soporte experimental. Agregar estas optimizaciones mejora significativamente el tiempo de build.

---

### OPT-008: Documentar versiones centralizadas
**Archivo:** `libs.versions.toml`  
**Ubicación:** Sección [versions]  
**Severidad:** BAJA  

```toml
# Estructura recomendada con comentarios:

[versions]
# Build Tools
agp = "8.2.2"
kotlin = "1.9.22"
ksp = "1.9.22-1.0.17"

# Compose
compose = "2024.02.02"
composeCompiler = "1.5.10"

# UI Components
activity = "1.8.2"
appcompat = "1.6.1"
coreKtx = "1.12.0"

# Dependency Injection
hilt = "2.50"
hiltExt = "1.2.0"

# Networking
okhttp = "4.12.0"
retrofit = "2.11.0"

# Database
room = "2.6.1"

# Security
security = "1.0.0"  # Versión estable, no alpha

# Testing
androidxTest = "1.1.5"
androidxTestRunner = "1.5.2"
espresso = "3.5.1"
junit = "4.13.2"
```

---

## Verificación de Compatibilidad

### Compatibilidad Kotlin-Compose Compiler

| Kotlin Version | Compose Compiler Version |
|----------------|-------------------------|
| 1.9.0          | 1.5.0                   |
| 1.9.10         | 1.5.3                   |
| 1.9.20         | 1.5.4                   |
| 1.9.21         | 1.5.6                   |
| 1.9.22         | 1.5.10 ✓                |
| 1.9.23         | 1.5.11                  |

### Compatibilidad AGP-Gradle

| AGP Version | Gradle Mínimo | Gradle Máximo |
|-------------|---------------|---------------|
| 8.2.0       | 8.0           | 8.5           |
| 8.2.2       | 8.2           | 8.6 ✓         |

### Compatibilidad Hilt-Navigation-Compose

hilt-navigation-compose 1.2.0 requiere:
- Hilt 2.48+ (OK, se usa 2.50 ✓)
- Compose Navigation 2.6.0+ (OK, se usa 2.7.7 ✓)
- Kotlin 1.8.0+ (OK, se usa 1.9.22 ✓)

---

## Resumen de Cambios

| ID | Archivo | Cambio | Prioridad |
|----|---------|--------|-----------|
| OPT-001 | libs.versions.toml | Actualizar Compose BOM | MEDIA |
| OPT-002 | libs.versions.toml | Actualizar Retrofit | MEDIA |
| OPT-003 | libs.versions.toml | Resolver colisión hilt-android | MEDIA |
| OPT-004 | libs.versions.toml | Agregar activity-ktx | BAJA |
| OPT-005 | libs.versions.toml | Agregar test runner | ALTA |
| OPT-006 | libs.versions.toml | Evaluar material-icons | MEDIA |
| OPT-007 | gradle.properties | Configurar incremental KSP | BAJA |
| OPT-008 | libs.versions.toml | Documentar versiones | BAJA |

---

**Nota:** Este documento debe aplicarse DESPUÉS de completar las correcciones del Documento 1.