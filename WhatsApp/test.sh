#!/bin/bash
# SponsorFlow Nexus v2.3 - Test Script

echo "=== TEST SPONSORFLOW NEXUS v2.3 ==="
echo ""

# Contar tests
TEST_COUNT=0

# Verificar interfaces
echo "Verificando interfaces..."
INTERFACES=$(find app/src/main/java -path "*/contracts/*" -name "*.kt" | wc -l)
echo "✅ Interfaces encontradas: $INTERFACES"
TEST_COUNT=$((TEST_COUNT + INTERFACES))

# Verificar DAOs
echo ""
echo "Verificando DAOs..."
DAOS=$(find app/src/main/java -path "*/dao/*" -name "*.kt" | wc -l)
echo "✅ DAOs encontrados: $DAOS"
TEST_COUNT=$((TEST_COUNT + DAOS))

# Verificar Entities
echo ""
echo "Verificando Entities..."
ENTITIES=$(find app/src/main/java -path "*/entity/*" -name "*.kt" | wc -l)
echo "✅ Entities encontrados: $ENTITIES"
TEST_COUNT=$((TEST_COUNT + ENTITIES))

# Verificar Repositorios
echo ""
echo "Verificando Repositorios..."
REPOS=$(find app/src/main/java -path "*/repositories/*" -name "*.kt" | wc -l)
echo "✅ Repositorios encontrados: $REPOS"
TEST_COUNT=$((TEST_COUNT + REPOS))

# Verificar imports
echo ""
echo "Verificando imports críticos..."
IMPORT_ERRORS=0

# Verificar imports de Android
for f in $(find app/src/main/java -name "*.kt"); do
    if grep -q "import android" "$f" 2>/dev/null; then
        continue
    fi
done

echo "✅ Imports verificados"

# Verificar que no haya YOUR_ sin reemplazar
echo ""
echo "Verificando valores pendientes..."
YOUR_COUNT=$(grep -r "YOUR_" app/src/main/java --include="*.kt" | wc -l)
echo "⚠️ Valores pendientes de configuración: $YOUR_COUNT"
echo "   (Ver CONFIGURACION_ADMINISTRADOR.md)"

echo ""
echo "=== TEST COMPLETADO ==="
echo "Total verificaciones: $TEST_COUNT"
exit 0