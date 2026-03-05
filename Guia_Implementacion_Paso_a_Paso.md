# Guía de Implementación Paso a Paso - SponsorFlowNexus

## Resumen Ejecutivo

Esta guía proporciona instrucciones detalladas y ordenadas para implementar todas las correcciones críticas y optimizaciones identificadas en los documentos anteriores.

---

## Fase 1: Correcciones Críticas (Prioridad Inmediata)

### Paso 1.1: Configurar JLeveraging en Gradle

**Archivo**: `WhatsApp/build.gradle.kts`

1. Abrir el archivo `build.gradle.kts` del módulo app
2. Localizar el bloque `dependencies`
3. Agregar las dependencias JLeveraging faltantes:

```kotlin
dependencies {
    // Core KTX (verificar versión)
    implementation("androidx.core:core-ktx:1.12.0")
    
    // Activity y Fragment KTX
    implementation("androidx.activity:activity-ktx:1.8.2")
    implementation("androidx.fragment:fragment-ktx:1.6.2")
    
    // Lifecycle KTX
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
}
```

**Verificación**: Ejecutar `./gradlew build` para confirmar que las dependencias se resuelven correctamente.

---

### Paso 1.2: Actualizar AndroidManifest.xml

**Archivo**: `WhatsApp/app/src/main/AndroidManifest.xml`

1. Agregar permisos necesarios:

```xml
<!-- Permisos de red -->
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

<!-- Permisos de almacenamiento (si son necesarios) -->
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />

<!-- Si usas cámara -->
<uses-permission android:name="android.permission.CAMERA" />
```

2. Configurar la Application class si existe:

```xml
<application
    android:name=".SponsorFlowApplication"
    android:allowBackup="true"
    android:icon="@mipmap/ic_launcher"
    android:label="@string/app_name"
    android:roundIcon="@mipmap/ic_launcher_round"
    android:supportsRtl="true"
    android:theme="@style/Theme.SponsorFlowNexus"
    android:usesCleartextTraffic="true">
```

**Verificación**: Revisar que no haya errores de lint en el manifest.

---

### Paso 1.3: Corregir errores de JNI/Rust

**Paso 1.3.1**: Verificar estructura de crates Rust

```bash
cd WhatsApp/app/src/main/rust
cargo check --all-targets
```

**Paso 1.3.2**: Verificar nomenclatura JNI

Para cada función `external fun` en Kotlin, verificar que existe su contraparte en Rust:

```kotlin
// Kotlin
package com.sponsorflownexus

class NativeBridge {
    external fun processData(input: String): String
}
```

```rust
// Rust
#[no_mangle]
pub extern "system" fn Java_com_sponsorflownexus_NativeBridge_processData(
    mut env: JNIEnv,
    _class: JClass,
    input: JString,
) -> jstring {
    // Implementación
}
```

**Paso 1.3.3**: Actualizar CMakeLists.txt

```cmake
cmake_minimum_required(VERSION 3.22.1)
project("sponsorflownexus")

add_library(sponsorflownexus SHARED
    src/main/cpp/native-lib.cpp
)

target_link_libraries(sponsorflownexus
    android
    log
)
```

---

### Paso 1.4: Configurar ProGuard/R8

**Archivo**: `WhatsApp/app/proguard-rules.pro`

```proguard
# Reglas para JNI
-keepclasseswithmembernames class * {
    native <methods>;
}

# Reglas para reflection
-keep class com.sponsorflownexus.** { *; }

# Reglas para data classes usadas en JSON
-keep class com.sponsorflownexus.data.model.** { *; }

# Gson/Retrofit
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }
-keep class retrofit2.** { *; }

# Coroutines
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}
```

---

## Fase 2: Optimizaciones de Arquitectura

### Paso 2.1: Migrar a StateFlow en ViewModels

**Antes**:
```kotlin
class MainViewModel : ViewModel() {
    private val _data = MutableLiveData<String>()
    val data: LiveData<String> = _data
}
```

**Después**:
```kotlin
class MainViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
    
    sealed class UiState {
        object Loading : UiState()
        data class Success(val data: String) : UiState()
        data class Error(val message: String) : UiState()
    }
}
```

---

### Paso 2.2: Implementar Inyección de Dependencias con Hilt

**Paso 2.2.1**: Agregar dependencias en `build.gradle.kts`:

```kotlin
plugins {
    id("com.google.dagger.hilt.android") version "2.50" apply true
}

dependencies {
    implementation("com.google.dagger:hilt-android:2.50")
    kapt("com.google.dagger:hilt-compiler:2.50")
    
    // Para ViewModels
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")
}
```

**Paso 2.2.2**: Crear Application class:

```kotlin
@HiltAndroidApp
class SponsorFlowApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Inicialización
    }
}
```

**Paso 2.2.3**: Anotar Activities/Fragments:

```kotlin
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    // ...
}
```

---

### Paso 2.3: Optimizar Compose

**Paso 2.3.1**: Usar remember correctamente:

```kotlin
@Composable
fun SponsorList(sponsors: List<Sponsor>) {
    // ✅ Correcto: usar remember para objetos costosos
    val formatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    
    // ✅ Usar derivedStateOf para estados derivados
    val sortedSponsors by remember {
        derivedStateOf { 
            sponsors.sortedByDescending { it.amount } 
        }
    }
}
```

**Paso 2.3.2**: Estabilizar parámetros:

```kotlin
// ✅ Usar @Stable para clases inmutables
@Stable
data class Sponsor(
    val id: String,
    val name: String,
    val amount: Double
)

// ✅ Usar key en LazyColumn
LazyColumn {
    items(
        items = sponsors,
        key = { sponsor -> sponsor.id }
    ) { sponsor ->
        SponsorItem(sponsor)
    }
}
```

---

## Fase 3: Optimizaciones de Build

### Paso 3.1: Configurar build.gradle.kts

```kotlin
android {
    // Compilar solo los recursos necesarios
    buildFeatures {
        compose = true
        buildConfig = true
        aidl = false
        renderScript = false
        resValues = false
        shaders = false
    }
    
    // Optimizar build types
    buildTypes {
        debug {
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    
    // Configurar Kotlin compile options
    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += listOf(
            "-opt-in=kotlin.RequiresOptIn",
            "-Xjvm-default=all-compatibility"
        )
    }
}
```

### Paso 3.2: Configurar Gradle.properties

```properties
# Habilitar paralelismo
org.gradle.parallel=true

# Habilitar caching
org.gradle.caching=true

# Configurar memoria
org.gradle.jvmargs=-Xmx4g -XX:+UseParallelGC

# Habilitar build config cache
org.gradle.configuration-cache=true

# Kotlin daemon
kotlin.daemon.jvmargs=-Xmx2g
```

---

## Fase 4: Testing

### Paso 4.1: Configurar tests unitarios

```kotlin
// app/src/test/java/com/sponsorflownexus/MainViewModelTest.kt
class MainViewModelTest {
    @get:Rule
    val dispatcherRule = StandardTestDispatcher()
    
    private lateinit var viewModel: MainViewModel
    
    @Before
    fun setup() {
        viewModel = MainViewModel()
    }
    
    @Test
    fun `initial state is Loading`() = runTest {
        assertEquals(UiState.Loading, viewModel.uiState.value)
    }
}
```

### Paso 4.2: Configurar tests de UI

```kotlin
// app/src/androidTest/java/com/sponsorflownexus/MainActivityTest.kt
@RunWith(AndroidJUnit4::class)
class MainActivityTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun app_launchesSuccessfully() {
        composeTestRule.setContent {
            SponsorFlowNexusTheme {
                MainScreen()
            }
        }
        
        composeTestRule.waitForIdle()
    }
}
```

---

## Fase 5: Verificación Final

### Checklist de Verificación

| Item | Comando/acción | Estado |
|------|----------------|--------|
| Build limpia | `./gradlew clean build` | [ ] |
| Tests unitarios | `./gradlew test` | [ ] |
| Tests instrumentados | `./gradlew connectedAndroidTest` | [ ] |
| Lint check | `./gradlew lint` | [ ] |
| Rust cargo check | `cargo check --all-targets` | [ ] |
| APK size check | Analizar APK en Android Studio | [ ] |

---

## Orden de Ejecución Recomendado

1. **Día 1**: Fase 1 completa (correcciones críticas)
2. **Día 2**: Fase 2.1 y 2.2 (ViewModel y Hilt)
3. **Día 3**: Fase 2.3 (Compose)
4. **Día 4**: Fase 3 (Build optimizations)
5. **Día 5**: Fase 4 y 5 (Testing y verificación)

---

## Comandos Útiles

```bash
# Build completo
cd WhatsApp && ./gradlew clean build

# Solo app
./gradlew :app:assembleDebug

# Instalar en dispositivo
./gradlew installDebug

# Ver dependencias
./gradlew :app:dependencies

# Analizar APK
./gradlew :app:analyzeReleaseBundle

# Rust
cd app/src/main/rust && cargo build --release
```

---

## Contacto y Soporte

Si encuentras problemas durante la implementación:
1. Verificar los logs de build: `./gradlew build --stacktrace`
2. Revisar issues similares en el repositorio
3. Documentar el error con screenshots y logs completos

---

**Documento generado automáticamente para SponsorFlowNexus**
**Versión: 1.0.0**