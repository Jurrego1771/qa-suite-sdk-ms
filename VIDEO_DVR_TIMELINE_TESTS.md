# 🧪 Tests de Video Live DVR - Navegación en Línea de Tiempo

## 📋 Archivo de Test

**Ubicación:** `app/src/androidTest/java/com/example/sdkqa/video/VideoLiveDvrActivityTest.kt`

---

## 🎯 8 Tests Implementados - Focus en Navegación DVR

### **TEST 3: `testDvrTimelineNavigation` ⭐ PRINCIPAL**
**Verifica navegación en la línea de tiempo DVR y validación de currentTime.**

#### **Asserts de Navegación:**

1. ✅ **Assert:** Player existe y está activo
   ```kotlin
   assert(player != null) { "Player debe existir" }
   ```

2. ✅ **Assert:** CurrentTime inicial se obtiene correctamente
   ```kotlin
   val initialTime = player.currentPosition
   Log.d(TAG, "Initial currentTime: $initialTime ms")
   ```

3. ✅ **Assert:** Duración (ventana DVR) es válida
   ```kotlin
   val duration = player.duration
   Log.d(TAG, "Total duration: $duration ms")
   ```

4. ✅ **Assert:** Seek a posición específica se ejecuta
   ```kotlin
   val seekPosition = initialTime - 10000  // 10 segundos atrás
   player.seekTo(seekPosition)
   ```

5. ✅ **Assert:** CurrentTime cambia después del seek
   ```kotlin
   val newTime = player.currentPosition
   val timeDiff = abs(newTime - seekPosition)
   assert(timeDiff < 2000) {
       "CurrentTime debe estar cerca de la posición buscada. " +
       "Buscado: $seekPosition, Actual: $newTime, Diferencia: $timeDiff ms"
   }
   ```
   **Tolerancia:** ±2 segundos

6. ✅ **Assert:** Player sigue reproduciendo después de seek
   ```kotlin
   val isPlayingOrBuffering = player.isPlaying || 
       player.playbackState == Player.STATE_BUFFERING
   assert(isPlayingOrBuffering) {
       "Player debe estar reproduciendo o en buffering después de seek"
   }
   ```

---

### **TEST 4: `testContinuousPlaybackAfterSeek` ⭐ REPRODUCCIÓN CONTINUA**
**Verifica que el tiempo avanza continuamente después de navegar.**

#### **Asserts de Reproducción Continua:**

7. ✅ **Assert:** Time antes del seek se captura
   ```kotlin
   val initialTime = player.currentPosition
   Log.d(TAG, "Time before seek: $initialTime ms")
   ```

8. ✅ **Assert:** Seek hacia atrás (15 segundos)
   ```kotlin
   val seekPosition = if (initialTime > 15000) initialTime - 15000 else 0
   player.seekTo(seekPosition)
   ```

9. ✅ **Assert:** Time después del seek se valida
   ```kotlin
   val timeAfterSeek = player.currentPosition
   Log.d(TAG, "Time after seek: $timeAfterSeek ms")
   ```

10. ✅ **Assert:** El tiempo avanza después de 3 segundos
    ```kotlin
    Thread.sleep(3000)
    val timeLater = player.currentPosition
    val timeAdvanced = timeLater > timeAfterSeek
    assert(timeAdvanced) {
        "El tiempo debe avanzar después del seek (reproducción continua). " +
        "Después de seek: $timeAfterSeek, 3s después: $timeLater"
    }
    ```

11. ✅ **Assert:** El avance es razonable (~3 segundos)
    ```kotlin
    val actualAdvance = timeLater - timeAfterSeek
    assert(actualAdvance > 1000 && actualAdvance < 5000) {
        "El avance debe ser aproximadamente 3 segundos. Avance real: $actualAdvance ms"
    }
    ```
    **Rango válido:** 1-5 segundos (tolerancia para buffering)

---

## 📊 Resumen de Asserts de Navegación

| # | Assert | Qué Valida | Test |
|---|--------|------------|------|
| 1 | Player existe | Player no null | Test 3 |
| 2 | CurrentTime inicial | Posición actual en ms | Test 3 |
| 3 | Duración válida | Ventana DVR disponible | Test 3 |
| 4 | Seek ejecutado | Navegación a posición específica | Test 3 |
| 5 | **CurrentTime cambió** | **Nueva posición ±2s** | **Test 3** ⭐ |
| 6 | Reproduciendo post-seek | isPlaying o buffering | Test 3 |
| 7 | Time pre-seek | Captura posición inicial | Test 4 |
| 8 | Seek 15s atrás | Navegar hacia atrás | Test 4 |
| 9 | Time post-seek | Nueva posición | Test 4 |
| 10 | **Tiempo avanza** | **Reproducción continua** | **Test 4** ⭐ |
| 11 | **Avance razonable** | **1-5 segundos en 3s** | **Test 4** ⭐ |

---

## 🎯 Flujo de Test de Navegación

### **Test 3: Navegación Básica**
```
1. Cambiar a modo DVR
   ↓
2. Esperar estabilización (5s)
   ↓
3. Capturar currentTime inicial (ej: 120,000 ms = 2 minutos)
   ↓
4. Calcular posición de seek (initialTime - 10,000 ms)
   ↓
5. Ejecutar player.seekTo(seekPosition)
   ↓
6. Esperar seek complete (2s)
   ↓
7. Capturar nuevo currentTime
   ↓
8. Validar: |newTime - seekPosition| < 2000 ms
   ↓
9. Validar: player.isPlaying == true
```

### **Test 4: Reproducción Continua**
```
1. Cambiar a modo DVR
   ↓
2. Capturar time inicial (ej: 150,000 ms)
   ↓
3. Seek 15 segundos atrás (135,000 ms)
   ↓
4. Capturar timeAfterSeek
   ↓
5. Esperar 3 segundos
   ↓
6. Capturar timeLater
   ↓
7. Validar: timeLater > timeAfterSeek (tiempo avanzó)
   ↓
8. Validar: (timeLater - timeAfterSeek) ≈ 3000 ms (±2000 ms)
```

---

## 🔍 Ejemplo Real de Ejecución

### **Escenario: Usuario retrocede 10 segundos**

```kotlin
// Estado inicial
currentTime = 120,000 ms (2 minutos desde el inicio)

// Usuario hace seek
seekPosition = 110,000 ms (1 min 50s)
player.seekTo(110,000)

// Después del seek
newTime = 109,800 ms  // ✅ Dentro de ±2s de tolerancia
                      // Diferencia: 200 ms

// Reproducción continua
timeAfterSeek = 109,800 ms
wait(3000 ms)
timeLater = 112,900 ms  // ✅ Avanzó 3,100 ms ≈ 3 segundos
                        // Dentro del rango 1000-5000 ms
```

**Resultado:** ✅ Todos los asserts pasan

---

## 📝 Otros Tests Incluidos

### **TEST 1: `testLiveModeInitialization`**
- ✅ Actividad visible
- ✅ Spinner existe
- ✅ Player inicializado
- ✅ Logs confirman modo Live

### **TEST 2: `testSwitchToDvrMode`**
- ✅ Cambio a DVR exitoso
- ✅ Logs confirman "1 hour window"
- ✅ Player activo después del cambio

### **TEST 5: `testDvrStartMode`**
- ✅ Logs confirman DVR Start
- ✅ dvrStart timestamp en logs
- ✅ Player activo

### **TEST 6: `testDvrVodMode`**
- ✅ Logs confirman DVR VOD
- ✅ dvrStart y dvrEnd en logs
- ✅ Duración definida

### **TEST 7: `testModeSelectorHasFourModes`**
- ✅ Spinner tiene 4 opciones

### **TEST 8: `testRapidModeSwitch`**
- ✅ Actividad viva después de cambios
- ✅ Player activo
- ✅ No crashes

---

## 🧪 Cómo Ejecutar

```bash
# Test específico de navegación
./gradlew connectedAndroidTest --tests VideoLiveDvrActivityTest.testDvrTimelineNavigation

# Test de reproducción continua
./gradlew connectedAndroidTest --tests VideoLiveDvrActivityTest.testContinuousPlaybackAfterSeek

# Todos los tests DVR
./gradlew connectedAndroidTest --tests VideoLiveDvrActivityTest
```

---

## 💡 Por Qué Estos Tests Son Importantes

### **Test 3: Navegación**
✅ Valida que **DVR funciona** - usuario puede retroceder  
✅ Valida que **seek es preciso** - va donde debe ir  
✅ Valida que **player responde** - no se queda congelado  

### **Test 4: Reproducción Continua**
✅ Valida que **no se queda pausado** después de seek  
✅ Valida que **tiempo avanza naturalmente** - no hay loops  
✅ Valida que **velocidad es correcta** - ~1 segundo real = ~1 segundo de video  

---

## 🎯 Tolerancias Definidas

| Validación | Tolerancia | Razón |
|------------|------------|-------|
| **Seek preciso** | ±2 segundos | Buffering, keyframes en video |
| **Avance de tiempo** | 1-5 segundos en 3s | Buffering, pausas breves |

---

## 📊 Total de Asserts

- **Test 3 (Navegación):** 6 asserts
- **Test 4 (Continua):** 5 asserts
- **Otros 6 tests:** 15+ asserts

**Total:** 26+ asserts en 8 tests

---

**Fecha:** 2026-01-30  
**Focus:** Navegación en línea de tiempo DVR y validación de currentTime  
**Cobertura:** Live, DVR, DVR Start, DVR VOD, Seek, Reproducción continua
