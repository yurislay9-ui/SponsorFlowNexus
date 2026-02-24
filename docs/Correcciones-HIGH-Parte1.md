# SponsorFlowNexus - Correcciones HIGH (Parte 1/2)

## Documento de Críticas y Correcciones
**Fecha:** 2026-02-24  
**Proyecto:** SponsorFlowNexus  
**Errores HIGH:** H1 - H11 (11 errores)

---

## HIGH-001: Accessibility Service con exported=false

### Ubicación
`app/src/main/AndroidManifest.xml`  
Línea: `<service android:name=".core.NexusAccessibilityService" ... android:exported="false"`

### Problema Detectado
Un Accessibility Service debe tener `android:exported="true"` para ser vinculado por el sistema. Con `android:exported="false"`, el sistema Android no puede vincularse al servicio y la función de accesibilidad nunca se activará.

### Crítica
Error de configuración crítico que impide que el servicio de accesibilidad funcione. El sistema operativo requiere `exported=true` para todos los servicios de accesibilidad. Sin esto, toda la funcionalidad de automatización de WhatsApp será inoperante.

### Corrección
```xml
<service 
    android:name=".core.NexusAccessibilityService"
    android:exported="true"
    android:permission="android.permission.BIND_ACCESSIBILITY_SERVICE">
    <intent-filter>
        <action android:name="android.accessibilityservice.AccessibilityService"/>
    </intent-filter>
    <meta-data
        android:name="android.accessibilityservice"
        android:resource="@xml/accessibility_service_config"/>
</service>
```

---

## HIGH-002: Valores Placeholder en Configuración

### Ubicación
`config.json`

### Problema Detectado
Múltiples valores de configuración están como cadenas placeholder:
- `"serverUrl": "https://TU-URL-NGROK.ngrok-free.app"`
- `"clientId": "TU_CLIENT_ID_GOOGLE_AQUI"`
- `"signature": "TU_FIRMA_SHA256_AQUI"`
- `"tronAddress": "TU_DIRECCION_TRON_TRC20_AQUI"`
- `"n8nUrl": "TU-N8N-URL"`
- `"modelUrl": "URL_DEL_MODELO_EN_GITHUB_O_HUGGINGFACE"`

Si config.json es obtenido remotamente y parseado por la app, causará fallos en runtime.

### Crítica
Error de configuración que causará fallos en producción. Las URLs placeholder no resolverán y las llamadas de red fallarán. Esto también representa un riesgo de seguridad si se commitean valores reales.

### Corrección
1. Reemplazar todos los valores placeholder con URLs y credenciales reales antes del despliegue
2. NO commitear secrets al control de versiones
3. Usar variables de entorno o un gestor de secrets:
```json
{
    "serverUrl": "${SERVER_URL}",
    "clientId": "${GOOGLE_CLIENT_ID}",
    "signature": "${APP_SIGNATURE}",
    "tronAddress": "${TRON_ADDRESS}",
    "n8nUrl": "${N8N_URL}",
    "modelUrl": "${MODEL_URL}"
}
```

---

## HIGH-003: @HiltWorker sin @AssistedInject

### Ubicación
`app/src/main/java/com/sponsorflow/nexus/NexusApplication.kt`  
Línea: ConfigSyncWorker class definition (~línea 95)

### Problema Detectado
La anotación `@HiltWorker` está presente pero el worker no inyecta dependencias via constructor `@AssistedInject`. La anotación `@HiltWorker` requiere que el constructor use `@AssistedInject` con parámetros `@Assisted` para Context y WorkerParameters. El constructor actual es un constructor plain, haciendo que `@HiltWorker` no funcione y probablemente cause crash cuando WorkManager intente instanciarlo via HiltWorkerFactory.

### Crítica
Error de implementación de Hilt que causará crash en runtime. La anotación `@HiltWorker` sin el constructor correcto es inútil y causará fallos cuando se ejecute el Worker.

### Corrección
```kotlin
class ConfigSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val dynamicConfigManager: DynamicConfigManager
) : CoroutineWorker(appContext, workerParams) {
    
    override suspend fun doWork(): Result {
        // implementación
    }
    
    companion object {
        const val WORK_NAME = "config_sync_work"
    }
}
```

---

## HIGH-004: Lógica Incorrecta en ClickLock.acquire()

### Ubicación
`app/src/main/java/com/sponsorflow/nexus/core/util/ClickLock.kt`  
Línea: `acquire()` method (~líneas 20-23)

### Problema Detectado
```kotlin
fun acquire(key: String): Boolean {
    val previous = locks.putIfAbsent(key, true)
    return previous == null || previous == false
}
```
`ConcurrentHashMap.putIfAbsent(key, true)` devuelve el valor previo si la clave existía, o null si estaba ausente. La condición `return previous == null || previous == false` significa que devuelve true (adquirido) cuando: (1) la clave estaba ausente (correcto) O (2) el valor previo era false. Pero si la clave existe con valor false (liberado), putIfAbsent NO lo actualiza — deja el valor false, devuelve false, y el método devuelve true diciendo que se adquirió sin realmente establecer true. El estado del lock ahora es inconsistente.

### Crítica
Error lógico grave en el mecanismo de bloqueo que puede causar race conditions y comportamiento impredecible. El lock puede ser "adquirido" múltiples veces por diferentes hilos.

### Corrección
```kotlin
fun acquire(key: String): Boolean {
    return locks.compute(key) { _, v ->
        if (v == true) true else true
    } == true && locks[key] == true
}

// O mejor, usar AtomicBoolean:
private val locks = ConcurrentHashMap<String, AtomicBoolean>()

fun acquire(key: String): Boolean {
    return locks.computeIfAbsent(key) { AtomicBoolean(false) }
        .compareAndSet(false, true)
}

fun release(key: String) {
    locks[key]?.set(false)
}
```

---

## HIGH-005: Referencias a DAOs Posiblemente Inexistentes

### Ubicación
`app/src/main/java/com/sponsorflow/nexus/di/DatabaseModule.kt`  
Línea: `provideDatabase()` method

### Problema Detectado
Los DAOs `ContactDao`, `TemplateDao`, `ConversationDao`, `ProductDao`, `MetricDao`, `SubscriptionDao` son importados desde `com.sponsorflow.nexus.data.dao.*` y `NexusDatabase` desde `com.sponsorflow.nexus.data.database.NexusDatabase`. Estas clases pueden no estar presentes en los archivos proporcionados. Si no existen o no coinciden con la interfaz esperada (ej. `database.metricDao()`, `database.subscriptionDao()`), esto causará errores de compilación.

### Crítica
Riesgo de error de compilación por referencias no resueltas. La arquitectura de datos puede estar incompleta o desincronizada entre el módulo de base de datos y los DAOs reales.

### Corrección
Asegurar que todas las interfaces DAO y la clase abstracta NexusDatabase existan en la capa de datos con firmas de métodos abstractos coincidentes:
```kotlin
@Database(entities = [...], version = 1)
abstract class NexusDatabase : RoomDatabase() {
    abstract fun contactDao(): ContactDao
    abstract fun templateDao(): TemplateDao
    abstract fun conversationDao(): ConversationDao
    abstract fun productDao(): ProductDao
    abstract fun metricDao(): MetricDao
    abstract fun subscriptionDao(): SubscriptionDao
}
```

---

## HIGH-006: DynamicConfigManager No Proporcionado

### Ubicación
Referenciado en `NexusApplication.kt` y `ConfigSyncWorker`

### Problema Detectado
`DynamicConfigManager` es referenciado en `NexusApplication.kt` (fetchRemoteConfig y ConfigSyncWorker.doWork) pero su archivo de clase no está proporcionado. El constructor toma un `Context` y tiene un `suspend fun fetchConfig()`. Si esta clase no existe o `fetchConfig()` no es una función suspend, causará errores de compilación.

### Crítica
Riesgo de error de compilación por clase faltante. La funcionalidad de configuración dinámica no puede operar sin esta clase.

### Corrección
Crear `DynamicConfigManager.kt`:
```kotlin
class DynamicConfigManager(private val context: Context) {
    private val prefs = context.getSharedPreferences("dynamic_config", Context.MODE_PRIVATE)
    
    suspend fun fetchConfig(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // Implementar fetch de configuración remota
                true
            } catch (e: Exception) {
                false
            }
        }
    }
    
    fun getConfig(key: String, default: String): String {
        return prefs.getString(key, default) ?: default
    }
}
```

---

## HIGH-007: IntegrityChecker Referenciado Incorrectamente

### Ubicación
`app/src/main/java/com/sponsorflow/nexus/NexusApplication.kt`  
Línea: initSecurity()

### Problema Detectado
`IntegrityChecker` es instanciado con constructor sin argumentos `IntegrityChecker()` y `runAllChecks(context)` es llamado esperando un tipo de retorno con `.passedAll: Boolean` y `.toString()`. Si esta clase no existe o tiene una interfaz diferente, causará errores de compilación.

### Crítica
Riesgo de error de compilación por interfaz incompatible. Las verificaciones de integridad son críticas para la seguridad de la aplicación.

### Corrección
Asegurar que la clase IntegrityChecker exista con la interfaz correcta:
```kotlin
class IntegrityChecker(
    private val context: Context,
    private val expectedSignature: String = ""
) {
    fun runAllChecks(context: Context): IntegrityReport {
        return IntegrityReport(
            signatureValid = checkSignature(context),
            installerValid = checkInstaller(context),
            isRooted = isRooted(),
            isEmulator = isEmulator()
        )
    }
}

data class IntegrityReport(
    val signatureValid: Boolean,
    val installerValid: Boolean,
    val isRooted: Boolean,
    val isEmulator: Boolean
) {
    val passedAll: Boolean
        get() = signatureValid && installerValid && !isRooted && !isEmulator
}
```

---

## HIGH-008: Race Condition en clearCache()

### Ubicación
`app/src/main/java/com/sponsorflow/nexus/account/LicenseVerifier.kt`  
Línea: `override fun clearCache()`

### Problema Detectado
`clearCache()` asigna `cachedLicense = null` sin usar el `cacheMutex`, mientras que `validate()` usa `cacheMutex.withLock { cachedLicense = info }`. Esta es una race condition: una llamada concurrente a `validate()` que tiene el lock y escribe `cachedLicense` puede competir con `clearCache()` escribiendo null sin el lock, llevando a estado inconsistente.

### Crítica
Error de concurrencia que puede causar estado corrupto del cache de licencias. En escenarios de alta concurrencia, la licencia puede ser invalidada incorrectamente o persistir cuando debería estar limpia.

### Corrección
```kotlin
override suspend fun clearCache() {
    cacheMutex.withLock {
        cachedLicense = null
    }
}

// O usar AtomicReference si no se quiere suspend:
private val cachedLicense = AtomicReference<LicenseInfo?>(null)

override fun clearCache() {
    cachedLicense.set(null)
}
```

---

## HIGH-009: Race Condition en refresh()

### Ubicación
`app/src/main/java/com/sponsorflow/nexus/account/LicenseVerifier.kt`  
Línea: `override suspend fun refresh()`

### Problema Detectado
`refresh()` lee `cachedLicense?.key` fuera de cualquier mutex lock, mientras que `validate()` escribe `cachedLicense` dentro de `cacheMutex.withLock`. Esta es una lectura no sincronizada de una variable mutable compartida, lo que puede resultar en leer un valor stale o parcialmente escrito bajo acceso concurrente.

### Crítica
Error de concurrencia que puede causar lecturas inconsistentes de la licencia cacheada. Puede llevar a usar una licencia inválida o expirada cuando no debería.

### Corrección
```kotlin
override suspend fun refresh(): AppResult<LicenseInfo> {
    val key = cacheMutex.withLock {
        cachedLicense?.key
    } ?: return AppResult.Error(AppError.LicenseError("No hay licencia"))
    return validate(key)
}
```

---

## HIGH-010: Reuso de Nonces Expirados Permite Replay Attacks

### Ubicación
`app/src/main/java/com/sponsorflow/nexus/account/NonceGenerator.kt`  
Línea: `fun consume(nonce: String): Boolean`

### Problema Detectado
Cuando un nonce es encontrado expirado dentro de `consume()`, el código lo reemplaza con un nuevo timestamp via `usedNonces[nonce] = ...` y devuelve `true`. Esto permite replay attacks: un atacante que captura un nonce viejo (expirado) puede reusarlo y será aceptado, derrotando el propósito anti-replay del mecanismo de nonce.

### Crítica
Vulnerabilidad de seguridad que permite ataques de replay. El mecanismo de nonce está diseñado para prevenir que la misma solicitud sea procesada múltiples veces, pero esta implementación permite exactamente eso.

### Corrección
```kotlin
fun consume(nonce: String): Boolean {
    val now = System.currentTimeMillis()
    val timestamp = usedNonces[nonce]
    
    // Si el nonce ya existe, rechazar SIEMPRE
    if (timestamp != null) {
        // No permitir reuso, incluso si expiró
        return false
    }
    
    // Nonce nuevo, almacenar
    usedNonces[nonce] = now
    return true
}

fun cleanupExpiredNonces() {
    val now = System.currentTimeMillis()
    val expiryMs = 5 * 60 * 1000L // 5 minutos
    usedNonces.entries.removeIf { now - it.value > expiryMs }
}
```

---

## HIGH-011: Declaraciones JNI external Potencialmente Incorrectas

### Ubicación
`app/src/main/java/com/sponsorflow/nexus/ai/LlamaBridge.kt`  
Línea: external fun declarations (~líneas 13-20)

### Problema Detectado
Las funciones `external` `loadModelNative`, `runInferenceNative`, `unloadModelNative`, y `getTokenCountNative` son declaradas como métodos de instancia de `LlamaBridge`. Los nombres de métodos nativos JNI deben coincidir con el nombre de clase fully-qualified. Si el lado Rust/C++ los implementa como `Java_com_sponsorflow_nexus_ai_LlamaBridge_loadModelNative` etc., esto es correcto. Pero el keyword `external` en métodos de instancia requiere que el lado Rust/C reciba un `jobject` como segundo parámetro (después de JNIEnv). Si el lado nativo está implementado esperando una firma de método static/companion, esto es un mismatch de JNI causando `UnsatisfiedLinkError` en runtime.

### Crítica
Error potencial de JNI que causará crash en runtime. La firma de métodos nativos debe coincidir exactamente entre Kotlin y el código nativo.

### Corrección
Asegurar que las implementaciones nativas (Rust/C++) coincidan con la firma JNI exacta para métodos de instancia:
```rust
// En Rust:
#[no_mangle]
pub extern "system" fn Java_com_sponsorflow_nexus_ai_LlamaBridge_loadModelNative(
    mut env: JNIEnv,
    _this: JObject,  // jobject para instancia, no jclass
    model_path: JString
) -> jboolean {
    // implementación
}
```

---

## Resumen - Parte 1

| ID | Archivo | Severidad | Estado |
|----|---------|-----------|--------|
| HIGH-001 | AndroidManifest.xml | Runtime | Pendiente |
| HIGH-002 | config.json | Runtime | Pendiente |
| HIGH-003 | NexusApplication.kt | Runtime | Pendiente |
| HIGH-004 | ClickLock.kt | Runtime | Pendiente |
| HIGH-005 | DatabaseModule.kt | Compilación | Pendiente |
| HIGH-006 | DynamicConfigManager.kt | Compilación | Pendiente |
| HIGH-007 | IntegrityChecker.kt | Compilación | Pendiente |
| HIGH-008 | LicenseVerifier.kt | Concurrency | Pendiente |
| HIGH-009 | LicenseVerifier.kt | Concurrency | Pendiente |
| HIGH-010 | NonceGenerator.kt | Seguridad | Pendiente |
| HIGH-011 | LlamaBridge.kt | Runtime | Pendiente |

---

*Continúa en Correcciones-HIGH-Parte2.md*