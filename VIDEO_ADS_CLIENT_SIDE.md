# Video Ads Client-Side Activity - Documentación

## 📋 Descripción

Activity para probar anuncios client-side usando VAST/VMAP en el Mediastream SDK versión 10.0.0-alpha.01.

## 🎯 Tipos de Ads Implementados

### 1. **Pre+Mid+Post Roll**
- **URL:** `https://pubads.g.doubleclick.net/gampad/ads?iu=/21775744923/external/vmap_ad_samples&sz=640x480&cust_params=sample_ar%3Dpremidpost&ciu_szs=300x250&gdfp_req=1&ad_rule=1&output=vmap&unviewed_position_start=1&env=vp&cmsid=496&vid=short_onecue&correlator=`
- **Descripción:** Prueba ads pre-roll (antes del contenido), mid-roll (durante el contenido) y post-roll (después del contenido)
- **Objetivo:** Verificar que los anuncios se reproduzcan correctamente en todas las posiciones

### 2. **Redirect Error**
- **URL:** `https://pubads.g.doubleclick.net/gampad/ads?iu=/21775744923/external/single_ad_samples&sz=640x480&cust_params=sample_ct%3Dredirecterror&ciu_szs=300x250%2C728x90&gdfp_req=1&output=vast&unviewed_position_start=1&env=vp&correlator=`
- **Descripción:** Simula un error de redireccionamiento en la carga del ad
- **Objetivo:** Verificar el manejo de errores cuando un ad no puede redirigir correctamente

### 3. **Redirect Broken (Fallback)**
- **URL:** `https://pubads.g.doubleclick.net/gampad/ads?iu=/21775744923/external/single_ad_samples&sz=640x480&cust_params=sample_ct%3Dredirecterror&ciu_szs=300x250%2C728x90&gdfp_req=1&output=vast&unviewed_position_start=1&env=vp&nofb=1&correlator=`
- **Descripción:** Redireccionamiento interrumpido sin fallback (nofb=1)
- **Objetivo:** Verificar que el SDK maneja correctamente cuando no hay ads de respaldo

## 📱 Configuración del SDK

El activity usa el parámetro `adURL` (con URL en mayúsculas) en `MediastreamPlayerConfig`:

```kotlin
val config = MediastreamPlayerConfig()
config.id = "696bc8a832ce0ef08c6fa0ef"  // Mismo ID que VOD Simple
config.type = MediastreamPlayerConfig.VideoTypes.VOD

// 🟢 CONFIGURACIÓN DE ANUNCIOS CLIENT-SIDE (VAST/VMAP)
config.adURL = "URL_DEL_AD_CORRESPONDIENTE"
```

**Nota:** Se usa tipo VOD con el mismo contenido que `VideoVodSimpleActivity` para pruebas de ads client-side.

### Parámetros Opcionales de Ads

Si necesitas enviar parámetros personalizados a tu servidor de anuncios:

```kotlin
// Añadir parámetros personalizados (query params)
config.addAdCustomAttribute("genero", "accion")
config.addAdCustomAttribute("edad", "25")
config.addAdCustomAttribute("ppid", "USER_12345")
```

## 🎬 Callbacks Monitoreados

### Callbacks de Ads

#### `onAdEvents(AdEvent.AdEventType)`
Eventos de anuncios que se capturan:
- `LOADED` - Ad cargado correctamente
- `STARTED` - Ad comenzó a reproducirse
- `COMPLETED` - Ad completado exitosamente
- `SKIPPED` - Ad saltado por el usuario
- `PAUSED` - Ad pausado
- `RESUMED` - Ad reanudado
- `ALL_ADS_COMPLETED` - Todos los ads completados

#### `onAdErrorEvent(AdError)`
Errores de anuncios:
- Captura código de error y mensaje
- Muestra en UI para debugging

### Callbacks de Video Estándar

- `playerViewReady(isFullScreen)` - Player listo
- `onPlay()` - Contenido comenzó a reproducirse
- `onPause()` - Contenido pausado
- `onEnded()` - Contenido terminó
- `onError(errorType, errMessage)` - Error en el video
- `newTime(currentTime, totalTime)` - Actualización de tiempo

## 🖥️ Elementos de UI

### PlayerView
- Muestra el contenido de video y los anuncios
- Controles nativos de Media3

### Spinner (Selector de Tipo de Ad)
- Permite cambiar entre los 3 tipos de ads
- Al cambiar, recarga el player con la nueva configuración

### TextView: tvAdStatus
- Muestra el estado actual del ad
- Estados posibles:
  - `Waiting...` - Esperando carga
  - `Ad Loaded ✓` - Ad cargado
  - `Ad Playing ▶` - Ad reproduciéndose
  - `Ad Completed ✓` - Ad completado
  - `Ad Paused ⏸` - Ad pausado
  - `Ad Skipped ⏭` - Ad saltado
  - `All Ads Completed ✅` - Todos los ads completados
  - `Error ❌` - Error en ad
  - `Video Error` - Error en contenido

### TextView: tvAdEvents
- Muestra el último evento de ad recibido
- Incluye mensajes de error con código y descripción

## 📊 Métricas Rastreadas

| Evento | Descripción | UI Feedback |
|--------|-------------|-------------|
| `LOADED` | Ad cargado | Status verde ✓ |
| `STARTED` | Ad empezó | Status con ▶ |
| `COMPLETED` | Ad terminó | Status verde ✓ |
| `SKIPPED` | Ad saltado | Status con ⏭ |
| `PAUSED` | Ad pausado | Status con ⏸ |
| `RESUMED` | Ad reanudado | Status con ▶ |
| `ALL_ADS_COMPLETED` | Todos los ads completados | Status verde ✅ |
| `AdError` | Error de ad | Status rojo ❌ |
| `onError` | Error de video | Mensaje de error |

## 📝 Logging

Todos los eventos se loguean con tag `SDK-QA`:

```kotlin
Log.d("SDK-QA", "AD EVENT: $type")
Log.e("SDK-QA", "AD ERROR: ${error.errorCode} - ${error.message}")
Log.d("SDK-QA", "onPlay - Content playback started")
```

## 🚀 Uso

1. **Abrir la app** y seleccionar "Video: Ads Client-Side (VAST/VMAP)"
2. **Observar** el player y los indicadores de status
3. **Seleccionar** diferentes tipos de ads desde el spinner
4. **Verificar**:
   - Los ads se cargan correctamente
   - Los eventos se muestran en UI
   - Los errores se manejan correctamente
   - El contenido se reproduce después de los ads

## ✅ Checklist de Validación

### Pre+Mid+Post Roll
- [ ] Se reproduce el pre-roll antes del contenido
- [ ] Se reproduce el mid-roll durante el contenido
- [ ] Se reproduce el post-roll después del contenido
- [ ] El contenido se reproduce sin interrupciones
- [ ] El evento `ALL_ADS_COMPLETED` se dispara

### Redirect Error
- [ ] Se detecta el error de redireccionamiento
- [ ] Se muestra en UI el código de error
- [ ] El callback `onAdErrorEvent` se dispara
- [ ] El contenido puede reproducirse después del error

### Redirect Broken (Fallback)
- [ ] Se detecta que no hay fallback
- [ ] Se muestra el error apropiado
- [ ] El SDK maneja la situación sin crashear
- [ ] El contenido se reproduce (si es posible)

### Callbacks Generales
- [ ] `onAdEvents` se llama para cada evento de ad
- [ ] `onAdErrorEvent` se llama para errores de ad
- [ ] Los callbacks de video (`onPlay`, `onPause`, etc.) funcionan correctamente
- [ ] Los logs aparecen en Logcat con tag `SDK-QA`

## 🔍 Troubleshooting

### El ad no se reproduce
1. Verificar conexión a internet
2. Revisar Logcat para errores
3. Verificar que la URL del ad sea correcta
4. Confirmar que el SDK tiene permisos de internet

### No se ven eventos en UI
1. Verificar que los callbacks se estén llamando (Logcat)
2. Confirmar que `runOnUiThread` se use en actualizaciones de UI
3. Revisar que los TextViews estén correctamente inicializados

### Error al cambiar tipo de ad
1. Verificar que `changeContent()` se llame correctamente
2. Revisar que la configuración sea válida
3. Confirmar que el player no esté en un estado inválido

### Los ads se saltan automáticamente
- Es comportamiento esperado en algunas redes de ads
- Verificar el parámetro `skippable` en la URL del ad

## 📦 Archivos Relacionados

- **Activity:** `app/src/main/java/com/example/sdkqa/video/VideoAdsClientSideActivity.kt`
- **Layout:** `app/src/main/res/layout/activity_video_ads_client_side.xml`
- **TestCase:** `app/src/main/java/com/example/sdkqa/TestCase.kt`
- **MainActivity:** `app/src/main/java/com/example/sdkqa/MainActivity.kt`
- **AndroidManifest:** `app/src/main/AndroidManifest.xml`

## 🔗 Referencias

- [IMA SDK Documentation](https://developers.google.com/interactive-media-ads)
- [VAST/VMAP Specification](https://www.iab.com/guidelines/vast/)
- [Mediastream SDK Documentation](https://github.com/mediastream)
