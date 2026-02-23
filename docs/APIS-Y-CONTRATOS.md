# APIs y Contratos - SponsorFlow Nexus

## 📋 Contratos del Sistema

Los contratos son interfaces que definen el comportamiento esperado de los componentes del sistema.

---

## 🤖 IAIEngine

Interface del motor de inteligencia artificial.

```kotlin
interface IAIEngine {
    suspend fun loadModel(modelPath: String): AppResult<Unit>
    suspend fun generateResponse(prompt: String, maxTokens: Int, temperature: Float): AppResult<String>
    fun isModelLoaded(): Boolean
    fun unloadModel()
}
```

**Implementaciones:**
- `AIEngine` - Motor principal
- `LlamaBridge` - Bridge para LLaMA
- `CloudAIProvider` - Proveedor en la nube

---

## 🔐 ILicenseValidator

Validador de licencias.

```kotlin
interface ILicenseValidator {
    suspend fun validate(licenseKey: String): AppResult<LicenseInfo>
    suspend fun refresh(): AppResult<LicenseInfo>
    fun getCachedLicenseInfo(): LicenseInfo?
    fun isGracePeriodActive(): Boolean
    fun getRemainingGraceDays(): Int
}
```

**Métodos:**
- `validate()` - Valida una clave de licencia
- `refresh()` - Refresca el estado de la licencia
- `getCachedLicenseInfo()` - Obtiene info cacheada
- `isGracePeriodActive()` - Verifica período de gracia
- `getRemainingGraceDays()` - Días restantes de gracia

---

## 🔒 IEncryptionService

Servicio de encriptación.

```kotlin
interface IEncryptionService {
    suspend fun encrypt(data: ByteArray): AppResult<ByteArray>
    suspend fun decrypt(data: ByteArray): AppResult<ByteArray>
    suspend fun encryptString(plaintext: String): AppResult<String>
    suspend fun decryptString(ciphertext: String): AppResult<String>
}
```

**Algoritmos soportados:**
- AES-256-GCM
- AES-256-CBC (legacy)

---

## 📦 IRepository

Repositorio genérico para acceso a datos.

```kotlin
interface IRepository<T, ID> {
    suspend fun getById(id: ID): AppResult<T>
    suspend fun getAll(): AppResult<List<T>>
    suspend fun insert(item: T): AppResult<Unit>
    suspend fun update(item: T): AppResult<Unit>
    suspend fun delete(id: ID): AppResult<Unit>
}
```

**Implementaciones:**
- `ContactRepository`
- `ConversationRepository`
- `ProductRepository`
- `SubscriptionRepository`

---

## 🔌 IPluginContract

Contrato para plugins.

```kotlin
interface IPluginContract {
    val id: String
    val name: String
    val version: String
    val requiredTier: SubscriptionTier
    
    suspend fun execute(input: Map<String, Any>): PluginResult
    fun isEnabled(): Boolean
}
```

**Métodos disponibles para plugins:**
- `context.getString()` - Obtener string del contexto
- `context.get()` - Obtener dato del contexto
- `success()` - Retornar éxito
- `error()` - Retornar error

---

## 📊 AppResult

Tipo resultado para operaciones.

```kotlin
sealed class AppResult<T> {
    data class Success<T>(val data: T) : AppResult<T>()
    data class Error<T>(val error: AppError, val message: String?) : AppResult<Nothing>()
    data object Loading : AppResult<Nothing>()
    
    fun onSuccess(action: (T) -> Unit): AppResult<T>
    fun onError(action: (AppError, String?) -> Unit): AppResult<T>
    fun isLoading(): Boolean
    fun isError(): Boolean
    fun isSuccess(): Boolean
}
```

---

## ⚠️ AppError

Errores de la aplicación.

```kotlin
sealed class AppError {
    data class NetworkError(val code: Int?, val message: String?) : AppError()
    data class DatabaseError(val cause: Throwable?) : AppError()
    data class LicenseError(val reason: String) : AppError()
    data class AIError(val reason: String) : AppError()
    data class PluginError(val pluginId: String, val reason: String) : AppError()
    data class SecurityError(val reason: String) : AppError()
    data class ValidationError(val field: String, val reason: String) : AppError()
    data class PaymentError(val reason: String) : AppError()
    data class UnknownError(val cause: Throwable?) : AppError()
    
    fun toUserMessage(): String
    
    companion object {
        fun fromException(t: Throwable): AppError
    }
}
```

---

## 🎯 SubscriptionTier

Niveles de suscripción.

```kotlin
enum class SubscriptionTier(
    val hasCustomPrompt: Boolean,
    val hasInventory: Boolean,
    val hasMemory: Boolean,
    val memoryLimit: Int,
    val hasPlugins: Boolean,
    val hasPluginSDK: Boolean,
    val hasCategories: Boolean,
    val price: Double,
    val displayName: String
) {
    FREE(hasCustomPrompt = false, hasInventory = false, hasMemory = false, memoryLimit = 0, hasPlugins = false, hasPluginSDK = false, hasCategories = false, price = 0.0, displayName = "Gratis"),
    OBSERVADOR(hasCustomPrompt = true, hasInventory = true, hasMemory = true, memoryLimit = 5, hasPlugins = true, hasPluginSDK = false, hasCategories = false, price = 9.0, displayName = "Observador"),
    DESARROLLO(hasCustomPrompt = true, hasInventory = true, hasMemory = true, memoryLimit = 20, hasPlugins = true, hasPluginSDK = false, hasCategories = true, price = 19.0, displayName = "Desarrollo"),
    EMPRESARIO(hasCustomPrompt = true, hasInventory = true, hasMemory = true, memoryLimit = Int.MAX_VALUE, hasPlugins = true, hasPluginSDK = true, hasCategories = true, price = 29.0, displayName = "Empresario");
    
    fun isAtLeast(other: SubscriptionTier): Boolean
    
    companion object {
        fun fromName(name: String): SubscriptionTier
    }
}
```

---

## 📱 PluginResult

Resultado de ejecución de plugin.

```kotlin
data class PluginResult(
    val success: Boolean,
    val data: Map<String, Any> = emptyMap(),
    val error: String? = null
)
```

---

## 📝 ModelInfo

Información del modelo de IA.

```kotlin
data class ModelInfo(
    val name: String,
    val path: String,
    val size: Long,
    val contextSize: Int,
    val quantization: String
)
```

---

## 📄 LicenseInfo

Información de licencia.

```kotlin
data class LicenseInfo(
    val key: String,
    val tier: SubscriptionTier,
    val expiresAt: Long,
    val deviceId: String,
    val isValid: Boolean
)
```

---

## 💳 PaymentVerification

Resultado de verificación de pago.

```kotlin
data class PaymentVerification(
    val isValid: Boolean,
    val amount: Double,
    val timestamp: Long,
    val confirmations: Int,
    val error: String? = null
)
```

---

## 🔄 Transaction Info

Información de transacción Tron.

```kotlin
data class TransactionInfo(
    val hash: String,
    val from: String,
    val to: String,
    val amount: Double,
    val token: String,
    val confirmations: Int,
    val timestamp: Long
)
```

---

## 🔐 IntegrityReport

Reporte de verificación de integridad.

```kotlin
data class IntegrityReport(
    val signatureValid: Boolean,
    val installerValid: Boolean,
    val isRooted: Boolean,
    val isEmulator: Boolean
) {
    val passedAll: Boolean
}
```

---

## 📊 AnalyticsData

Datos de análisis.

```kotlin
data class AnalyticsData(
    val messagesToday: Int = 0,
    val messagesTotal: Long = 0,
    val topClients: List<ClientStats> = emptyList(),
    val peakHours: List<HourStats> = emptyList(),
    val avgResponseTime: Long = 0
)

data class ClientStats(
    val phone: String,
    val messageCount: Int,
    val lastMessageTime: Long
)

data class HourStats(
    val hour: Int,
    val messageCount: Int
)
```

---

## 🧠 MemoryConfig

Configuración de memoria para IA.

```kotlin
data class MemoryConfig(
    val contextSize: Int,
    val threads: Int,
    val batchSize: Int
)
```

---

## 🔧 RemoteConfig

Configuración remota.

```kotlin
data class RemoteConfig(
    val version: String = "",
    val min_version: String = "",
    val update_url: String = "",
    val force_update: Boolean = false,
    val license_url: String = "",
    val webhook_url: String = "",
    val heartbeat_url: String = "",
    val error_url: String = ""
)
```

---

## 📡 Endpoints API

### Licencias
```
GET  /api/license/validate?key={key}&device={deviceId}
POST /api/license/activate
POST /api/license/transfer
```

### Configuración
```
GET /api/config
```

### Pagos
```
POST /api/payment/verify
```

### Webhooks
```
POST {webhook_url}
```

---

## 🔒 Encriptación de Datos

### Datos en Reposo
- AES-256-GCM
- MasterKey con Android Keystore
- EncryptedSharedPreferences

### Datos en Tránsito
- HTTPS obligatorio
- Certificate pinning
- Token authentication

---

**Última actualización:** Febrero 2026