# Guía de Reportes y Captura de Información en Tests

## Descripción

Este sistema captura automáticamente información valiosa cuando los tests fallan:
- **Screenshots**: Capturas de pantalla automáticas en fallos
- **Logcat**: Logs del SDK y errores de Android
- **Reportes XML/HTML**: Generados automáticamente por JUnit/Espresso

## Componentes

### 1. TestFailureWatcher

`TestFailureWatcher` es una regla de JUnit que se ejecuta automáticamente y captura información cuando un test falla.

**Uso:**
```kotlin
@get:Rule
val failureWatcher = TestFailureWatcher()
```

**Qué hace automáticamente:**
- Inicia captura de logs al comenzar cada test
- Captura screenshot cuando un test falla
- Filtra y muestra logs de errores relevantes
- Detiene captura de logs al finalizar el test

### 2. ScreenshotCapture

Utilidad para capturar screenshots manualmente o automáticamente.

**Uso manual:**
```kotlin
// Capturar screenshot completa
ScreenshotCapture.capture("testPlayPauseCallbacks", "before_assert")

// Capturar screenshot de una vista específica
val playerView = findPlayerViewInHierarchy(frameLayout)
ScreenshotCapture.captureView(playerView, "testPlayPauseCallbacks", "player_view")
```

**Ubicación de screenshots:**
```
/storage/emulated/0/Android/data/com.example.sdkqa/files/test_screenshots/
```

### 3. LogcatCapture

Utilidad para capturar logs durante los tests, especialmente útil para errores del SDK.

**Uso manual:**
```kotlin
// Iniciar captura
LogcatCapture.startCapture("testPlayPauseCallbacks")

// Obtener logs de errores
val errorLogs = LogcatCapture.getErrorLogs()

// Obtener logs específicos del SDK
val sdkLogs = LogcatCapture.getLogsByTag("SDK-QA")
val mediastreamLogs = LogcatCapture.getLogsByTag("MediastreamPlayer")

// Detener captura
LogcatCapture.stopCapture()
```

**Tags capturados automáticamente:**
- `SDK-QA:*` - Logs de nuestra aplicación
- `MediastreamPlayer:*` - Logs del SDK Mediastream
- `ExoPlayer:*` - Logs de Media3/ExoPlayer
- `Media3:*` - Logs de Media3
- `AndroidRuntime:E` - Errores de Android
- `*:E` - Todos los errores

**Ubicación de logs:**
```
/storage/emulated/0/Android/data/com.example.sdkqa/files/test_logs/
```

## Ubicación de Archivos

### Screenshots
```
/storage/emulated/0/Android/data/com.example.sdkqa/files/test_screenshots/
screenshot_testPlayPauseCallbacks_failure_20260118_095253_123.png
```

### Logs
```
/storage/emulated/0/Android/data/com.example.sdkqa/files/test_logs/
logcat_testPlayPauseCallbacks_20260118_095253_123.txt
```

### Reportes JUnit XML
```
app/build/outputs/androidTest-results/connected/
  - TEST-*.xml
  - test-results.xml
```

### Reportes HTML (si se configura)
```
app/build/reports/androidTests/connected/
  - index.html
```

## Acceder a los Archivos

### Desde Android Studio

1. **Screenshots y Logs:**
   - Abre Device File Explorer (View → Tool Windows → Device File Explorer)
   - Navega a `/storage/emulated/0/Android/data/com.example.sdkqa/files/`
   - Descarga los archivos necesarios

2. **Reportes XML:**
   - Abre Build → Test Results
   - O navega a `app/build/outputs/androidTest-results/connected/`

### Desde ADB

```bash
# Listar screenshots
adb shell ls -la /storage/emulated/0/Android/data/com.example.sdkqa/files/test_screenshots/

# Descargar screenshot
adb pull /storage/emulated/0/Android/data/com.example.sdkqa/files/test_screenshots/screenshot_testPlayPauseCallbacks_failure_20260118_095253_123.png

# Listar logs
adb shell ls -la /storage/emulated/0/Android/data/com.example.sdkqa/files/test_logs/

# Descargar log
adb pull /storage/emulated/0/Android/data/com.example.sdkqa/files/test_logs/logcat_testPlayPauseCallbacks_20260118_095253_123.txt

# Ver log en tiempo real
adb logcat -s SDK-QA:* MediastreamPlayer:* *:E
```

## Información Capturada Automáticamente

Cuando un test falla, `TestFailureWatcher` automáticamente:

1. **Captura screenshot** del estado actual de la pantalla
2. **Filtra y muestra logs de errores** relevantes:
   - Errores de Android (`AndroidRuntime:E`)
   - Errores del SDK (`onError`, `onPlaybackErrors`, `onEmbedErrors`)
   - Logs de nuestra app (`SDK-QA:*`)
   - Logs de MediastreamPlayer
3. **Guarda logs completos** en archivo para análisis posterior

## Ejemplo de Salida en Logcat

Cuando un test falla, verás algo como:

```
E/TestFailureWatcher: Test falló: testPlayPauseCallbacks
D/TestFailureWatcher: Screenshot de fallo guardada: /storage/emulated/0/Android/data/com.example.sdkqa/files/test_screenshots/screenshot_testPlayPauseCallbacks_failure_20260118_095253_123.png
D/TestFailureWatcher: Errores encontrados en logs (3):
E/TestFailureWatcher:   01-18 09:52:53.123 E/MediastreamPlayer: onError: Network error
E/TestFailureWatcher:   01-18 09:52:53.456 E/AndroidRuntime: FATAL EXCEPTION: main
D/TestFailureWatcher: Logs del SDK (15):
D/TestFailureWatcher:   01-18 09:52:53.789 D/SDK-QA: onPlay
D/TestFailureWatcher:   01-18 09:52:53.790 D/SDK-QA: onBuffering
```

## Limpieza Automática

Los archivos antiguos (más de 7 días) se limpian automáticamente al iniciar los tests para evitar llenar el almacenamiento.

## Mejores Prácticas

1. **Siempre incluye `TestFailureWatcher`** en tus tests
2. **Revisa los logs** cuando un test falle para entender el contexto
3. **Compara screenshots** de diferentes ejecuciones para detectar cambios visuales
4. **Filtra logs por tag** cuando busques errores específicos del SDK
5. **Guarda reportes XML** para análisis histórico

## Troubleshooting

### Las screenshots no se capturan

- Verifica que el permiso de almacenamiento esté concedido
- Revisa que UiAutomator esté disponible (requiere Android 4.3+)
- Verifica los logs para ver errores específicos

### Los logs están vacíos

- Verifica que los tags estén correctos
- Asegúrate de que `LogcatCapture.startCapture()` se haya llamado
- Revisa que el proceso de logcat se haya iniciado correctamente

### No puedo acceder a los archivos

- Verifica permisos de almacenamiento en el dispositivo
- Usa `adb shell` para verificar que los archivos existen
- Asegúrate de que la app tenga permisos de escritura
