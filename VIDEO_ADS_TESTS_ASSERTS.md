# 🧪 Tests de Video Ads Client-Side - Asserts Implementados

## 📋 Archivo de Test

**Ubicación:** `app/src/androidTest/java/com/example/sdkqa/video/VideoAdsClientSideActivityTest.kt`

---

## 🎯 8 Tests Implementados con 25+ Asserts

### **TEST 1: `testPlayerInitializationWithAds`**
Verifica que el player se inicializa correctamente con configuración de ads.

#### **Asserts:**
1. ✅ **Assert:** Actividad está visible
   ```kotlin
   onView(isRoot()).check(matches(isDisplayed()))
   ```

2. ✅ **Assert:** FrameLayout principal existe y es visible
   ```kotlin
   onView(withId(R.id.main_media_frame)).check(matches(isDisplayed()))
   ```

3. ✅ **Assert:** FrameLayout no es null
   ```kotlin
   assert(frameLayout != null) { "El FrameLayout debe existir" }
   ```

4. ✅ **Assert:** PlayerView existe dentro del layout
   ```kotlin
   assert(playerView != null) { "PlayerView debe existir" }
   ```

5. ✅ **Assert:** Player está asignado al PlayerView
   ```kotlin
   assert(playerView?.player != null) { "El PlayerView debe tener un player asignado" }
   ```

6. ✅ **Assert:** Logs confirman configuración de adURL
   ```kotlin
   assert(hasAdConfig) { "Los logs deben indicar que se configuró la URL de ads" }
   ```

---

### **TEST 2: `testAdTypeSelectorExists`**
Verifica que el selector de tipos de ads funciona.

#### **Asserts:**
7. ✅ **Assert:** Spinner es visible
   ```kotlin
   onView(withId(R.id.spinnerAdType)).check(matches(isDisplayed()))
   ```

8. ✅ **Assert:** Spinner es clickeable
   ```kotlin
   onView(withId(R.id.spinnerAdType)).check(matches(isClickable()))
   ```

9. ✅ **Assert:** Spinner no es null
   ```kotlin
   assert(spinner != null) { "El spinner debe existir" }
   ```

10. ✅ **Assert:** Spinner tiene exactamente 3 opciones
    ```kotlin
    assert(spinner.adapter.count == 3) { 
        "El spinner debe tener 3 opciones de ads. Actual: ${spinner.adapter.count}" 
    }
    ```

---

### **TEST 3: `testAdStatusTextViewsExist`**
Verifica que los TextViews de status de ads existen y son visibles.

#### **Asserts:**
11. ✅ **Assert:** TextView de ad status es visible
    ```kotlin
    onView(withId(R.id.tvAdStatus)).check(matches(isDisplayed()))
    ```

12. ✅ **Assert:** TextView de ad status contiene texto "Ad Status"
    ```kotlin
    onView(withId(R.id.tvAdStatus)).check(matches(withText(containsString("Ad Status"))))
    ```

13. ✅ **Assert:** TextView de ad events es visible
    ```kotlin
    onView(withId(R.id.tvAdEvents)).check(matches(isDisplayed()))
    ```

14. ✅ **Assert:** TextView de ad events contiene texto "Last Ad Event"
    ```kotlin
    onView(withId(R.id.tvAdEvents)).check(matches(withText(containsString("Last Ad Event"))))
    ```

---

### **TEST 4: `testPlayerViewReadyCallback`**
Verifica que el callback playerViewReady se ejecuta.

#### **Asserts:**
15. ✅ **Assert:** playerViewReady se ejecuta dentro de 15 segundos
    ```kotlin
    assert(playerViewReadyReceived) { 
        "El callback playerViewReady debe ejecutarse dentro de 15 segundos" 
    }
    ```

16. ✅ **Assert:** playerViewReady se llama al menos 1 vez
    ```kotlin
    assert(PlayerCallbackTracker.getPlayerViewReadyCallCount() > 0) {
        "Debe haber al menos 1 llamada a playerViewReady. Actual: ${...}"
    }
    ```

---

### **TEST 5: `testAdEventsInLogs`**
Verifica que se registran eventos de ads en logs.

#### **Asserts:**
17. ✅ **Assert:** Logs contienen eventos de ads
    ```kotlin
    assert(hasAdLogs) {
        "Los logs deben contener eventos o configuración de ads después de 20 segundos"
    }
    ```
    **Busca:**
    - `AD EVENT:` - Eventos de ads
    - `AD ERROR:` - Errores de ads
    - `Ad configuration set` - Configuración
    - `onAdEvents` - Callback de eventos
    - `onAdErrorEvent` - Callback de errores
    - `LOADED`, `STARTED`, `COMPLETED` - Estados de ads

---

### **TEST 6: `testAdTypeSwitch`**
Verifica que cambiar el tipo de ad reconfigura el player.

#### **Asserts:**
18. ✅ **Assert:** Nueva configuración de ad después del cambio
    ```kotlin
    assert(afterAdConfigCount > initialAdConfigCount) {
        "Debe haber una nueva configuración de ad después de cambiar el tipo"
    }
    ```

19. ✅ **Assert:** Logs mencionan el nuevo tipo de ad seleccionado
    ```kotlin
    assert(hasRedirectErrorConfig) {
        "Los logs deben mencionar 'Redirect Error' después del cambio"
    }
    ```

---

### **TEST 7: `testContentPlaybackAfterAds`**
Verifica que el contenido se reproduce después de los ads.

#### **Asserts:**
20. ✅ **Assert:** PlayerView existe después de 30 segundos
    ```kotlin
    assert(playerView != null) { "PlayerView debe existir" }
    ```

21. ✅ **Assert:** Player está en estado válido (no idle, no error)
    ```kotlin
    assert(isValidState) {
        "El player debe estar en un estado válido después de 30 segundos"
    }
    ```

22. ✅ **Assert:** Player no es null
    ```kotlin
    playerView?.player?.let { ... } ?: throw AssertionError("Player no debe ser null")
    ```

23. ✅ **Assert:** Logs confirman reproducción de contenido
    ```kotlin
    assert(hasPlaybackLogs) { "Debe haber logs de reproducción de contenido" }
    ```
    **Busca:** `onPlay`, `onReady`, `Content playback`

---

### **TEST 8: `testNoAdRelatedCrashes`**
Verifica que no hay crashes durante la reproducción de ads.

#### **Asserts:**
24. ✅ **Assert:** Actividad sigue viva después de 25 segundos
    ```kotlin
    onView(isRoot()).check(matches(isDisplayed()))
    ```

25. ✅ **Assert:** PlayerView sigue existiendo
    ```kotlin
    assert(playerView != null) { 
        "PlayerView debe seguir existiendo después de 25 segundos" 
    }
    ```

26. ✅ **Assert:** Player sigue existiendo
    ```kotlin
    assert(playerView?.player != null) { 
        "Player debe seguir existiendo después de 25 segundos" 
    }
    ```

27. ✅ **Assert:** No hay errores críticos en logs
    ```kotlin
    assert(!hasCriticalErrors) { 
        "No debe haber errores críticos durante la reproducción de ads" 
    }
    ```
    **Busca:** `FATAL`, `RuntimeException`, `NullPointerException`

---

## 📊 Resumen de Asserts por Categoría

| Categoría | Cantidad | Descripción |
|-----------|----------|-------------|
| **UI Visibility** | 6 | Verifican que elementos UI son visibles |
| **Component Existence** | 8 | Verifican que componentes existen |
| **Functional Behavior** | 5 | Verifican comportamiento funcional |
| **Log Verification** | 5 | Verifican eventos en logs |
| **State Validation** | 3 | Verifican estados del player |

**Total: 27 Asserts** distribuidos en 8 tests

---

## 🎯 Eventos de Ads que se Verifican

### **Eventos Esperados (AdEvent.AdEventType):**
- ✅ `LOADED` - Ad cargado
- ✅ `STARTED` - Ad comenzó
- ✅ `COMPLETED` - Ad completado
- ✅ `SKIPPED` - Ad saltado
- ✅ `PAUSED` - Ad pausado
- ✅ `RESUMED` - Ad reanudado
- ✅ `ALL_ADS_COMPLETED` - Todos los ads completados

### **Errores de Ads (AdError):**
- ✅ Errores de redireccionamiento
- ✅ Errores de carga
- ✅ Errores de red
- ✅ Cualquier error reportado por IMA SDK

---

## 🔍 Estrategia de Verificación

### **1. Verificación por UI**
```kotlin
// Verificar elementos visuales
onView(withId(R.id.tvAdStatus))
    .check(matches(isDisplayed()))
    .check(matches(withText(containsString("Ad Status"))))
```

### **2. Verificación por Logs**
```kotlin
// Capturar y analizar logs
val logs = LogcatCapture.getRecentLogs(100)
val hasAdEvents = logs.any { it.contains("AD EVENT:") }
assert(hasAdEvents) { "Debe haber eventos de ads en logs" }
```

### **3. Verificación por Callbacks**
```kotlin
// Usar PlayerCallbackTracker
val playerViewReadyReceived = PlayerCallbackTracker.waitForPlayerViewReady(15)
assert(playerViewReadyReceived) { "playerViewReady debe ejecutarse" }
```

### **4. Verificación por Estado del Player**
```kotlin
// Verificar estado directo del player
playerView?.player?.let { player ->
    assert(player.playbackState != Player.STATE_IDLE) {
        "Player debe estar en estado válido"
    }
}
```

---

## ⏱️ Timeouts Usados

| Test | Timeout | Razón |
|------|---------|-------|
| Inicialización | 3s | Player setup |
| Ad Events | 20s | Carga y reproducción de ads |
| Content Playback | 30s | Ads + contenido |
| Stability | 25s | Detección de crashes |
| PlayerViewReady | 15s | Callback inicial |

---

## 🧪 Cómo Ejecutar los Tests

### **Opción 1: Todos los tests**
```bash
./gradlew connectedAndroidTest --tests VideoAdsClientSideActivityTest
```

### **Opción 2: Test específico**
```bash
./gradlew connectedAndroidTest --tests VideoAdsClientSideActivityTest.testAdEventsInLogs
```

### **Opción 3: Desde Android Studio**
1. Abrir `VideoAdsClientSideActivityTest.kt`
2. Click derecho en la clase o método
3. Seleccionar "Run 'VideoAdsClientSideActivityTest'"

---

## 📝 Qué Hacer Si un Test Falla

### **Test 1-3 Fallan (UI):**
- Verificar que los IDs en el layout XML sean correctos
- Verificar que la activity se inicia correctamente

### **Test 4 Falla (Callbacks):**
- Verificar que PlayerCallbackTracker esté funcionando
- Revisar si el callback se registra correctamente

### **Test 5 Falla (Ad Events):**
- Verificar conexión a internet
- Verificar que las URLs de ads sean válidas
- Revisar logs para ver si hay errores de red

### **Test 6 Falla (Ad Switch):**
- Verificar que el spinner funciona correctamente
- Verificar que `reloadPlayer()` se llama

### **Test 7 Falla (Content Playback):**
- Verificar que el contenido VOD existe
- Verificar que hay contenido después de ads

### **Test 8 Falla (Crashes):**
- Revisar logs completos para encontrar la causa del crash
- Verificar memory leaks

---

## ✅ Criterios de Éxito

Un test pasa si:
- ✅ Todos los asserts son verdaderos
- ✅ No hay excepciones no manejadas
- ✅ No hay crashes durante la ejecución
- ✅ Los timeouts no se exceden

---

**Fecha:** 2026-01-30  
**Tests Totales:** 8  
**Asserts Totales:** 27  
**Cobertura:** Inicialización, UI, Ads, Callbacks, Errores, Estabilidad
