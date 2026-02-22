# 📁 SponsorFlow Nexus v2.3 - Mapa Completo del Proyecto

## Estructura de Carpetas

```
app/src/main/java/com/sponsorflow/nexus/
│
├── 📱 account/                    # Gestión de cuentas
│   ├── GoogleSignInManager.kt     # Login Google
│   ├── LicenseVerifier.kt         # Verificación licencia
│   └── SessionManager.kt          # Sesiones de usuario
│
├── 🤖 ai/                         # Inteligencia Artificial
│   ├── AIEngine.kt                # Motor IA principal
│   ├── CloudAIProvider.kt         # OpenAI/Gemini/Anthropic
│   ├── LlamaBridge.kt             # Bridge llama.cpp
│   ├── PromptBuilder.kt           # Construcción prompts
│   └── ResourceDownloadManager.kt # Descarga modelo
│
├── 📊 analytics/                  # Analíticas (BÁSICO+)
│   ├── AnalyticsData.kt           # Modelos de datos
│   └── AnalyticsManager.kt        # Gestor analíticas
│
├── 🕵️ antidetection/             # Anti-detección
│   ├── HumanBehavior.kt           # Simulación humana
│   └── PatternRotator.kt          # Rotación patrones
│
├── 💾 cache/                      # Cache y Memoria
│   ├── AIMemoryManager.kt         # Memoria IA (BÁSICO+)
│   └── ConversationCache.kt       # Cache conversaciones
│
├── ⚙️ config/                     # Configuración
│   └── DynamicConfigManager.kt    # Config desde GitHub
│
├── 🔧 core/                       # Núcleo de la app
│   ├── BootReceiver.kt            # Inicio al arrancar
│   ├── NexusAccessibilityService.kt # Servicio accesibilidad
│   ├── NexusForegroundService.kt  # Servicio foreground
│   │
│   ├── contracts/                 # Interfaces
│   │   ├── ai/IAIEngine.kt
│   │   ├── plugin/IPluginContract.kt
│   │   ├── repository/IRepository.kt
│   │   └── security/
│   │       ├── IEncryptionService.kt
│   │       └── ILicenseValidator.kt
│   │
│   ├── enums/                     # Enumeraciones
│   │   ├── OperationStatus.kt
│   │   └── SubscriptionTier.kt
│   │
│   └── result/                    # Resultados
│       ├── AppError.kt
│       └── Result.kt
│
├── 💾 data/                       # Datos locales
│   ├── dao/                       # DAOs Room
│   │   ├── ContactDao.kt
│   │   ├── ConversationDao.kt
│   │   ├── MetricDao.kt
│   │   ├── ProductDao.kt
│   │   ├── SubscriptionDao.kt
│   │   └── TemplateDao.kt
│   │
│   ├── database/
│   │   └── NexusDatabase.kt       # DB principal
│   │
│   ├── entity/                    # Entidades Room
│   │   ├── ContactEntity.kt
│   │   ├── ConversationEntity.kt
│   │   ├── MetricEntity.kt
│   │   ├── ProductEntity.kt
│   │   ├── SubscriptionEntity.kt
│   │   └── TemplateEntity.kt
│   │
│   └── repositories/              # Repositorios
│       ├── ContactRepository.kt
│       ├── ConversationRepository.kt
│       ├── ProductRepository.kt
│       └── SubscriptionRepository.kt
│
├── 🔌 integration/                # Integraciones externas
│   └── WhatsAppAPI.kt             # WhatsApp Business API
│
├── 📦 inventory/                  # Inventario
│   └── ProductManager.kt          # Gestión productos
│
├── 🌐 language/                   # Multi-idioma (PRO+)
│   └── LanguageManager.kt         # 5 idiomas
│
├── 🧩 plugin/                     # Plugins (ENTERPRISE)
│   ├── PluginAPI.kt               # API para devs
│   ├── PluginManager.kt           # Gestión plugins
│   └── PluginTypes.kt             # Tipos de plugins
│
├── 🔒 security/                   # Seguridad
│   ├── EncryptionManager.kt       # Cifrado AES
│   └── IntegrityChecker.kt        # Verificación firma
│
├── 😊 sentiment/                  # Análisis sentimiento (PRO+)
│   ├── SentimentAnalyzer.kt       # Detecta emociones
│   └── SentimentTypes.kt          # Tipos sentimiento
│
├── 💳 subscription/               # Suscripciones
│   ├── PaymentManager.kt          # Pagos USDT TRC20
│   ├── SubscriptionGate.kt        # Control acceso
│   └── TronScanVerifier.kt        # Verifica transacciones
│
├── 🎨 ui/                         # Interfaz usuario
│   ├── MainActivity.kt
│   ├── OnboardingActivity.kt
│   ├── ResourceDownloadActivity.kt
│   │
│   ├── dashboard/
│   │   └── DashboardFragment.kt
│   │
│   ├── settings/
│   │   └── SettingsFragment.kt
│   │
│   ├── conversation/              # (vacío, para expandir)
│   └── subscription/              # (vacío, para expandir)
│
└── 📄 NexusApplication.kt         # Clase Application
```

---

## 📋 Módulos por Función

### 💰 PAGOS - subscription/
| Archivo | Función |
|---------|---------|
| PaymentManager.kt | Inicia pagos, genera QR |
| TronScanVerifier.kt | Verifica en blockchain |
| SubscriptionGate.kt | Controla acceso por plan |

### 💾 MEMORIA - cache/
| Archivo | Función |
|---------|---------|
| ConversationCache.kt | Guarda conversaciones |
| AIMemoryManager.kt | Contexto para IA |

### 📦 INVENTARIO - inventory/
| Archivo | Función |
|---------|---------|
| ProductManager.kt | Gestiona productos |

### 🤖 IA - ai/
| Archivo | Función |
|---------|---------|
| AIEngine.kt | Motor principal |
| LlamaBridge.kt | Conexión llama.cpp |
| CloudAIProvider.kt | APIs externas |

### 🔒 SEGURIDAD - security/
| Archivo | Función |
|---------|---------|
| EncryptionManager.kt | Cifrado datos |
| IntegrityChecker.kt | Anti-tampering |

---

## 🔄 Flujo de Actualización

### Para actualizar un módulo:

1. **Localizar archivo** en la carpeta correcta
2. **Modificar solo ese archivo**
3. **Ejecutar:** `./build.sh && ./test.sh`

### Ejemplo - Agregar nuevo idioma:
```
Editar: language/LanguageManager.kt
Agregar idioma a: SupportedLanguage enum
```

### Ejemplo - Modificar pago:
```
Editar: subscription/PaymentManager.kt
Modificar: walletAddress, QR, etc.
```

### Ejemplo - Nueva función IA:
```
Editar: ai/AIEngine.kt
Agregar lógica nueva
```

---

## 📊 Dependencias entre módulos

```
config/DynamicConfigManager
    ↓
ai/AIEngine → cache/AIMemoryManager
    ↓
subscription/PaymentManager
    ↓
security/EncryptionManager
```

---

## ✅ Checklist de actualización

- [ ] Identificar módulo correcto
- [ ] Modificar solo archivos necesarios
- [ ] Ejecutar build.sh
- [ ] Ejecutar test.sh
- [ ] Ejecutar deploy.sh