package com.example.sdkqa.testutils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * Utilidad para capturar screenshots durante los tests.
 * Las capturas se guardan en el directorio de almacenamiento externo de la app.
 */
object ScreenshotCapture {
    
    private val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.US)
    
    /**
     * Captura una screenshot de la vista raíz actual.
     * @param testName Nombre del test (se usa en el nombre del archivo)
     * @param suffix Sufijo adicional para el nombre del archivo (ej: "before_failure")
     * @return Ruta del archivo guardado, o null si falla
     */
    fun capture(testName: String, suffix: String = ""): String? {
        return try {
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            val timestamp = dateFormat.format(Date())
            val fileName = "screenshot_${testName}_${suffix}_$timestamp.png"
            
            // Guardar en el directorio de almacenamiento externo de la app
            val screenshotsDir = File(context.getExternalFilesDir(null), "test_screenshots")
            if (!screenshotsDir.exists()) {
                screenshotsDir.mkdirs()
            }
            
            val file = File(screenshotsDir, fileName)
            
            // Capturar la vista usando UiAutomator (más confiable que View.draw)
            // La nueva API guarda directamente en el archivo
            val uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
            val success = uiDevice.takeScreenshot(file)
            
            if (!success) {
                android.util.Log.e("ScreenshotCapture", "No se pudo capturar la screenshot")
                return null
            }
            
            android.util.Log.d("ScreenshotCapture", "Screenshot guardada: ${file.absolutePath}")
            file.absolutePath
        } catch (e: Exception) {
            android.util.Log.e("ScreenshotCapture", "Error capturando screenshot: ${e.message}", e)
            null
        }
    }
    
    /**
     * Captura una screenshot de una vista específica.
     */
    fun captureView(view: View, testName: String, suffix: String = ""): String? {
        return try {
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            val timestamp = dateFormat.format(Date())
            val fileName = "screenshot_${testName}_${suffix}_$timestamp.png"
            
            val screenshotsDir = File(context.getExternalFilesDir(null), "test_screenshots")
            if (!screenshotsDir.exists()) {
                screenshotsDir.mkdirs()
            }
            
            val file = File(screenshotsDir, fileName)
            
            // Crear bitmap del tamaño de la vista
            val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            view.draw(canvas)
            
            // Guardar
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            
            android.util.Log.d("ScreenshotCapture", "Screenshot de vista guardada: ${file.absolutePath}")
            file.absolutePath
        } catch (e: Exception) {
            android.util.Log.e("ScreenshotCapture", "Error capturando screenshot de vista: ${e.message}", e)
            null
        }
    }
    
    /**
     * Limpia screenshots antiguas (más de 7 días).
     */
    fun cleanupOldScreenshots() {
        try {
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            val screenshotsDir = File(context.getExternalFilesDir(null), "test_screenshots")
            
            if (screenshotsDir.exists()) {
                val sevenDaysAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000)
                screenshotsDir.listFiles()?.forEach { file ->
                    if (file.lastModified() < sevenDaysAgo) {
                        file.delete()
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.w("ScreenshotCapture", "Error limpiando screenshots antiguas: ${e.message}")
        }
    }
}
