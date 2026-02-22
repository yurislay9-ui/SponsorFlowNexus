# SponsorFlow Nexus v2.3 - Configuración del Administrador
## Datos Requeridos para Funcionamiento Completo

---

## 🔑 SECCIÓN 1: GOOGLE SIGN-IN

### Archivo: `GoogleSignInManager.kt`
**Línea:** `private const val WEB_CLIENT_ID = "YOUR_WEB_CLIENT_ID"`

**Qué proporcionar:**
- Client ID de OAuth 2.0 de Google Cloud Console

**Cómo obtenerlo:**
1. Ir a https://console.cloud.google.com
2. Crear proyecto o seleccionar existente
3. APIs & Services → Credentials → Create Credentials → OAuth client ID
4. Seleccionar "Web application"
5. Copiar el Client ID

**Formato esperado:**
```
private const val WEB_CLIENT_ID = "123456789-abcdefg.apps.googleusercontent.com"
```

---

## 💰 SECCIÓN 2: PAGOS USDT TRC20

### Archivo: `PaymentManager.kt`
**Línea:** `private const val WALLET_ADDRESS = "YOUR_WALLET_ADDRESS"`

**Qué proporcionar:**
- Tu dirección de wallet TRON (TRC20) para recibir pagos

**Cómo obtenerla:**
1. Instalar wallet TRON (TronLink, Trust Wallet, etc.)
2. Copiar tu dirección pública TRON

**Formato esperado:**
```
private const val WALLET_ADDRESS = "TYourTronWalletAddressHere123456789"
```

### Archivo: `TronScanVerifier.kt`
**API de TronScan:**
- URL base: `https://apilist.tronscanapi.com/api`
- No requiere API key para uso básico
- Para uso intensivo, obtener API key en: https://tronscan.org

**Contrato USDT TRC20:**
```
const val USDT_CONTRACT = "TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t"
```
(Este es el contrato oficial, NO cambiar)

---

## 🖥️ SECCIÓN 3: SERVIDOR BACKEND

### Archivo: `LicenseVerifier.kt`
**Línea:** `private const val SERVER_URL = "https://api.sponsorflow.com"`

**Qué proporcionar:**
- URL de tu servidor de licencias

**Endpoints requeridos:**
```
POST /api/license/validate
- Body: {"licenseKey": "...", "deviceId": "...", "appVersion": "..."}
- Response: {"key": "...", "tier": "...", "expiresAt": ..., "isActive": true}
```

### Archivo: `DynamicConfigManager.kt`
**Línea:** `private const val CONFIG_URL = "https://config.sponsorflow.com"`

**Qué proporcionar:**
- URL donde está alojado el archivo config.json

**Formato config.json:**
```json
{
  "serverUrl": "https://api.sponsorflow.com",
  "minAppVersion": "2.3.0",
  "maintenanceMode": false,
  "announcement": null
}
```

---

## 🤖 SECCIÓN 4: MODELOS IA (LLAMA.CPP)

### Archivo: `ResourceDownloadManager.kt`

**Modelos recomendados:**
| Modelo | Tamaño | Uso | Link |
|--------|--------|-----|------|
| Phi-3 Mini | ~2GB | Respuestas rápidas | https://huggingface.co/microsoft/Phi-3-mini-4k-instruct-gguf |
| Llama 3.2 1B | ~1GB | Ligero | https://huggingface.co/bartowski/Llama-3.2-1B-Instruct-GGUF |
| Mistral 7B | ~4GB | Mayor calidad | https://huggingface.co/TheBloke/Mistral-7B-Instruct-v0.2-GGUF |

**Dónde alojar los modelos:**
- Tu propio servidor (recomendado)
- GitHub Releases (máx 2GB por archivo)
- Hugging Face Hub
- AWS S3 / Google Cloud Storage

**Formato URL:**
```
https://tu-servidor.com/models/phi-3-mini.gguf
```

---

## 🔐 SECCIÓN 5: SEGURIDAD

### Archivo: `IntegrityChecker.kt`
**Línea:** `private const val EXPECTED_SIGNATURE = "YOUR_APP_SIGNATURE"`

**Cómo obtener la firma:**
```bash
# Debug
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android

# Release
keytool -list -v -keystore tu-keystore.jks -alias tu-alias
```

**Formato esperado (SHA-256):**
```
private const val EXPECTED_SIGNATURE = "AB:CD:EF:12:34:56:78:90:AB:CD:EF:12:34:56:78:90:AB:CD:EF:12:34:56:78:90:AB:CD:EF:12:34:56:78:90"
```

---

## 📱 SECCIÓN 6: FIREBASE (OPCIONAL)

### Archivo: `google-services.json`
**Ubicación:** `app/google-services.json`

**Cómo obtenerlo:**
1. Ir a https://console.firebase.google.com
2. Crear proyecto
3. Añadir app Android (com.sponsorflow.nexus)
4. Descargar google-services.json

---

## 🔄 SECCIÓN 7: ACTUALIZACIONES DE APP (DOS GITHUB)

### GitHub 1 - Actualización por cambio de WhatsApp/Meta
**Propósito:** Cuando Meta actualiza WhatsApp y rompe la app, aquí subes la versión corregida

**Archivo: `DynamicConfigManager.kt`**
```kotlin
// Actualización por cambios de WhatsApp
const val GITHUB_WHATSAPP_FIX_URL = "https://api.github.com/repos/TU_USUARIO/nexus-whatsapp-fix/releases/latest"
const val GITHUB_WHATSAPP_DOWNLOAD = "https://github.com/TU_USUARIO/nexus-whatsapp-fix/releases/download/"
```

**Qué proporcionar:**
- Usuario de GitHub
- Nombre del repositorio para fixes de WhatsApp

**Ejemplo:**
```
https://api.github.com/repos/juanperez/nexus-whatsapp-fix/releases/latest
https://github.com/juanperez/nexus-whatsapp-fix/releases/download/v2.3.1/nexus-fix.apk
```

---

### GitHub 2 - Actualización de tu App (código propio)
**Propósito:** Cuando tú quieras actualizar tu app con nuevas funciones

**Archivo: `DynamicConfigManager.kt`**
```kotlin
// Actualización de tu app
const val GITHUB_APP_UPDATE_URL = "https://api.github.com/repos/TU_USUARIO/nexus/releases/latest"
const val GITHUB_APP_DOWNLOAD = "https://github.com/TU_USUARIO/nexus/releases/download/"
```

**Qué proporcionar:**
- Usuario de GitHub
- Nombre del repositorio principal

**Ejemplo:**
```
https://api.github.com/repos/juanperez/nexus/releases/latest
https://github.com/juanperez/nexus/releases/download/v2.4.0/nexus.apk
```

---

## 🌐 SECCIÓN 8: N8N + NGROK (WEBHOOKS)

### Archivo: `DynamicConfigManager.kt`
**Añadir URLs de n8n:**

```kotlin
// N8N Webhooks
const val N8N_WEBHOOK_URL = "https://TU-IP-NGROK.ngrok-free.app/webhook/nexus"
const val N8N_WEBHOOK_LICENSE = "https://TU-IP-NGROK.ngrok-free.app/webhook/license"
const val N8N_WEBHOOK_PAYMENT = "https://TU-IP-NGROK.ngrok-free.app/webhook/payment"
```

### Configuración de Ngrok:
**Qué proporcionar:**
- URL de Ngrok (cambia cada vez que reinicias Ngrok)

**Cómo obtenerla:**
```bash
# Instalar ngrok
# Ejecutar:
ngrok http 5678

# Copiar la URL que aparece, ejemplo:
# https://abc123-45-67-89.ngrok-free.app
```

### Configuración de N8N:
**Puerto por defecto:** 5678

**Webhooks a crear en n8n:**
| Webhook | Uso |
|---------|-----|
| `/webhook/nexus` | Eventos generales |
| `/webhook/license` | Validar licencias |
| `/webhook/payment` | Confirmar pagos |

**Ejemplo completo:**
```
https://abc123-45-67-89.ngrok-free.app/webhook/nexus
https://abc123-45-67-89.ngrok-free.app/webhook/license
https://abc123-45-67-89.ngrok-free.app/webhook/payment
```

**Alternativa con IP fija:**
```
http://TU-IP-PUBLICA:5678/webhook/nexus
http://123.45.67.89:5678/webhook/license
```

---

## 📱 SECCIÓN 8: DETECCIÓN DE ACTUALIZACIONES DE WHATSAPP

### Archivo: `OperationStatus.kt`
Ya incluye el estado `SUSPENDED_WHATSAPP_UPDATE`

### Configuración en config.json (DynamicConfigManager):
```json
{
  "serverUrl": "https://api.sponsorflow.com",
  "minAppVersion": "2.3.0",
  "maintenanceMode": false,
  "whatsappMinVersion": "2.24.1",
  "whatsappUpdateRequired": false,
  "downloadUrl": "https://github.com/TU_USUARIO/nexus/releases/latest"
}
```

**Endpoint para verificar versión WhatsApp:**
```
GET /api/whatsapp/version
Response: {"minVersion": "2.24.1", "updateRequired": false}
```

---

## 📋 RESUMEN COMPLETO DE VALORES A PROPORCIONAR

| Archivo | Constante | Propósito | Ejemplo |
|---------|-----------|-----------|---------|
| GoogleSignInManager.kt | WEB_CLIENT_ID | Login Google | `123456.apps.googleusercontent.com` |
| PaymentManager.kt | WALLET_ADDRESS | Recibir pagos USDT | `TABC123...` |
| LicenseVerifier.kt | SERVER_URL | Backend licencias | `https://api.tudominio.com` |
| DynamicConfigManager.kt | CONFIG_URL | Archivo config.json | `https://config.tudominio.com` |
| IntegrityChecker.kt | EXPECTED_SIGNATURE | Seguridad app | `AB:CD:EF...` |
| DynamicConfigManager.kt | GITHUB_WHATSAPP_FIX_URL | Fix por cambio WhatsApp | `https://api.github.com/repos/USER/nexus-fix/...` |
| DynamicConfigManager.kt | GITHUB_APP_UPDATE_URL | Actualizar tu app | `https://api.github.com/repos/USER/nexus/...` |
| DynamicConfigManager.kt | N8N_WEBHOOK_URL | Webhooks n8n | `https://tu-ip.ngrok-free.app/webhook/nexus` |
| DynamicConfigManager.kt | N8N_WEBHOOK_LICENSE | Validar licencias | `https://tu-ip.ngrok-free.app/webhook/license` |
| DynamicConfigManager.kt | N8N_WEBHOOK_PAYMENT | Confirmar pagos | `https://tu-ip.ngrok-free.app/webhook/payment` |

---

## ✅ CHECKLIST COMPLETO DEL ADMINISTRADOR

### Cuentas necesarias:
- [ ] Cuenta Google Cloud (OAuth2)
- [ ] Wallet TRON (TronLink/Trust Wallet)
- [ ] Servidor/Backend desplegado
- [ ] Repositorio GitHub para actualizaciones
- [ ] Firebase (opcional)

### URLs a configurar:
- [ ] URL del servidor de licencias
- [ ] URL del servidor de configuración
- [ ] URL de GitHub releases
- [ ] URL de descarga de modelos IA

### Valores de seguridad:
- [ ] Client ID de Google OAuth2
- [ ] Wallet TRON para pagos
- [ ] Firma SHA-256 de la app
- [ ] google-services.json (si usa Firebase)

### Archivos de configuración:
- [ ] config.json alojado en servidor
- [ ] APK subido a GitHub Releases
- [ ] Modelo IA alojado (.gguf)

---

## 📝 NOTAS IMPORTANTES

1. **NUNCA** subas estos valores a repositorios públicos
2. Usa variables de entorno o archivos de configuración cifrados
3. Considera usar Firebase Remote Config para valores dinámicos
4. Los pagos USDT TRC20 requieren al menos 20 confirmaciones
5. El grace period de licencias es de 3 días por defecto