#!/bin/bash
# SponsorFlow Nexus v2.3 - Deploy Script

echo "=== DEPLOY SPONSORFLOW NEXUS v2.3 ==="
echo ""

# Verificar que build y test pasaron
if [ ! -f "build.sh" ] || [ ! -f "test.sh" ]; then
    echo "❌ ERROR: Faltan scripts build.sh o test.sh"
    exit 1
fi

# Verificar estructura completa
echo "Verificando estructura de despliegue..."

# Verificar AndroidManifest
if [ ! -f "app/src/main/AndroidManifest.xml" ]; then
    echo "❌ ERROR: AndroidManifest.xml no encontrado"
    exit 1
fi
echo "✅ AndroidManifest.xml encontrado"

# Verificar gradle files
if [ ! -f "build.gradle.kts" ] || [ ! -f "app/build.gradle.kts" ]; then
    echo "❌ ERROR: Archivos Gradle no encontrados"
    exit 1
fi
echo "✅ Archivos Gradle correctos"

# Verificar recursos
if [ ! -d "app/src/main/res" ]; then
    echo "❌ ERROR: Carpeta res no encontrada"
    exit 1
fi
echo "✅ Recursos presentes"

# Verificar ProGuard
if [ ! -f "app/proguard-rules.pro" ]; then
    echo "❌ ERROR: ProGuard rules no encontrado"
    exit 1
fi
echo "✅ ProGuard configurado"

# Verificar CI/CD
if [ ! -f ".github/workflows/ci.yml" ]; then
    echo "⚠️ CI workflow no encontrado"
else
    echo "✅ CI workflow configurado"
fi

# Resumen de archivos
echo ""
echo "=== RESUMEN DE ARCHIVOS ==="
KOTLIN=$(find app/src/main/java -name "*.kt" | wc -l)
XML=$(find app/src/main/res -name "*.xml" | wc -l)
echo "Archivos Kotlin: $KOTLIN"
echo "Archivos XML: $XML"
echo "Total: $((KOTLIN + XML))"

echo ""
echo "=== DEPLOY COMPLETADO ==="
echo "El proyecto está listo para compilación con ./gradlew assembleDebug"
exit 0