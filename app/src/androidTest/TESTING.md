# Guía de Pruebas Automatizadas - QA Suite SDK

## Descripción General

Este directorio contiene pruebas automatizadas usando Espresso para validar el funcionamiento del MediastreamPlayer en diferentes escenarios. Las pruebas están diseñadas para ser escalables y reutilizables.

## Estructura

```
androidTest/
├── java/com/example/sdkqa/
│   ├── audio/
│   │   └── AudioLiveActivityTest.kt    # Pruebas para AudioLiveActivity
│   └── testutils/
│       ├── PlayerCallbackTracker.kt    # Utilidad para rastrear callbacks
│       └── TestablePlayerCallback.kt    # Wrapper de callback para pruebas
```

## Componentes Principales

### PlayerCallbackTracker

Utilidad singleton que rastrea la ejecución de callbacks del player. Permite:
- Contar cuántas veces se ha llamado cada callback
- Verificar el estado actual del player (reproduciendo/pausado)
- Esperar a que se ejecuten callbacks específicos

**Uso:**
```kotlin
// Reiniciar antes de cada prueba
PlayerCallbackTracker.reset()

// Verificar que se llamó onPlay
assert(PlayerCallbackTracker.getPlayCallCount() > 0)

// Esperar a que se ejecute onReady
PlayerCallbackTracker.waitForReady(10) // timeout en segundos
```

### TestablePlayerCallback

Wrapper del `MediastreamPlayerCallback` que registra eventos en el `PlayerCallbackTracker` mientras mantiene la funcionalidad original del callback.

## Pruebas Implementadas

### AudioLiveActivityTest

Conjunto de pruebas para `AudioLiveActivity` que verifican:

1. **testPlayerInitialization**: Verifica que el FrameLayout y PlayerView se crean correctamente
2. **testMediastreamPlayerCreation**: Valida que el MediastreamPlayer se inicializa y asigna al PlayerView
3. **testPlayButtonClick**: Simula clic en play y verifica que el player responde
4. **testPauseButtonClick**: Simula clic en pause y verifica que el player se pausa
5. **testPlayPauseSequence**: Prueba una secuencia completa play -> pause -> play
6. **testPlayerViewVisualChanges**: Verifica cambios visuales en el PlayerView
7. **testPlayPauseCallbacks**: Verifica específicamente que los callbacks onPlay/onPause se ejecutan

## Ejecutar las Pruebas

### Desde Android Studio

1. Abre el proyecto en Android Studio
2. Navega a `app/src/androidTest/java/com/example/sdkqa/audio/AudioLiveActivityTest.kt`
3. Haz clic derecho en la clase o método de prueba
4. Selecciona "Run 'AudioLiveActivityTest'"

### Desde la línea de comandos

```bash
./gradlew connectedAndroidTest
```

Para ejecutar una prueba específica:
```bash
./gradlew connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.sdkqa.audio.AudioLiveActivityTest#testPlayerInitialization
```

## Extender las Pruebas

### Agregar Pruebas para Otra Activity

1. Crea una nueva clase de prueba siguiendo el patrón de `AudioLiveActivityTest`
2. Usa `PlayerCallbackTracker` para rastrear callbacks
3. Inyecta `TestablePlayerCallback` usando reflection o modificando la Activity para aceptar callbacks de prueba

**Ejemplo:**
```kotlin
@RunWith(AndroidJUnit4::class)
@LargeTest
class AudioAodActivityTest {
    @get:Rule
    val activityRule = ActivityScenarioRule(AudioAodActivity::class.java)
    
    @Test
    fun testPlayerInitialization() {
        // Tu prueba aquí
    }
}
```

### Agregar Nuevos Callbacks al Tracker

1. Agrega un contador en `PlayerCallbackTracker`:
```kotlin
private val onEndCallCount = AtomicInteger(0)
```

2. Agrega métodos para incrementar y obtener el contador:
```kotlin
fun onEnd() {
    onEndCallCount.incrementAndGet()
}

fun getOnEndCallCount(): Int = onEndCallCount.get()
```

3. Actualiza `TestablePlayerCallback` para llamar al tracker:
```kotlin
override fun onEnd() {
    PlayerCallbackTracker.onEnd()
    originalCallback?.onEnd()
}
```

## Mejores Prácticas

1. **Siempre reinicia el tracker** en `@Before` para evitar interferencia entre pruebas
2. **Usa timeouts apropiados** al esperar callbacks (10-15 segundos para operaciones de red)
3. **Verifica el estado del player** además de los callbacks para mayor confiabilidad
4. **Usa `@LargeTest`** para pruebas que requieren red o tiempo de inicialización
5. **Documenta casos edge** y comportamientos esperados en comentarios

## Troubleshooting

### Los callbacks no se están registrando

- Verifica que `TestablePlayerCallback` se haya inyectado correctamente
- Asegúrate de que `PlayerCallbackTracker.reset()` se llame antes de cada prueba
- Revisa los logs para ver si hay errores de reflection

### Las pruebas fallan por timeout

- Aumenta los timeouts en `waitFor*` methods
- Verifica que la conexión de red esté disponible
- Asegúrate de que el content ID sea válido

### El PlayerView no se encuentra

- Verifica que la Activity haya terminado de inicializarse (usa `Thread.sleep()` o mejor, espera a callbacks)
- Asegúrate de que el layout se haya cargado completamente

## Próximos Pasos

- [ ] Agregar pruebas para otras Activities (AudioAodActivity, VideoLiveActivity, etc.)
- [ ] Implementar Idling Resources para esperas más robustas
- [ ] Agregar pruebas de integración con servicios
- [ ] Crear mocks para contenido de prueba
- [ ] Implementar pruebas de UI más complejas (seek, fullscreen, etc.)
