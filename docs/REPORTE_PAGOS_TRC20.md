# 💳 REPORTE DE PRUEBAS - PAGOS TRC20
## SponsorFlow Nexus v2.3

---

## 📊 RESUMEN DE VULNERABILIDADES

| Prueba | Resultado | Severidad |
|--------|-----------|-----------|
| Monto Incorrecto | ❌ VULNERABLE | ALTA |
| Timeout Confirmación | ⚠️ PARCIAL | MEDIA |
| Double Spend | ❌ VULNERABLE | ALTA |

---

## 🔴 PRUEBA 1: TRANSACCIÓN CON MONTO INCORRECTO

### Hallazgos:
```kotlin
// TronScanVerifier.kt:52
val isCorrectAmount = tx.amount >= expected
// ⚠️ PROBLEMA: Acepta montos MAYORES, pero también menores por redondeo
```

### Escenario de ataque:
1. Usuario debe pagar 9.00 USDT (plan BÁSICO)
2. Usuario envía 0.000001 USDT
3. `tx.amount >= expected` → `0.000001 >= 9.0` → **FALSE** ❌

### Veredicto: ✅ CORRECTO
- La lógica `>=` rechaza montos menores
- Pero permite montos mayores (aceptable)

---

## 🟡 PRUEBA 2: TIMEOUT DE CONFIRMACIÓN

### Hallazgos:
```kotlin
// PaymentManager.kt:36
val timeout = 30 * 60 * 1000L // 30 minutos
while (System.currentTimeMillis() - startTime < timeout) {
    // espera...
}
```

### Problemas:
- ✅ Timeout de 30 minutos implementado
- ⚠️ No marca transacción como "Expirada"
- ⚠️ No permite reintentar limpiamente

### Veredicto: ⚠️ PARCIALMENTE CORRECTO
- Necesita estado "Expirado" claro

---

## 🔴 PRUEBA 3: DOUBLE SPEND

### Hallazgos:
```kotlin
// NO hay registro de tx_hash usados
// Cualquiera puede reusar el mismo hash
```

### Escenario de ataque:
1. Usuario A paga 9 USDT → obtiene licencia
2. Usuario A comparte tx_hash con Usuario B
3. Usuario B usa mismo tx_hash → obtiene licencia gratis
4. Repetir infinitamente

### Veredicto: ❌ VULNERABLE
- Falta registro de tx_hash gastados
- Vulnerabilidad crítica

---

## 📋 CORRECCIONES APLICADAS

### Alta Prioridad:
- [x] TxHashRegistry con EncryptedSharedPreferences ✅
- [x] Validación double spend ✅
- [x] Mensajes de error detallados ✅
- [x] Tolerancia de 0.01 USDT para monto ✅

### Implementado:
```kotlin
// TxHashRegistry.kt
fun isUsed(txHash: String): Boolean
fun markUsedWithTimestamp(txHash: String)
fun useIfNew(txHash: String): Boolean

// TronScanVerifier.kt
if (TxHashRegistry.isUsed(tx.hash)) {
    return PaymentVerification(error = "Transacción ya usada")
}
```

---

## ✅ LO QUE ESTÁ BIEN

1. ✅ Timeout de 30 minutos
2. ✅ 20 confirmaciones requeridas
3. ✅ Validación de wallet destino
4. ✅ Validación de token USDT