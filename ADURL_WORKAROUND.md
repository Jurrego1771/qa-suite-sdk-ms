# ✅ RESUELTO: adURL Sí Está Disponible en SDK 10.0.0-alpha.01

**ACTUALIZACIÓN:** El parámetro correcto es `adURL` (con URL en mayúsculas), no `adUrl`.

---

# ~~Solución: adUrl No Disponible en SDK 10.0.0-alpha.01~~ (OBSOLETO)

## ✅ **SOLUCIÓN FINAL**

El parámetro **SÍ existe** en el SDK, simplemente se llama `adURL` (mayúsculas) en lugar de `adUrl`:

```kotlin
val config = MediastreamPlayerConfig()
config.id = "696bc8a832ce0ef08c6fa0ef"
config.type = MediastreamPlayerConfig.VideoTypes.VOD

// ✅ CORRECTO: adURL (con URL en mayúsculas)
config.adURL = "https://pubads.g.doubleclick.net/gampad/ads?..."
```

**Documentación oficial confirma:**
- Propiedad: `adURL` (String)
- Método alternativo: `addAdCustomAttribute(key, value)` para parámetros personalizados
- El SDK prioriza `config.adURL` sobre la configuración de la plataforma

---

## 🔍 **~~Problema~~** (YA NO ES UN PROBLEMA)

El parámetro `adUrl` no está disponible como propiedad pública en `MediastreamPlayerConfig` en el SDK 10.0.0-alpha.01, aunque está documentado en la versión 9.x del SDK.

**Error:**
```
e: Unresolved reference 'adUrl'.
```

**Código que falla:**
```kotlin
MediastreamPlayerConfig().apply {
    id = VIDEO_CONTENT_ID
    type = MediastreamPlayerConfig.VideoTypes.LIVE
    this.adUrl = adUrl  // ❌ Error: Unresolved reference
}
```

---

## ✅ **Solución Implementada**

Usar reflexión de Java para acceder al campo `adUrl` si existe internamente en la clase:

```kotlin
private fun createConfigForAdType(adType: String): MediastreamPlayerConfig {
    val adUrlValue = when (adType) {
        "Pre+Mid+Post Roll" -> AD_URL_PRE_MID_POST
        "Redirect Error" -> AD_URL_REDIRECT_ERROR
        "Redirect Broken (Fallback)" -> AD_URL_REDIRECT_BROKEN
        else -> AD_URL_PRE_MID_POST
    }

    val config = MediastreamPlayerConfig()
    config.id = VIDEO_CONTENT_ID
    config.type = MediastreamPlayerConfig.VideoTypes.LIVE
    
    // Intentar configurar adUrl usando reflexión
    try {
        val field = config.javaClass.getDeclaredField("adUrl")
        field.isAccessible = true
        field.set(config, adUrlValue)
        Log.d(TAG, "adUrl set successfully via reflection: $adUrlValue")
    } catch (e: Exception) {
        Log.w(TAG, "Could not set adUrl: ${e.message}")
        // Si no funciona, los ads configurados en la plataforma se usarán
    }
    
    return config
}
```

---

## 🎯 **Cómo Funciona**

1. **Crear configuración base** con `id` y `type`
2. **Intentar acceder al campo** `adUrl` usando reflexión
3. **Hacer el campo accesible** con `field.isAccessible = true`
4. **Setear el valor** con `field.set(config, adUrlValue)`
5. **Manejo de errores**: Si falla, loguear y continuar con ads de plataforma

---

## 📊 **Escenarios**

### ✅ **Escenario 1: adUrl existe internamente**
- La reflexión accede al campo exitosamente
- Se configura la URL del ad
- Los ads client-side funcionan correctamente
- Log: `"adUrl set successfully via reflection: [URL]"`

### ⚠️ **Escenario 2: adUrl no existe**
- La reflexión falla con `NoSuchFieldException`
- Se captura la excepción
- Se loguea una advertencia
- El player usará los ads configurados en Mediastream Platform
- Log: `"Could not set adUrl: [error message]"`

---

## 🔄 **Alternativas Consideradas**

### **1. Esperar a SDK estable** ❌
- Requiere esperar a nueva versión
- No cumple con el requisito de QA de la versión alpha

### **2. Usar método setAdUrl()** ❌
- El método no existe en la interfaz pública
- Similar problema al de la propiedad

### **3. Usar reflexión** ✅ **IMPLEMENTADO**
- Funciona si el campo existe internamente
- Fail-safe: no rompe si el campo no existe
- Permite probar la funcionalidad en versión alpha

### **4. No configurar adUrl** ❌
- No permite probar ads client-side específicos
- Solo usaría ads de plataforma
- No cumple con el requerimiento del usuario

---

## 📝 **Consideraciones**

### **Pros**
- ✅ Permite probar ads client-side en versión alpha
- ✅ No rompe si la propiedad no existe
- ✅ Loguea claramente el resultado
- ✅ Fallback automático a ads de plataforma

### **Cons**
- ⚠️ Uso de reflexión (menos performante)
- ⚠️ Puede no funcionar en futuras versiones
- ⚠️ Puede no funcionar si el campo cambia de nombre

### **Recomendaciones**
1. **Reportar a Mediastream** que `adUrl` no está disponible públicamente en SDK 10.x
2. **Actualizar cuando SDK esté estable** y `adUrl` esté disponible públicamente
3. **Monitorear logs** para ver si la reflexión funciona correctamente

---

## 🧪 **Testing**

### **Para Verificar si Funciona:**
1. Ejecutar la app
2. Abrir "Video: Ads Client-Side (VAST/VMAP)"
3. Revisar Logcat con filtro `SDK-QA`
4. Buscar uno de estos mensajes:
   - ✅ `"adUrl set successfully via reflection: [URL]"` - Funciona
   - ⚠️ `"Could not set adUrl: ..."` - No funciona, usando ads de plataforma

### **Para Verificar Ads Client-Side:**
1. Si el log muestra "adUrl set successfully":
   - Los ads deberían reproducirse según la URL configurada
   - Pre+Mid+Post Roll: 3 ads en diferentes posiciones
   - Redirect Error: Debería mostrar error de ad
   - Redirect Broken: Debería fallar sin fallback
2. Si el log muestra "Could not set adUrl":
   - Se reproducirán los ads configurados en Mediastream Platform
   - Los callbacks de ads seguirán funcionando
   - Solo no serán las URLs específicas de prueba

---

## 📚 **Documentación Relevante**

Según la documentación oficial (dicsdk.md línea 218):
```
- adUrl (String): AdURL (e.g., VAST). If not specified, will play ads 
  configured in the Mediastream Platform.
```

Esta propiedad está documentada pero no está disponible públicamente en la versión alpha.

---

## 🔮 **Plan Futuro**

Una vez que el SDK 10.x esté estable y `adUrl` esté disponible públicamente, reemplazar:

```kotlin
// Código temporal con reflexión
try {
    val field = config.javaClass.getDeclaredField("adUrl")
    field.isAccessible = true
    field.set(config, adUrlValue)
} catch (e: Exception) {
    Log.w(TAG, "Could not set adUrl: ${e.message}")
}
```

Por código directo:

```kotlin
// Código futuro cuando adUrl esté disponible
config.adUrl = adUrlValue
```

---

**Fecha:** 2026-01-30  
**SDK Version:** 10.0.0-alpha.01  
**Estado:** Workaround temporal con reflexión
