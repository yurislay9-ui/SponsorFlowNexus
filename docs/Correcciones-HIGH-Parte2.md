# SponsorFlowNexus - Correcciones HIGH (Parte 2/2)

## Documento de Críticas y Correcciones
**Fecha:** 2026-02-24  
**Proyecto:** SponsorFlowNexus  
**Errores HIGH:** H12 - H21 (10 errores)

---

## HIGH-012: Error en Validación de Magic Bytes GGUF

### Ubicación
`app/src/main/java/com/sponsorflow/nexus/ai/ModelValidator.kt`  
Línea: `isValidGGUF()`, return inside use lambda, ~línea 36

### Problema Detectado
```kotlin
val magic = Integer.reverseBytes(raf.readInt())
if (magic != GGUF_MAGIC_LE) return false
```
Los magic bytes GGUF son `0x47 0x47 0x55 0x46` ('G','G','U','F'). Leer 4 bytes como big-endian int produce `0x47475546`. `Integer.reverseBytes(0x47475546)` da `0x46554747`. Pero `GGUF_MAGIC_LE` está definido como `0x47475546`. Entonces `magic` (después de reverseBytes) `0x46554747` se compara contra `GGUF_MAGIC_LE = 0x47475546` — NUNCA coincidirán. La reversión de bytes se aplica cuando no debería, causando que todos los archivos GGUF válidos sean rechazados.

### Crítica
Error de lógica que causa rechazo de todos los modelos GGUF válidos. La validación de modelos es crítica para la funcionalidad de IA de la aplicación.

### Corrección
```kotlin
fun isValidGGUF(file: File): Boolean {
    return try {
        RandomAccessFile(file, "r").use { raf ->
            // CORREGIDO: No revertir bytes, leer directamente
            val magic = raf.readInt()
            // GGUF magic en little-endian: 0x47475546
            if (magic != GGUF_MAGIC_LE) return false
            
            val version = Integer.reverseBytes(raf.readInt())
            if (version < 1) return false // Soportar todas las versiones
            
            true
        }
    } catch (e: Exception) {
        false
    }
}

companion object {
    const val GGUF_MAGIC_LE = 0x47475546 // "GGUF" en little-endian
}
```

---

## HIGH-013: Off-by-one en addTypo

### Ubicación
`app/src/main/java/com/sponsorflow/nexus/antidetection/HumanBehavior.kt`  
Línea: Línea 36 (addTypo function, chars[pos+1] access)

### Problema Detectado
`pos` es generado como `nextInt(1, text.length - 1)`, dando rango [1, text.length-2]. Entonces `chars[pos + 1]` accede índice hasta `text.length - 1`, que es válido. Sin embargo, cuando `text.length == 2`, `nextInt(1, 1)` es llamado con bound igual a origin, lo que arroja `IllegalArgumentException: bound must be greater than origin` en ThreadLocalRandom.

### Crítica
Error que causa crash para textos muy cortos. La función de simulación de comportamiento humano fallará para mensajes de 2 caracteres.

### Corrección
```kotlin
fun addTypo(text: String): String {
    if (text.length < 3) return text // CORREGIDO: Mínimo 3 caracteres
    
    val chars = text.toCharArray()
    val pos = ThreadLocalRandom.current().nextInt(1, text.length - 1)
    
    // Intercambiar caracteres
    val temp = chars[pos]
    chars[pos] = chars[pos + 1]
    chars[pos + 1] = temp
    
    return String(chars)
}
```

---

## HIGH-014: Uso de Switch Deprecated

### Ubicación
`app/src/main/java/com/sponsorflow/nexus/ui/settings/SettingsFragment.kt`  
Línea: import android.widget.Switch y lateinit var autoReplySwitch: Switch

### Problema Detectado
Usando `android.widget.Switch` que está deprecated y reemplazado por `SwitchCompat` o `MaterialSwitch`. Si el layout usa `androidx.appcompat.widget.SwitchCompat` o `com.google.android.material.switchmaterial.SwitchMaterial`, `findViewById<Switch>()` devolverá null causando NullPointerException crash cuando se acceda.

### Crítica
Error de compatibilidad que puede causar crash. El uso de widgets deprecated también genera advertencias de lint y puede causar inconsistencias visuales en diferentes niveles de API.

### Corrección
```kotlin
import androidx.appcompat.widget.SwitchCompat

class SettingsFragment : PreferenceFragmentCompat() {
    private lateinit var autoReplySwitch: SwitchCompat
    private lateinit var notificationsSwitch: SwitchCompat
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        autoReplySwitch = view.findViewById(R.id.switch_auto_reply)
        notificationsSwitch = view.findViewById(R.id.switch_notifications)
    }
}
```

---

## HIGH-015: AdminCommand Duplicado

### Ubicación
`app/src/main/java/com/sponsorflow/nexus/config/DynamicConfigManager.kt`  
Línea: AdminCommand data class definition at bottom of file

### Problema Detectado
Un `data class AdminCommand` es declarado en este archivo, pero `AdminCommand` ya está declarado en `com.sponsorflow.nexus.admin.AdminTypes.kt` (paquete diferente). Dentro de `DynamicConfigManager.kt` la clase `AdminCommandsConfig` referencia `AdminCommand` — esta definición local hace shadow a la del admin y crea dos tipos incompatibles separados, lo que causará errores de type mismatch si ambos son usados juntos.

### Crítica
Error de diseño que crea ambigüedad y posibles errores de tipo. La duplicación de clases con el mismo nombre en diferentes paquetes es una mala práctica que puede causar confusión y errores sutiles.

### Corrección
```kotlin
// Opción A: Remover la clase duplicada y usar import
// En DynamicConfigManager.kt:
import com.sponsorflow.nexus.admin.AdminCommand

// Opción B: Renombrar la clase local
data class RemoteAdminCommand(
    val command: String,
    val params: Map<String, String>
)
```

---

## HIGH-016: TransactionTooLargeException Potencial

### Ubicación
`app/src/main/java/com/sponsorflow/nexus/cache/ConversationCache.kt`  
Línea: `saveEnterpriseCache()`, comment and code

### Problema Detectado
El comentario dice `// Sin límite para ENTERPRISE` y las entradas se guardan sin ningún límite. Combinado con el backend SharedPreferences, el crecimiento de string sin límite a través de muchos mensajes causará `TransactionTooLargeException` crashes cuando el string serializado exceda el límite de transacción Binder (~1 MB) en `prefs.edit().putString(...)`.

### Crítica
Error que causará crash en producción después de acumular suficientes mensajes. El límite de Binder es una restricción fundamental de Android que no puede ignorarse.

### Corrección
```kotlin
private fun saveEnterpriseCache(phone: String, message: String, response: String) {
    val key = "ent_$phone"
    val existing = prefs.getString(key, "") ?: ""
    val entries = if (existing.isNotEmpty()) existing.split("||").toMutableList() else mutableListOf<String>()
    
    entries.add("$message|$response|${System.currentTimeMillis()}")
    
    // CORREGIDO: Aplicar límite MAX_ENTERPRISE_MESSAGES
    val trimmed = entries.takeLast(MAX_ENTERPRISE_MESSAGES)
    prefs.edit().putString(key, trimmed.joinToString("||")).apply()
    
    updateMemory(phone, message)
    savePersistentMemory(phone)
}
```

---

## HIGH-017: Parsing Incorrecto de Transacción TRC20

### Ubicación
`app/src/main/rust/src/payment/validate.rs`  
Línea: Líneas 50-53, extract_amount function

### Problema Detectado
El selector de función TRC20 `transfer(address,uint256)` es `a9059cbb`. Sin embargo, el comentario dice `Transfer(address,address,uint256)` que es incorrecto - TRC20 transfer solo tiene un argumento address. Más críticamente, la extracción del monto es incorrecta: en calldata TRC20 (4-byte selector + 32-byte address + 32-byte uint256), el uint256 ocupa bytes 36..68. El código lee bytes [60..68] como u64 desde big-endian, pero el valor uint256 tiene 32 bytes (bytes 36..68). Leer solo los últimos 8 bytes del campo uint256 de 32 bytes ([60..68]) da los 8 bytes bajos correctos solo si el monto cabe en u64. Pero el bug real es `data[0..4] != selector` donde `data` son los bytes crudos de la transacción, no calldata ABI-encoded — el selector es poco probable que aparezca en offset 0 en una transacción TRON raw, haciendo que esta validación siempre devuelva None para transacciones reales. Este es un bug de lógica HIGH-severity que causa que la validación de pagos siempre falle.

### Crítica
Error crítico en la validación de pagos TRC20. Los pagos nunca serán validados correctamente, causando que las transacciones legítimas sean rechazadas.

### Corrección
```rust
pub fn extract_amount(data: &[u8]) -> Option<u64> {
    // CORREGIDO: Parsear transacción protobuf de TRON para localizar el calldata
    // El selector debe buscarse en el campo de datos del contrato, no al inicio
    
    // TRC20 transfer selector: a9059cbb
    let selector: [u8; 4] = [0xa9, 0x05, 0x9c, 0xbb];
    
    // Buscar el selector en los datos
    let selector_pos = data.windows(4)
        .position(|w| w == selector)?;
    
    // El monto empieza 36 bytes después del selector (4 selector + 32 address)
    let amount_start = selector_pos + 36;
    let amount_end = amount_start + 32;
    
    if data.len() < amount_end {
        return None;
    }
    
    // Leer los últimos 8 bytes del uint256 (asumiendo que el monto cabe en u64)
    let amount_bytes: [u8; 8] = data[amount_end - 8..amount_end]
        .try_into().ok()?;
    
    Some(u64::from_be_bytes(amount_bytes))
}
```

---

## HIGH-018: unwrap() que Puede Panic en JNI

### Ubicación
`app/src/main/rust/src/crypto/kdf.rs`  
Línea: Línea 16, derive_key function

### Problema Detectado
`Params::new(65536, 3, 4, Some(32)).unwrap()` y `argon2.hash_password_into(...).unwrap()` usan `unwrap()` que hará panic si la construcción de parámetros o la derivación de clave falla. Aunque los parámetros están hardcodeados y es poco probable que fallen, `hash_password_into` puede fallar si la longitud de salida no coincide con la longitud de salida de Params o por fallo de asignación de memoria. En un contexto JNI, un panic de Rust a través del límite FFI es undefined behavior y causará crash del proceso Android.

### Crítica
Error que puede causar crash del proceso Android. Los panics en Rust a través de FFI son undefined behavior y pueden corromper el estado de la JVM.

### Corrección
```rust
use jni::errors::Result as JniResult;

pub fn derive_key(password: &[u8], salt: &[u8]) -> Result<[u8; 32], DeriveError> {
    let params = Params::new(65536, 3, 4, Some(32))
        .map_err(|e| DeriveError::InvalidParams(e.to_string()))?;
    
    let mut output = [0u8; 32];
    let argon2 = Argon2::new(Algorithm::Argon2id, Version::V0x13, params);
    
    argon2.hash_password_into(password, salt, &mut output)
        .map_err(|e| DeriveError::HashFailed(e.to_string()))?;
    
    Ok(output)
}

#[derive(Debug)]
pub enum DeriveError {
    InvalidParams(String),
    HashFailed(String),
}
```

---

## HIGH-019: unwrap() en Función JNI basic.rs

### Ubicación
`app/src/main/rust/src/jni/basic.rs`  
Línea: Líneas 17-20, getRustVersion JNI function

### Problema Detectado
`env.new_string(VERSION).unwrap().into_raw()` llama `unwrap()` directamente en una función JNI. Si la creación del string falla (OOM), esto hace panic a través del límite FFI, que es undefined behavior en JNI y causará crash del proceso Android.

### Crítica
Error que puede causar crash del proceso Android en condiciones de baja memoria. El panic cruzará el límite FFI causando comportamiento indefinido.

### Corrección
```rust
#[no_mangle]
pub extern "system" fn Java_com_sponsorflow_nexus_rust_RustBridge_getRustVersion(
    env: JNIEnv, _class: JClass) -> jstring {
    // CORREGIDO: Manejar error sin unwrap
    match env.new_string(VERSION) {
        Ok(s) => s.into_raw(),
        Err(_) => std::ptr::null_mut(),
    }
}
```

---

## HIGH-020: unwrap() en Función JNI crypto.rs

### Ubicación
`app/src/main/rust/src/jni/crypto.rs`  
Línea: Líneas 35-38, generateAesKey y blake3Hash

### Problema Detectado
`env.byte_array_from_slice(&key).unwrap().into_raw()` y `env.byte_array_from_slice(&hash).unwrap().into_raw()` usan `unwrap()` en funciones JNI. Un panic en una función JNI es undefined behavior y causa crash del proceso Android.

### Crítica
Error que puede causar crash del proceso Android. Similar a HIGH-019, el panic atraviesa el límite FFI.

### Corrección
```rust
#[no_mangle]
pub extern "system" fn Java_com_sponsorflow_nexus_rust_RustBridge_generateAesKey(
    mut env: JNIEnv, _class: JClass) -> jbyteArray {
    let key = generate_key();
    // CORREGIDO: Manejar error sin unwrap
    match env.byte_array_from_slice(&key) {
        Ok(arr) => arr.into_raw(),
        Err(_) => std::ptr::null_mut(),
    }
}

#[no_mangle]
pub extern "system" fn Java_com_sponsorflow_nexus_rust_RustBridge_blake3Hash(
    mut env: JNIEnv, _class: JClass, data: JByteArray) -> jbyteArray {
    let d = env.convert_byte_array(&data).unwrap_or_default();
    let hash = crate::crypto::blake3(&d);
    // CORREGIDO: Manejar error sin unwrap
    match env.byte_array_from_slice(&hash) {
        Ok(arr) => arr.into_raw(),
        Err(_) => std::ptr::null_mut(),
    }
}
```

---

## HIGH-021: unwrap() en Función JNI payment.rs

### Ubicación
`app/src/main/rust/src/jni/payment.rs`  
Línea: Líneas 22-25, generatePaymentQr

### Problema Detectado
`env.get_string(&wallet).unwrap()` y `env.new_string(&qr).unwrap().into_raw()` usan `unwrap()` en una función JNI. Si `wallet` es un JString null o la creación del string falla, esto hace panic a través de FFI — undefined behavior que causa crash del proceso Android.

### Crítica
Error que puede causar crash del proceso Android. Similar a HIGH-019 y HIGH-020, representa un riesgo de estabilidad.

### Corrección
```rust
#[no_mangle]
pub extern "system" fn Java_com_sponsorflow_nexus_rust_RustBridge_generatePaymentQr(
    mut env: JNIEnv, _class: JClass, wallet: JString, amount: jlong) -> jstring {
    // CORREGIDO: Manejar error sin unwrap
    let wallet_str = match env.get_string(&wallet) {
        Ok(s) => s.into(),
        Err(_) => return std::ptr::null_mut(),
    };
    
    let qr = match generate_qr(&wallet_str, amount) {
        Ok(q) => q,
        Err(_) => return std::ptr::null_mut(),
    };
    
    match env.new_string(&qr) {
        Ok(s) => s.into_raw(),
        Err(_) => std::ptr::null_mut(),
    }
}
```

---

## Resumen - Parte 2

| ID | Archivo | Severidad | Estado |
|----|---------|-----------|--------|
| HIGH-012 | ModelValidator.kt | Funcionalidad | ✅ Ya corregido (reverseBytes) |
| HIGH-013 | HumanBehavior.kt | Crash | ✅ CORREGIDO |
| HIGH-014 | SettingsFragment.kt | Crash/Deprecación | ✅ CORREGIDO |
| HIGH-015 | DynamicConfigManager.kt | Conflicto de tipos | ✅ CORREGIDO |
| HIGH-016 | ConversationCache.kt | Crash potencial | ✅ CORREGIDO |
| HIGH-017 | validate.rs (Rust) | Funcionalidad | ✅ CORREGIDO |
| HIGH-018 | kdf.rs (Rust) | Crash/Seguridad | ✅ CORREGIDO |
| HIGH-019 | basic.rs (Rust) | Crash | ✅ CORREGIDO |
| HIGH-020 | crypto.rs (Rust) | Crash | ✅ CORREGIDO (en CRITICAL) |
| HIGH-021 | payment.rs (Rust) | Crash | ✅ CORREGIDO |

---

## Patrones Identificados

### 1. Errores JNI (H18-H21)
Todos los archivos JNI en Rust usan `unwrap()` incorrectamente, lo que causa undefined behavior al cruzar el límite FFI.

### 2. Errores de Validación (H12, H17)
La validación de modelos GGUF y transacciones TRC20 tiene errores de lógica que causan falsos negativos.

### 3. Errores de Límites (H13, H16)
Casos edge no manejados que causan crashes o excepciones.

### 4. Errores de Compatibilidad (H14, H15)
Uso de APIs deprecated o duplicación de clases que causan problemas de compilación o runtime.

---

*Continuación de Correcciones-HIGH-Parte1.md*