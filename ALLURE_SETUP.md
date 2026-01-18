# Configuración de Allure para Reportes de Tests

## Descripción

Este proyecto utiliza Allure Framework para generar reportes HTML interactivos de los tests automatizados. Allure captura automáticamente screenshots, logs y otra información cuando los tests fallan.

## Componentes

### 1. AllureScreenshotRule

Regla de JUnit que automáticamente:
- Captura screenshots cuando un test falla
- Adjunta logs de errores al reporte
- Adjunta stack traces
- Guarda toda la evidencia en el reporte de Allure

**Uso:**
```kotlin
@get:Rule
val allureScreenshotRule = AllureScreenshotRule()
```

### 2. Tests de Integración

- **SDKIntegrationTest**: Valida el ciclo de vida básico del SDK
- **PictureInPictureTest**: Valida funcionalidad de Picture-in-Picture

## Configuración Local

### 1. Instalar Allure CLI

**macOS:**
```bash
brew install allure
```

**Linux:**
```bash
# Descargar y extraer
wget https://github.com/allure-framework/allure2/releases/download/2.24.0/allure-2.24.0.tgz
tar -zxvf allure-2.24.0.tgz
sudo mv allure-2.24.0 /opt/allure
sudo ln -s /opt/allure/bin/allure /usr/local/bin/allure
```

**Windows:**
```powershell
# Usar Chocolatey
choco install allure-commandline

# O descargar manualmente desde:
# https://github.com/allure-framework/allure2/releases
```

### 2. Ejecutar Tests con Allure

```bash
# Ejecutar tests
./gradlew connectedAndroidTest

# Los resultados se guardan en:
# app/build/outputs/androidTest-results/connected/allure-results/

# Generar reporte
allure generate app/build/outputs/androidTest-results/connected/allure-results -o allure-report --clean

# Abrir reporte
allure open allure-report
```

## Configuración de CI/CD

### GitHub Actions

El workflow `.github/workflows/android_tests.yml` está configurado para:

1. **Ejecutar tests en matriz de versiones del SDK:**
   - 9.6.0
   - 9.7.0
   - 9.8.0
   - 9.9.1-alpha04

2. **Generar reportes de Allure automáticamente**

3. **Subir reportes como artifacts**

4. **Desplegar reportes a GitHub Pages** (solo en branch main)

5. **Enviar notificaciones a Slack**

### Configurar Slack Webhook

1. Crear un webhook en Slack:
   - Ir a https://api.slack.com/apps
   - Crear nueva app o usar existente
   - Ir a "Incoming Webhooks"
   - Activar y crear webhook
   - Copiar la URL del webhook

2. Agregar secret en GitHub:
   - Ir a Settings → Secrets and variables → Actions
   - Agregar nuevo secret: `SLACK_WEBHOOK_URL`
   - Pegar la URL del webhook

### Ver Reportes en GitHub Pages

Después de que el workflow se ejecute en `main`:
- Los reportes estarán disponibles en: `https://<usuario>.github.io/<repo>/test-reports/`

## Estructura de Reportes

Los reportes de Allure incluyen:

1. **Overview**: Resumen general de todos los tests
2. **Behaviors**: Tests agrupados por funcionalidad
3. **Packages**: Tests agrupados por paquete
4. **Suites**: Tests agrupados por suite
5. **Graphs**: Gráficos de resultados
6. **Timeline**: Línea de tiempo de ejecución

### Información Capturada Automáticamente

- **Screenshots**: Capturadas cuando un test falla
- **Logs de errores**: Filtrados automáticamente
- **Logs del SDK**: Específicos de MediastreamPlayer
- **Stack traces**: Completos de las excepciones
- **Steps**: Pasos detallados usando `Allure.step()`

## Mejores Prácticas

1. **Usar Allure.step()** para documentar pasos importantes:
   ```kotlin
   Allure.step("Ejecutar play() y verificar callbacks") {
       // código del test
   }
   ```

2. **Usar @DisplayName** para nombres descriptivos:
   ```kotlin
   @DisplayName("Validar ciclo de vida básico del SDK")
   fun testSDKBasicLifecycle() { ... }
   ```

3. **Adjuntar información adicional cuando sea necesario**:
   ```kotlin
   Allure.addAttachment("Configuración", "text/plain", configJson)
   ```

## Troubleshooting

### Los reportes no se generan

- Verificar que Allure CLI esté instalado
- Verificar que los resultados estén en la ruta correcta
- Revisar logs del workflow en GitHub Actions

### Las screenshots no aparecen

- Verificar permisos de almacenamiento
- Verificar que `ScreenshotCapture.capture()` se ejecute correctamente
- Revisar logs de `AllureScreenshotRule`

### Slack no recibe notificaciones

- Verificar que el secret `SLACK_WEBHOOK_URL` esté configurado
- Verificar que el webhook esté activo en Slack
- Revisar logs del workflow para errores

## Referencias

- [Allure Framework](https://github.com/allure-framework/allure2)
- [Allure Kotlin](https://github.com/allure-framework/allure-kotlin)
- [GitHub Actions](https://docs.github.com/en/actions)
