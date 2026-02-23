# SponsorFlow Nexus v2.4

## 🤖 Asistente de WhatsApp con Inteligencia Artificial

SponsorFlow Nexus es una aplicación Android que funciona como asistente automatizado de WhatsApp, utilizando IA para responder mensajes de forma inteligente y natural.

---

## 📋 Tabla de Contenidos

- [Características](#-características)
- [Planes de Suscripción](#-planes-de-suscripción)
- [Arquitectura](#-arquitectura)
- [Módulos](#-módulos)
- [Instalación](#-instalación)
- [Configuración](#-configuración)
- [Compilación](#-compilación)
- [APIs y Contratos](#apis-y-contratos)
- [Seguridad](#-seguridad)
- [CI/CD](#cicd)

---

## ✨ Características

### 🧠 Inteligencia Artificial
- Motor de IA integrado con soporte para LLaMA
- Memoria de conversación configurable por plan
- Procesamiento de lenguaje natural (NLP)
- Detección de intenciones automática
- Análisis de sentimientos

### 💬 WhatsApp Integration
- Respuestas automáticas inteligentes
- Anti-detección con comportamiento humano
- Patrones de rotación para evitar bloqueos
- Delays aleatorios realistas

### 📦 Gestión de Productos
- Inventario completo
- Categorización automática
- Búsqueda inteligente

### 🔌 Sistema de Plugins
- Plugins extensible
- SDK para desarrollo
- Categorías de plugins

### 💳 Pagos con Criptomonedas
- USDT (TRC20) en red Tron
- Verificación automática de transacciones
- Sistema de licencias seguro

---

## 💎 Planes de Suscripción

| Plan | Precio | Memoria | Inventario | Plugins | SDK |
|------|--------|---------|------------|---------|-----|
| GRATIS | $0 | Sin memoria | ❌ | ❌ | ❌ |
| OBSERVADOR | $9/mes | 5 conversaciones | ✅ | Básicos | ❌ |
| DESARROLLO | $19/mes | 20 conversaciones | Ilimitado | Avanzados | ❌ |
| EMPRESARIO | $29/mes | Ilimitada | Ilimitado | Todos | ✅ |

---

## 🏗️ Arquitectura

```
app/src/main/java/com/sponsorflow/nexus/
├── account/           # Gestión de cuenta y licencias
├── admin/             # Controles de administrador
├── ai/                # Motor de IA y memoria
├── analytics/         # Análisis y métricas
├── antidetection/     # Anti-detección para WhatsApp
├── cache/             # Caché y memoria de IA
├── config/            # Configuración dinámica y segura
├── core/              # Núcleo, enums, contratos
├── data/              # Base de datos, DAOs, entidades
├── di/                # Inyección de dependencias (Hilt)
├── integration/       # APIs de integración externa
├── inventory/         # Gestión de productos
├── language/          # Soporte multiidioma
├── network/           # Red, reintentos, monitoreo
├── nlp/               # Procesamiento de lenguaje natural
├── offline/           # Modo offline y sincronización
├── plugin/            # Sistema de plugins
├── rust/              # Bridge a código Rust nativo
├── security/          # Encriptación e integridad
├── sentiment/         # Análisis de sentimientos
├── subscription/      # Gestión de suscripciones
├── ui/                # Interfaz de usuario (Jetpack Compose)
├── whatsapp/          # Servicio de WhatsApp
└── NexusApplication.kt # Clase Application principal
```

---

## 📦 Módulos

### 1. Core (`/core`)
Núcleo de la aplicación con enums, contratos y resultados.

**Enums principales:**
- `SubscriptionTier` - Niveles de suscripción
- `OperationStatus` - Estados de operación

**Contratos:**
- `IAIEngine` - Interface del motor de IA
- `ILicenseValidator` - Validador de licencias
- `IEncryptionService` - Servicio de encriptación
- `IRepository` - Repositorio genérico
- `IPluginContract` - Contrato de plugins

### 2. AI (`/ai`)
Motor de inteligencia artificial.

**Componentes:**
- `AIEngine.kt` - Motor principal de IA
- `LlamaBridge.kt` - Bridge para modelo LLaMA
- `MemoryLimiter.kt` - Límites de memoria por plan
- `CloudAIProvider.kt` - Proveedor de IA en la nube
- `ModelValidator.kt` - Validador de modelos

### 3. Subscription (`/subscription`)
Sistema de suscripciones y pagos.

**Componentes:**
- `SubscriptionGate.kt` - Control de acceso por plan
- `PaymentManager.kt` - Gestión de pagos
- `TronScanVerifier.kt` - Verificación en blockchain Tron
- `TxHashRegistry.kt` - Registro de transacciones

### 4. Security (`/security`)
Seguridad y encriptación.

**Componentes:**
- `EncryptionManager.kt` - Gestor de encriptación
- `IntegrityChecker.kt` - Verificador de integridad
- `IntegrityService.kt` - Servicio de integridad

### 5. Anti-Detection (`/antidetection`)
Evita detección por WhatsApp.

**Componentes:**
- `HumanBehavior.kt` - Simula comportamiento humano
- `PatternRotator.kt` - Rota patrones de respuesta

### 6. WhatsApp (`/whatsapp`)
Integración con WhatsApp.

**Componentes:**
- `WhatsAppService.kt` - Servicio de accesibilidad

### 7. Plugin (`/plugin`)
Sistema de plugins extensible.

**Componentes:**
- `PluginManager.kt` - Gestor de plugins
- `PluginAPI.kt` - API pública para plugins
- `PluginTypes.kt` - Tipos de datos de plugins

### 8. Data (`/data`)
Capa de datos con Room Database.

**Entidades:**
- `ContactEntity.kt`
- `ConversationEntity.kt`
- `ProductEntity.kt`
- `MetricEntity.kt`
- `TemplateEntity.kt`
- `SubscriptionEntity.kt`

**DAOs:**
- `ContactDao.kt`
- `ConversationDao.kt`
- `ProductDao.kt`
- `MetricDao.kt`
- `TemplateDao.kt`
- `SubscriptionDao.kt`

### 9. UI (`/ui`)
Interfaz de usuario con Jetpack Compose.

**Pantallas:**
- `HomeScreen` - Dashboard principal
- `AssistantChatScreen` - Chat con IA
- `InventoryManagementScreen` - Gestión de inventario
- `IntegrationsScreen` - Configuración
- `SubscriptionScreen` - Planes de suscripción

### 10. Rust (`/rust`)
Código nativo Rust para rendimiento.

**Módulos:**
- `crypto/` - Criptografía (AES, Hash, KDF)
- `payment/` - Validación de pagos Tron
- `jni/` - Bridge JNI con Android

---

## 🚀 Instalación

### Requisitos
- Android 8.0+ (API 26)
- JDK 17
- Android Studio Hedgehog+
- Rust (para compilación nativa)

### Pasos

```bash
# Clonar repositorio
git clone https://github.com/yurislay9-ui/SponsorFlowNexus.git
cd SponsorFlowNexus

# Compilar
./gradlew assembleDebug

# Instalar en dispositivo
adb install app/build/outputs/apk/debug/app-debug.apk
```

---

## ⚙️ Configuración

### Archivos de configuración

**`gradle.properties`**
```properties
android.useAndroidX=true
android.enableJetifier=true
kotlin.code.style=official
```

**`app/build.gradle.kts`**
```kotlin
android {
    compileSdk = 34
    defaultConfig {
        applicationId = "com.sponsorflow.nexus"
        minSdk = 26
        targetSdk = 34
    }
}
```

### Permisos requeridos

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
```

---

## 🔨 Compilación

### Debug APK
```bash
./gradlew assembleDebug
```

### Release APK
```bash
./gradlew assembleRelease
```

### Compilar Rust
```bash
cd app/src/main/rust
./build_android.sh
```

---

## 🔐 Seguridad

### Encriptación
- AES-256-GCM para datos sensibles
- EncryptedSharedPreferences
- MasterKey con KeyScheme_AES256_GCM

### Verificación de Integridad
- Verificación de firma de app
- Detección de instalación desde fuente no autorizada
- Validación de licencia offline

### Pago Seguro
- Verificación en blockchain Tron
- Validación de transacciones USDT TRC20
- Sin datos de tarjeta almacenados

---

## 🔄 CI/CD

### GitHub Actions Workflows

**`.github/workflows/build.yml`**
- Compilación automática de APK
- Upload de artifacts
- Soporte para ZIP externo

**`.github/workflows/ci.yml`**
- Integración continua
- Tests automáticos

### Descargar APK

1. Ir a [Actions](https://github.com/yurislay9-ui/SponsorFlowNexus/actions)
2. Seleccionar el workflow completado
3. Descargar artifact `SponsorFlowNexus-debug-apk`
4. Extraer el ZIP para obtener el APK

---

## 📱 Uso

### Activar Asistente

1. Abrir la app SponsorFlow Nexus
2. Presionar "ACTIVAR ASISTENTE"
3. Conceder permisos de accesibilidad
4. El servicio comenzará a escuchar mensajes de WhatsApp

### Configurar Suscripción

1. Ir a "Ver Planes de Suscripción"
2. Seleccionar plan deseado
3. Realizar pago en USDT TRC20
4. La activación es automática

---

## 📊 Monitoreo

### Métricas disponibles
- Mensajes procesados
- Respuestas enviadas
- Tiempo de respuesta
- Errores encontrados

### Logs
Los logs se almacenan de forma segura en:
- `nexus_secure_prefs` (encriptado)
- `nexus_ai_memory` (memoria de IA)

---

## 🐛 Solución de Problemas

### La app no abre
1. Verificar que no esté instalada una versión anterior
2. Limpiar datos de la app
3. Reinstalar APK

### El servicio no responde
1. Verificar permisos de accesibilidad
2. Verificar que WhatsApp esté instalado
3. Reiniciar el servicio

### Error de licencia
1. Verificar transacción en TronScan
2. Contactar soporte con hash de transacción

---

## 📞 Soporte

- **Email:** soporte@sponsorflow.com
- **GitHub Issues:** [Crear issue](https://github.com/yurislay9-ui/SponsorFlowNexus/issues)

---

## 📄 Licencia

Este proyecto es propiedad de SponsorFlow. Todos los derechos reservados.

---

**Versión:** 2.4  
**Última actualización:** Febrero 2026  
**Build:** 553a52c