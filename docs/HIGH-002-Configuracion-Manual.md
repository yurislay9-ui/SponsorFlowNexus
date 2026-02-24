# HIGH-002: Guía de Configuración Manual de config.json

## Resumen
El archivo `config.json` contiene valores placeholder que deben ser reemplazados con valores reales antes del despliegue en producción. Este documento explica qué debe configurar y cómo obtener cada valor.

---

## Campos que Requieren Configuración

### 1. serverUrl
**Valor actual:** `"https://TU-URL-NGROK.ngrok-free.app"`

**Qué es:** La URL base de tu servidor backend.

**Cómo obtenerlo:**
1. Despliega tu servidor backend (Node.js, Python, etc.)
2. Si usas ngrok para desarrollo local:
   ```bash
   ngrok http 3000
   ```
   Copia la URL HTTPS que ngrok te proporciona (ej: `https://abc123.ngrok-free.app`)
3. Si usas un servidor en producción, usa tu dominio (ej: `https://api.tuapp.com`)

**Valor correcto:**
```json
"serverUrl": "https://tu-servidor-real.com"
```

---

### 2. google_client_id
**Valor actual:** `"TU_CLIENT_ID_GOOGLE_AQUI"`

**Qué es:** El Client ID de OAuth 2.0 de Google para autenticación.

**Cómo obtenerlo:**
1. Ve a [Google Cloud Console](https://console.cloud.google.com/)
2. Crea un proyecto o selecciona uno existente
3. Ve a "APIs & Services" > "Credentials"
4. Crea credenciales de "OAuth 2.0 Client IDs"
5. Configura el tipo de aplicación como "Android" o "Web application"
6. Copia el "Client ID"

**Valor correcto:**
```json
"google_client_id": "123456789-abcdefg.apps.googleusercontent.com"
```

---

### 3. app_signature
**Valor actual:** `"TU_FIRMA_SHA256_AQUI"`

**Qué es:** La firma SHA-256 de tu aplicación Android para verificación de integridad.

**Cómo obtenerlo:**
1. Genera tu keystore de firma:
   ```bash
   keytool -genkey -v -keystore mi-app.keystore -alias mi-app -keyalg RSA -keysize 2048 -validity 10000
   ```

2. Obtén el hash SHA-256:
   ```bash
   keytool -list -v -keystore mi-app.keystore -alias mi-app
   ```

3. Convierte a formato hexadecimal (sin los dos puntos):
   ```bash
   keytool -list -v -keystore mi-app.keystore -alias mi-app | grep SHA256 | awk '{print $2}' | tr -d ':'
   ```

**Valor correcto:**
```json
"app_signature": "a1b2c3d4e5f6789012345678901234567890abcdef1234567890abcdef123456"
```

---

### 4. wallet_address (TRON TRC20)
**Valor actual:** `"TU_DIRECCION_TRON_TRC20_AQUI"`

**Qué es:** Tu dirección de billetera TRON para recibir pagos en USDT (TRC20).

**Cómo obtenerlo:**
1. Crea una billetera TRON en [TronLink](https://www.tronlink.org/) o [Trust Wallet](https://trustwallet.com/)
2. Copia tu dirección pública (empieza con "T")
3. **IMPORTANTE:** Esta es la dirección donde recibirás los pagos, mantenla segura

**Valor correcto:**
```json
"wallet_address": "TYourTronAddress123456789abcdefghijklmnop"
```

---

### 5. webhooks (nexus, license, payment)
**Valores actuales:** URLs con placeholder de ngrok

**Qué son:** URLs donde tu servidor recibirá notificaciones de la app.

**Cómo configurarlos:**
1. En tu servidor backend, crea los endpoints:
   - `/webhook/nexus` - Para eventos generales
   - `/webhook/license` - Para validación de licencias
   - `/webhook/payment` - Para confirmación de pagos

2. Usa la misma URL base que `serverUrl`:
```json
"webhooks": {
  "nexus": "https://tu-servidor-real.com/webhook/nexus",
  "license": "https://tu-servidor-real.com/webhook/license",
  "payment": "https://tu-servidor-real.com/webhook/payment"
}
```

---

### 6. admin_webhooks (heartbeat_url, error_url)
**Valores actuales:** URLs con placeholder de n8n

**Qué son:** URLs de webhooks de n8n para monitoreo y reportes de errores.

**Cómo configurarlos:**
1. Instala [n8n](https://n8n.io/) (puede ser self-hosted o cloud)
2. Crea flujos de trabajo con triggers de webhook:
   - Un flujo para recibir heartbeats (latidos de la app)
   - Un flujo para recibir reportes de errores
3. Copia las URLs de los webhooks de n8n

**Valor correcto:**
```json
"admin_webhooks": {
  "heartbeat_url": "https://tu-n8n.com/webhook/abc123-heartbeat",
  "error_url": "https://tu-n8n.com/webhook/def456-errors"
}
```

---

### 7. ai_model.download_url
**Valor actual:** `"URL_DEL_MODELO_EN_GITHUB_O_HUGGINGFACE"`

**Qué es:** URL de descarga del modelo de IA en formato GGUF.

**Cómo obtenerlo:**
1. Opción A - GitHub Release:
   - Sube el modelo a un release de GitHub
   - Copia la URL de descarga directa

2. Opción B - Hugging Face:
   - Sube el modelo a [Hugging Face](https://huggingface.co/)
   - Usa la URL de descarga directa

**Valor correcto:**
```json
"ai_model": {
  "download_url": "https://huggingface.co/tu-usuario/tu-modelo/resolve/main/modelo.gguf",
  "version": "1.0.0",
  "filename": "modelo.gguf",
  "size_mb": 1500
}
```

---

### 8. github_updates
**Valores actuales:** URLs con placeholder

**Qué son:** URLs de la API de GitHub para verificar actualizaciones.

**Cómo configurarlos:**
1. Crea repositorios en GitHub para:
   - La app Android (ej: `yurislay9-ui/nexus-app`)
   - Los fixes de WhatsApp (ej: `yurislay9-ui/nexus-whatsapp-fix`)

2. Usa la API de GitHub para releases:
```json
"github_updates": {
  "app_update_url": "https://api.github.com/repos/yurislay9-ui/nexus-app/releases/latest",
  "whatsapp_fix_url": "https://api.github.com/repos/yurislay9-ui/nexus-whatsapp-fix/releases/latest"
}
```

---

## Ejemplo de config.json Completado

```json
{
  "config_url": "https://raw.githubusercontent.com/yurislay9-ui/nexus-backend/main/config.json",
  "serverUrl": "https://api.tuapp.com",
  "minAppVersion": "2.4.0",
  "maintenanceMode": false,
  "announcement": null,

  "google_client_id": "123456789-abcdefg.apps.googleusercontent.com",
  "app_signature": "a1b2c3d4e5f6789012345678901234567890abcdef1234567890abcdef123456",

  "payment_config": {
    "wallet_address": "TYourTronAddress123456789abcdefghijklmnop",
    "usdt_contract": "TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t",
    "required_confirmations": 20
  },

  "webhooks": {
    "nexus": "https://api.tuapp.com/webhook/nexus",
    "license": "https://api.tuapp.com/webhook/license",
    "payment": "https://api.tuapp.com/webhook/payment"
  },

  "admin_webhooks": {
    "heartbeat_url": "https://n8n.tuapp.com/webhook/heartbeat",
    "error_url": "https://n8n.tuapp.com/webhook/errors"
  },

  "admin_commands": {
    "global": [],
    "devices": {}
  },

  "admin_banned_devices": [],

  "heartbeat_interval_hours": 1,

  "ai_model": {
    "download_url": "https://huggingface.co/tu-usuario/modelo-ia/resolve/main/modelo.gguf",
    "version": "1.0.0",
    "filename": "modelo.gguf",
    "size_mb": 1500
  },

  "github_updates": {
    "app_update_url": "https://api.github.com/repos/yurislay9-ui/nexus-app/releases/latest",
    "whatsapp_fix_url": "https://api.github.com/repos/yurislay9-ui/nexus-whatsapp-fix/releases/latest"
  },

  "whatsapp_config": {
    "minVersion": "2.24.1",
    "updateRequired": false
  }
}
```

---

## ⚠️ Advertencias de Seguridad

1. **NO commitees** el archivo config.json con valores reales a un repositorio público
2. Usa variables de entorno o un gestor de secrets para producción
3. La `wallet_address` debe ser una billetera dedicada para la app, no tu billetera personal principal
4. El `app_signature` debe mantenerse privado - cualquiera con acceso puede firmar apps maliciosas
5. Los webhooks de administrador (`admin_webhooks`) deben estar protegidos con autenticación

---

## Checklist de Configuración

- [ ] serverUrl configurado
- [ ] google_client_id obtenido de Google Cloud Console
- [ ] app_signature generado desde tu keystore
- [ ] wallet_address creado en billetera TRON
- [ ] webhooks configurados en tu backend
- [ ] admin_webhooks configurados en n8n
- [ ] ai_model subido y URL obtenida
- [ ] Repositorios de GitHub creados para actualizaciones