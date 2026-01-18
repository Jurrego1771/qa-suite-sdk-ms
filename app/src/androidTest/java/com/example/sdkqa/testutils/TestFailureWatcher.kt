package com.example.sdkqa.testutils

import org.junit.rules.TestWatcher
import org.junit.runner.Description
import android.util.Log

/**
 * TestWatcher que captura screenshots y logs cuando un test falla.
 * 
 * Uso:
 * ```kotlin
 * @get:Rule
 * val failureWatcher = TestFailureWatcher()
 * ```
 */
class TestFailureWatcher : TestWatcher() {
    
    private var testName: String = ""
    private var testStarted = false
    
    override fun starting(description: Description) {
        super.starting(description)
        testName = description.methodName ?: description.className
        testStarted = true
        
        // Limpiar screenshots y logs antiguos al inicio
        ScreenshotCapture.cleanupOldScreenshots()
        LogcatCapture.cleanupOldLogs()
        
        // Iniciar captura de logs
        LogcatCapture.startCapture(testName)
        
        Log.d("TestFailureWatcher", "Iniciando test: $testName")
    }
    
    override fun succeeded(description: Description) {
        super.succeeded(description)
        Log.d("TestFailureWatcher", "Test exitoso: $testName")
        
        // Detener captura de logs
        LogcatCapture.stopCapture()
    }
    
    override fun failed(e: Throwable, description: Description) {
        super.failed(e, description)
        testName = description.methodName ?: description.className
        
        Log.e("TestFailureWatcher", "Test falló: $testName", e)
        
        try {
            // Capturar screenshot del error
            val screenshotPath = ScreenshotCapture.capture(testName, "failure")
            if (screenshotPath != null) {
                Log.d("TestFailureWatcher", "Screenshot de fallo guardada: $screenshotPath")
            }
            
            // Obtener logs de errores relevantes
            val errorLogs = LogcatCapture.getErrorLogs()
            if (errorLogs.isNotEmpty()) {
                Log.d("TestFailureWatcher", "Errores encontrados en logs (${errorLogs.size}):")
                errorLogs.takeLast(10).forEach { log ->
                    Log.e("TestFailureWatcher", "  $log")
                }
            }
            
            // Obtener logs específicos del SDK
            val sdkLogs = LogcatCapture.getLogsByTag("SDK-QA")
            if (sdkLogs.isNotEmpty()) {
                Log.d("TestFailureWatcher", "Logs del SDK (${sdkLogs.size}):")
                sdkLogs.takeLast(10).forEach { log ->
                    Log.d("TestFailureWatcher", "  $log")
                }
            }
            
            // Obtener logs de Mediastream
            val mediastreamLogs = LogcatCapture.getLogsByTag("MediastreamPlayer")
            if (mediastreamLogs.isNotEmpty()) {
                Log.d("TestFailureWatcher", "Logs de MediastreamPlayer (${mediastreamLogs.size}):")
                mediastreamLogs.takeLast(10).forEach { log ->
                    Log.d("TestFailureWatcher", "  $log")
                }
            }
            
        } catch (ex: Exception) {
            Log.e("TestFailureWatcher", "Error capturando información de fallo: ${ex.message}", ex)
        } finally {
            // Detener captura de logs
            LogcatCapture.stopCapture()
        }
    }
    
    override fun finished(description: Description) {
        super.finished(description)
        // Asegurarse de que la captura de logs se detenga
        if (testStarted) {
            LogcatCapture.stopCapture()
        }
    }
}
