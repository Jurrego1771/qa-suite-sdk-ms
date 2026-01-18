# Guía de CI/CD - Android Tests con Allure

## Resumen

Este proyecto incluye una pipeline completa de CI/CD que:
- Ejecuta tests automatizados en múltiples versiones del SDK
- Genera reportes HTML interactivos con Allure
- Despliega reportes a GitHub Pages
- Envía notificaciones a Slack

## Componentes

### 1. Workflow de GitHub Actions

**Archivo:** `.github/workflows/android_tests.yml`

**Características:**
- ✅ Matriz de versiones del SDK (9.6.0, 9.7.0, 9.8.0, 9.9.1-alpha04)
- ✅ Ejecución en emulador Android API 34
- ✅ Generación automática de reportes Allure
- ✅ Despliegue a GitHub Pages
- ✅ Notificaciones a Slack

### 2. Tests de Integración

**SDKIntegrationTest:**
- Valida inicialización con MediastreamPlayerConfig
- Verifica llamada a play()
- Verifica callbacks onPlay() o onError()

**PictureInPictureTest:**
- Valida funcionalidad de Picture-in-Picture
- Usa UI Automator para verificar ventana PiP

### 3. AllureScreenshotRule

Captura automáticamente:
- Screenshots en fallos
- Logs de errores
- Logs del SDK
- Stack traces

## Configuración

### 1. Secrets de GitHub

Configurar los siguientes secrets en GitHub (Settings → Secrets and variables → Actions):

**SLACK_WEBHOOK_URL** (requerido para notificaciones):
```
https://hooks.slack.com/services/YOUR/WEBHOOK/URL
```

Para obtener el webhook:
1. Ir a https://api.slack.com/apps
2. Crear nueva app o seleccionar existente
3. Ir a "Incoming Webhooks"
4. Activar y crear webhook
5. Copiar la URL

### 2. GitHub Pages

El workflow despliega automáticamente los reportes a GitHub Pages. Para habilitar:

1. Ir a Settings → Pages
2. Source: "GitHub Actions"
3. Los reportes estarán en: `https://<usuario>.github.io/<repo>/test-reports/`

## Ejecución Local

### Prerequisitos

```bash
# Instalar Allure CLI
brew install allure  # macOS
# o descargar desde: https://github.com/allure-framework/allure2/releases
```

### Ejecutar Tests

```bash
# Ejecutar todos los tests
./gradlew connectedAndroidTest

# Ejecutar tests específicos
./gradlew connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.sdkqa.integration.SDKIntegrationTest

# Generar reporte Allure
allure generate app/build/outputs/androidTest-results/connected/allure-results -o allure-report --clean

# Abrir reporte
allure open allure-report
```

## Estructura del Workflow

### Job: test

1. **Setup**: JDK 11, Android SDK, Emulador
2. **Update SDK Version**: Actualiza la versión del SDK en build.gradle.kts
3. **Run Tests**: Ejecuta tests en emulador API 34
4. **Generate Allure Report**: Genera reporte HTML
5. **Upload Artifacts**: Sube resultados y reportes
6. **Extract Results**: Extrae estadísticas
7. **Notify Slack**: Envía notificación

### Job: deploy-report

1. **Download Reports**: Descarga todos los reportes
2. **Deploy to GitHub Pages**: Despliega a Pages

## Notificación de Slack

El mensaje incluye:
- 🟢/🔴 Status (PASSED/FAILED)
- Versión del SDK testeada
- Estadísticas (Total, Passed, Failed)
- Enlace al workflow run
- Enlace para descargar artifacts

**Formato del mensaje:**
```
🧪 Android Tests - SDK QA Suite

SDK Version: 9.6.0
Status: 🟢 PASSED
Total Tests: 15
Passed: 15
Failed: 0

Allure Report: [Download Artifact]
Workflow: [View Run]
```

## Matriz de Versiones

El workflow ejecuta tests en paralelo para:
- 9.6.0
- 9.7.0
- 9.8.0
- 9.9.1-alpha04

Para agregar más versiones, editar `.github/workflows/android_tests.yml`:

```yaml
matrix:
  sdk_version:
    - "9.6.0"
    - "9.7.0"
    - "9.8.0"
    - "9.9.1-alpha04"
    - "10.0.0"  # Nueva versión
```

## Troubleshooting

### Tests fallan en CI pero pasan localmente

- Verificar que el emulador tenga suficiente tiempo para inicializar
- Revisar logs del workflow para errores de timeout
- Verificar que la versión del SDK sea correcta

### Reportes no se generan

- Verificar que Allure CLI esté instalado en el workflow
- Revisar que los resultados estén en la ruta correcta
- Verificar permisos de escritura

### Slack no recibe notificaciones

- Verificar que el secret `SLACK_WEBHOOK_URL` esté configurado
- Verificar que el webhook esté activo
- Revisar logs del workflow para errores

### GitHub Pages no se actualiza

- Verificar que el workflow se ejecute en branch `main`
- Verificar permisos de GitHub Pages
- Revisar logs del job `deploy-report`

## Mejores Prácticas

1. **Siempre revisar los reportes de Allure** después de cambios
2. **Usar Allure.step()** para documentar pasos importantes
3. **Mantener las versiones del SDK actualizadas** en la matriz
4. **Revisar notificaciones de Slack** para detectar problemas rápidamente
5. **Usar artifacts** para descargar reportes completos

## Referencias

- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [Allure Framework](https://github.com/allure-framework/allure2)
- [Android Emulator Runner](https://github.com/reactivecircus/android-emulator-runner)
- [Slack API](https://api.slack.com/)
