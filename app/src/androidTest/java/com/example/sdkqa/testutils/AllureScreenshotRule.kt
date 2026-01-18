package com.example.sdkqa.testutils

import io.qameta.allure.kotlin.Allure
import io.qameta.allure.kotlin.AllureLifecycle
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import android.util.Log
import java.io.File
import java.io.FileInputStream

/**
 * TestWatcher que captura screenshots automáticamente cuando un test falla
 * y las adjunta al reporte de Allure.
 * 
 * Uso:
 * ```kotlin
 * @get:Rule
 * val allureScreenshotRule = AllureScreenshotRule()
 * ```
 */
class AllureScreenshotRule : TestWatcher() {
    
    private var testName: String = ""
    
    override fun starting(description: Description) {
        super.starting(description)
        testName = description.methodName ?: description.className
        Log.d("AllureScreenshotRule", "Iniciando test: $testName")
    }
    
    override fun succeeded(description: Description) {
        super.succeeded(description)
        testName = description.methodName ?: description.className
        Log.d("AllureScreenshotRule", "Test exitoso: $testName")
    }
    
    override fun failed(e: Throwable, description: Description) {
        super.failed(e, description)
        testName = description.methodName ?: description.className
        
        Log.e("AllureScreenshotRule", "Test falló: $testName", e)
        
        try {
            // Capturar screenshot del error
            val screenshotPath = ScreenshotCapture.capture(testName, "failure")
            
            if (screenshotPath != null) {
                // Adjuntar screenshot a Allure
                attachScreenshotToAllure(screenshotPath, "Screenshot del fallo")
                
                // También adjuntar logs de errores si están disponibles
                val errorLogs = LogcatCapture.getErrorLogs()
                if (errorLogs.isNotEmpty()) {
                    attachTextToAllure(
                        errorLogs.joinToString("\n"),
                        "Logs de errores",
                        "text/plain"
                    )
                }
                
                // Adjuntar logs del SDK
                val sdkLogs = LogcatCapture.getLogsByTag("SDK-QA")
                if (sdkLogs.isNotEmpty()) {
                    attachTextToAllure(
                        sdkLogs.takeLast(50).joinToString("\n"),
                        "Logs del SDK",
                        "text/plain"
                    )
                }
                
                // Adjuntar logs de MediastreamPlayer
                val mediastreamLogs = LogcatCapture.getLogsByTag("MediastreamPlayer")
                if (mediastreamLogs.isNotEmpty()) {
                    attachTextToAllure(
                        mediastreamLogs.takeLast(50).joinToString("\n"),
                        "Logs de MediastreamPlayer",
                        "text/plain"
                    )
                }
                
                // Adjuntar el stack trace del error
                attachTextToAllure(
                    e.stackTraceToString(),
                    "Stack trace del error",
                    "text/plain"
                )
            } else {
                Log.w("AllureScreenshotRule", "No se pudo capturar screenshot")
            }
        } catch (ex: Exception) {
            Log.e("AllureScreenshotRule", "Error adjuntando evidencia a Allure: ${ex.message}", ex)
        }
    }
    
    /**
     * Adjunta una screenshot al reporte de Allure.
     */
    private fun attachScreenshotToAllure(filePath: String, name: String) {
        try {
            val file = File(filePath)
            if (file.exists()) {
                val bytes = file.readBytes()
                // Usar la API de Allure Kotlin para adjuntar archivos
                Allure.attachment(
                    name,
                    bytes.inputStream(),
                    "image/png"
                )
                Log.d("AllureScreenshotRule", "Screenshot adjuntada a Allure: $name")
            }
        } catch (e: Exception) {
            Log.e("AllureScreenshotRule", "Error adjuntando screenshot a Allure: ${e.message}", e)
        }
    }
    
    /**
     * Adjunta texto al reporte de Allure.
     */
    private fun attachTextToAllure(content: String, name: String, contentType: String = "text/plain") {
        try {
            // Usar la API de Allure Kotlin para adjuntar texto
            Allure.attachment(
                name,
                content.toByteArray().inputStream(),
                contentType
            )
            Log.d("AllureScreenshotRule", "Texto adjuntado a Allure: $name")
        } catch (e: Exception) {
            Log.e("AllureScreenshotRule", "Error adjuntando texto a Allure: ${e.message}", e)
        }
    }
}
