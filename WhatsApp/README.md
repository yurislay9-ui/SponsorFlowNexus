# SponsorFlow Nexus - WhatsApp Automation App

## 🚀 Compilación con GitHub Actions

### Configuración Automática

El proyecto incluye un workflow de GitHub Actions que compila automáticamente:

1. **Push a main/develop** → Compila automáticamente
2. **Pull Request** → Compila y ejecuta tests
3. **Release** → Genera APK para descargar

### Workflows Incluidos

| Workflow | Función |
|----------|---------|
| `android.yml` | Build + Test + Release |

### Cómo Usar

#### 1. Subir a GitHub
```bash
git init
git add .
git commit -m "Initial commit"
git branch -M main
git remote add origin https://github.com/TU_USUARIO/nexus-backend.git
git push -u origin main
```

#### 2. Configurar Secrets (Opcional)
En GitHub → Settings → Secrets:
- `ANDROID_SDK_TOKEN` - Token para Android SDK
- `FIREBASE_TOKEN` - Para distribución (opcional)

#### 3. Compilación Automática
- Ve a **Actions** en tu repositorio
- El workflow corre automáticamente
- Descarga el APK desde **Artifacts**

### Estructura del Proyecto

```
WhatsApp/
├── .github/
│   └── workflows/
│       └── android.yml          # CI/CD Pipeline
├── app/
│   └── src/main/
│       ├── java/com/sponsorflow/nexus/
│       │   ├── config/          # NexusConfigManager
│       │   ├── ui/             # Screens
│       │   ├── subscription/    # PaymentManager
│       │   └── ...
│       └── res/                 # Resources
├── build.gradle.kts
└── gradlew
```

### Configuración

#### Variables de Build
Las variables se configuran en GitHub Secrets:
- `CONFIG_URL` - URL de config en GitHub
- `SERVER_URL` - URL del servidor API

### GitHub como Cerebro

El proyecto está diseñado para que **GitHub sea el cerebro**:
- Configuración centralizada en `config.json`
- URLs ocultas en el código
- Todo carga desde GitHub

### Seguridad
- ✅ URLs nunca visibles en código
- ✅ GitHub Actions compila automáticamente
- ✅ APK disponible para descarga

### Licencia
Copyright © 2024 SponsorFlow Nexus
