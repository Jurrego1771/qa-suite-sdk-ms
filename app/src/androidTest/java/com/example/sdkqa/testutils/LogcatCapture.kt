package com.example.sdkqa.testutils

import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import java.io.BufferedReader
import java.io.File
import java.io.FileWriter
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.*

/**
 * Utilidad para capturar logs de Logcat durante los tests.
 * Especialmente útil para capturar logs del SDK Mediastream (onPlaybackErrors, onEmbedErrors, etc.).
 */
object LogcatCapture {
    
    private val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.US)
    private var logcatProcess: Process? = null
    private var logWriter: FileWriter? = null
    private val logBuffer = mutableListOf<String>()
    private val maxBufferSize = 1000 // Mantener últimos 1000 logs en memoria
    
    /**
     * Inicia la captura de logs.
     * @param testName Nombre del test (se usa en el nombre del archivo)
     */
    fun startCapture(testName: String) {
        try {
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            val timestamp = dateFormat.format(Date())
            val fileName = "logcat_${testName}_$timestamp.txt"
            
            val logsDir = File(context.getExternalFilesDir(null), "test_logs")
            if (!logsDir.exists()) {
                logsDir.mkdirs()
            }
            
            val logFile = File(logsDir, fileName)
            logWriter = FileWriter(logFile, true)
            
            // Iniciar proceso de logcat
            val processBuilder = ProcessBuilder(
                "logcat",
                "-v", "time",
                "-s", // Silenciar logs por defecto
                "SDK-QA:*", // Logs de nuestra app
                "MediastreamPlayer:*", // Logs del SDK
                "ExoPlayer:*", // Logs de Media3/ExoPlayer
                "Media3:*", // Logs de Media3
                "AndroidRuntime:E", // Errores de Android
                "*:E" // Todos los errores
            )
            
            logcatProcess = processBuilder.start()
            
            // Leer logs en un thread separado
            Thread {
                try {
                    BufferedReader(InputStreamReader(logcatProcess?.inputStream)).use { reader ->
                        var line: String?
                        while (reader.readLine().also { line = it } != null) {
                            line?.let { logLine ->
                                synchronized(logBuffer) {
                                    logBuffer.add(logLine)
                                    if (logBuffer.size > maxBufferSize) {
                                        logBuffer.removeAt(0)
                                    }
                                }
                                logWriter?.write("$logLine\n")
                                logWriter?.flush()
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("LogcatCapture", "Error leyendo logcat: ${e.message}", e)
                }
            }.start()
            
            Log.d("LogcatCapture", "Captura de logs iniciada: ${logFile.absolutePath}")
        } catch (e: Exception) {
            Log.e("LogcatCapture", "Error iniciando captura de logs: ${e.message}", e)
        }
    }
    
    /**
     * Detiene la captura de logs y guarda el archivo.
     */
    fun stopCapture() {
        try {
            logcatProcess?.destroy()
            logcatProcess = null
            
            logWriter?.flush()
            logWriter?.close()
            logWriter = null
        } catch (e: Exception) {
            Log.e("LogcatCapture", "Error deteniendo captura de logs: ${e.message}", e)
        }
    }
    
    /**
     * Obtiene los últimos logs capturados (útil para incluir en reportes).
     */
    fun getRecentLogs(count: Int = 100): List<String> {
        synchronized(logBuffer) {
            return logBuffer.takeLast(count)
        }
    }
    
    /**
     * Obtiene logs filtrados por tag (útil para buscar errores específicos del SDK).
     */
    fun getLogsByTag(tag: String): List<String> {
        synchronized(logBuffer) {
            return logBuffer.filter { it.contains(tag) }
        }
    }
    
    /**
     * Obtiene todos los logs de errores capturados.
     */
    fun getErrorLogs(): List<String> {
        synchronized(logBuffer) {
            return logBuffer.filter { 
                it.contains(" E ") || 
                it.contains("ERROR") || 
                it.contains("onError") ||
                it.contains("onPlaybackErrors") ||
                it.contains("onEmbedErrors")
            }
        }
    }
    
    /**
     * Limpia logs antiguos (más de 7 días).
     */
    fun cleanupOldLogs() {
        try {
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            val logsDir = File(context.getExternalFilesDir(null), "test_logs")
            
            if (logsDir.exists()) {
                val sevenDaysAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000)
                logsDir.listFiles()?.forEach { file ->
                    if (file.lastModified() < sevenDaysAgo) {
                        file.delete()
                    }
                }
            }
        } catch (e: Exception) {
            Log.w("LogcatCapture", "Error limpiando logs antiguos: ${e.message}")
        }
    }
}
