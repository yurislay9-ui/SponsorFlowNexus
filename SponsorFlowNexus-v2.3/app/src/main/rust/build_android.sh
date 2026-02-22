#!/bin/bash
# ============================================================
# SponsorFlow Nexus - Rust Android Build Script
# Compila la librería Rust para múltiples arquitecturas Android
# ============================================================

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(cd "$SCRIPT_DIR/../../../.." && pwd)"
NDK_HOME="${NDK_HOME:-$HOME/Android/Sdk/ndk/26.1.10909125}"
RUST_DIR="$SCRIPT_DIR"

# Arquitecturas soportadas
TARGETS=(
    "aarch64-linux-android"
    "armv7-linux-androideabi"
    "x86_64-linux-android"
    "i686-linux-android"
)

# API level mínimo
API_LEVEL=26

echo "🦀 Compilando Rust para Android..."
echo "   NDK: $NDK_HOME"
echo "   API Level: $API_LEVEL"
echo "   Targets: ${TARGETS[*]}"
echo ""

# Verificar que el NDK existe
if [ ! -d "$NDK_HOME" ]; then
    echo "❌ NDK no encontrado en: $NDK_HOME"
    echo "   Por favor, instala el NDK o establece la variable NDK_HOME"
    exit 1
fi

# Configurar toolchain de Android
export ANDROID_NDK_HOME="$NDK_HOME"
export NDK_HOME="$NDK_HOME"

# Crear directorio de salida
OUTPUT_DIR="$PROJECT_DIR/app/src/main/jniLibs"
mkdir -p "$OUTPUT_DIR"

# Compilar para cada arquitectura
for TARGET in "${TARGETS[@]}"; do
    echo "📦 Compilando para $TARGET..."
    
    # Determinar el directorio de salida según la arquitectura
    case $TARGET in
        aarch64-linux-android)
            LIB_DIR="arm64-v8a"
            ;;
        armv7-linux-androideabi)
            LIB_DIR="armeabi-v7a"
            ;;
        x86_64-linux-android)
            LIB_DIR="x86_64"
            ;;
        i686-linux-android)
            LIB_DIR="x86"
            ;;
    esac
    
    # Configurar el linker
    export CFLAGS_aarch64_linux_android="--target=aarch64-linux-android$API_LEVEL"
    export CFLAGS_armv7_linux_androideabi="--target=armv7-linux-androideabi$API_LEVEL"
    export CFLAGS_x86_64_linux_android="--target=x86_64-linux-android$API_LEVEL"
    export CFLAGS_i686_linux_android="--target=i686-linux-android$API_LEVEL"
    
    # Construir con cargo
    cd "$RUST_DIR"
    cargo build --release --target "$TARGET"
    
    # Copiar la librería al directorio jniLibs
    mkdir -p "$OUTPUT_DIR/$LIB_DIR"
    cp "target/$TARGET/release/libnexus_rust.so" "$OUTPUT_DIR/$LIB_DIR/"
    
    echo "   ✅ $TARGET -> $OUTPUT_DIR/$LIB_DIR/libnexus_rust.so"
done

echo ""
echo "✅ Build completado!"
echo "   Librerías en: $OUTPUT_DIR"
ls -la "$OUTPUT_DIR"/*/libnexus_rust.so 2>/dev/null || echo "   (No se encontraron librerías)"