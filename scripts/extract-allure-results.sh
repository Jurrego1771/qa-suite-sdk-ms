#!/bin/bash

# Script para extraer resultados de Allure y generar estadísticas
# Uso: ./extract-allure-results.sh <allure-results-dir>

ALLURE_RESULTS_DIR=${1:-"app/build/outputs/androidTest-results/connected/allure-results"}

if [ ! -d "$ALLURE_RESULTS_DIR" ]; then
    echo "Error: Directorio de resultados no encontrado: $ALLURE_RESULTS_DIR"
    exit 1
fi

TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0
BROKEN_TESTS=0
SKIPPED_TESTS=0

# Procesar archivos JSON de Allure
for json_file in "$ALLURE_RESULTS_DIR"/*-result.json; do
    if [ -f "$json_file" ]; then
        STATUS=$(jq -r '.status // "unknown"' "$json_file")
        
        TOTAL_TESTS=$((TOTAL_TESTS + 1))
        
        case "$STATUS" in
            "passed")
                PASSED_TESTS=$((PASSED_TESTS + 1))
                ;;
            "failed")
                FAILED_TESTS=$((FAILED_TESTS + 1))
                ;;
            "broken")
                BROKEN_TESTS=$((BROKEN_TESTS + 1))
                ;;
            "skipped")
                SKIPPED_TESTS=$((SKIPPED_TESTS + 1))
                ;;
        esac
    fi
done

# Si no hay archivos JSON, intentar parsear XML
if [ $TOTAL_TESTS -eq 0 ]; then
    XML_DIR=$(dirname "$ALLURE_RESULTS_DIR")
    for xml_file in "$XML_DIR"/*.xml; do
        if [ -f "$xml_file" ]; then
            TESTS=$(grep -o 'tests="[0-9]*"' "$xml_file" | head -1 | grep -o '[0-9]*' || echo "0")
            FAILURES=$(grep -o 'failures="[0-9]*"' "$xml_file" | head -1 | grep -o '[0-9]*' || echo "0")
            ERRORS=$(grep -o 'errors="[0-9]*"' "$xml_file" | head -1 | grep -o '[0-9]*' || echo "0")
            
            TOTAL_TESTS=$((TOTAL_TESTS + TESTS))
            FAILED_TESTS=$((FAILED_TESTS + FAILURES + ERRORS))
        fi
    done
    PASSED_TESTS=$((TOTAL_TESTS - FAILED_TESTS))
fi

echo "TOTAL_TESTS=$TOTAL_TESTS"
echo "PASSED_TESTS=$PASSED_TESTS"
echo "FAILED_TESTS=$FAILED_TESTS"
echo "BROKEN_TESTS=$BROKEN_TESTS"
echo "SKIPPED_TESTS=$SKIPPED_TESTS"

# Generar JSON para GitHub Actions
cat > test-results.json << EOF
{
  "total": $TOTAL_TESTS,
  "passed": $PASSED_TESTS,
  "failed": $FAILED_TESTS,
  "broken": $BROKEN_TESTS,
  "skipped": $SKIPPED_TESTS
}
EOF

echo "Resultados guardados en test-results.json"
