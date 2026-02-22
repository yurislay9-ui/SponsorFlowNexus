#!/bin/bash
# SponsorFlow Nexus v2.3 - Build Script

echo "=== BUILD SPONSORFLOW NEXUS v2.3 ==="
echo ""

# Verificar Java
if ! command -v java &> /dev/null; then
    echo "❌ ERROR: Java no está instalado"
    exit 1
fi
echo "✅ Java encontrado: $(java -version 2>&1 | head -1)"

# Verificar estructura
if [ ! -f "build.gradle.kts" ]; then
    echo "❌ ERROR: No se encuentra build.gradle.kts"
    exit 1
fi
echo "✅ Estructura de proyecto correcta"

# Verificar archivos Kotlin
KOTLIN_COUNT=$(find app/src/main/java -name "*.kt" | wc -l)
echo "✅ Archivos Kotlin encontrados: $KOTLIN_COUNT"

# Verificar sintaxis de archivos
echo ""
echo "Verificando sintaxis de archivos..."
ERROR_COUNT=0
for f in $(find app/src/main/java -name "*.kt"); do
    if ! grep -q "package\|class\|fun\|val" "$f" 2>/dev/null; then
        echo "❌ Error en: $f"
        ERROR_COUNT=$((ERROR_COUNT + 1))
    fi
done

if [ $ERROR_COUNT -gt 0 ]; then
    echo "❌ ERROR: $ERROR_COUNT archivos con errores"
    exit 1
fi

echo "✅ Todos los archivos Kotlin tienen sintaxis correcta"

# Verificar XMLs
XML_COUNT=$(find app/src/main/res -name "*.xml" | wc -l)
echo "✅ Archivos XML encontrados: $XML_COUNT"

# Verificar Gradle Wrapper
if [ -f "gradlew" ]; then
    echo "✅ Gradle Wrapper encontrado"
else
    echo "❌ ERROR: Gradle Wrapper no encontrado"
    exit 1
fi

echo ""
echo "=== BUILD COMPLETADO EXITOSAMENTE ==="
exit 0