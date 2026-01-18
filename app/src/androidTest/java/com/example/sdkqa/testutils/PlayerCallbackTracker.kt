package com.example.sdkqa.testutils

import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

/**
 * Utilidad para rastrear callbacks del player en pruebas automatizadas.
 * Permite verificar que los callbacks se ejecuten correctamente.
 */
object PlayerCallbackTracker {
    private val playCallCount = AtomicInteger(0)
    private val pauseCallCount = AtomicInteger(0)
    private val readyCallCount = AtomicInteger(0)
    private val errorCallCount = AtomicInteger(0)
    private val playerViewReadyCallCount = AtomicInteger(0)
    
    private val lastPlayTime = AtomicLong(0)
    private val lastPauseTime = AtomicLong(0)
    private val lastReadyTime = AtomicLong(0)
    private val lastPlayerViewReadyTime = AtomicLong(0)
    
    private val isPlaying = AtomicBoolean(false)
    
    fun onPlay() {
        playCallCount.incrementAndGet()
        isPlaying.set(true)
        lastPlayTime.set(System.currentTimeMillis())
    }
    
    fun onPause() {
        pauseCallCount.incrementAndGet()
        isPlaying.set(false)
        lastPauseTime.set(System.currentTimeMillis())
    }
    
    fun onReady() {
        readyCallCount.incrementAndGet()
        lastReadyTime.set(System.currentTimeMillis())
    }
    
    fun onPlayerViewReady() {
        playerViewReadyCallCount.incrementAndGet()
        lastPlayerViewReadyTime.set(System.currentTimeMillis())
    }
    
    fun onError() {
        errorCallCount.incrementAndGet()
    }
    
    fun getPlayCallCount(): Int = playCallCount.get()
    fun getPauseCallCount(): Int = pauseCallCount.get()
    fun getReadyCallCount(): Int = readyCallCount.get()
    fun getErrorCallCount(): Int = errorCallCount.get()
    fun getPlayerViewReadyCallCount(): Int = playerViewReadyCallCount.get()
    fun isPlaying(): Boolean = isPlaying.get()
    
    /**
     * Espera hasta que se haya llamado onPlay, con polling cada 100ms
     */
    fun waitForPlay(timeoutSeconds: Long = 10): Boolean {
        val timeout = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(timeoutSeconds)
        val initialCount = playCallCount.get()
        
        while (System.currentTimeMillis() < timeout) {
            if (playCallCount.get() > initialCount) {
                return true
            }
            Thread.sleep(100)
        }
        return false
    }
    
    /**
     * Espera hasta que se haya llamado onPause, con polling cada 100ms
     */
    fun waitForPause(timeoutSeconds: Long = 10): Boolean {
        val timeout = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(timeoutSeconds)
        val initialCount = pauseCallCount.get()
        
        while (System.currentTimeMillis() < timeout) {
            if (pauseCallCount.get() > initialCount) {
                return true
            }
            Thread.sleep(100)
        }
        return false
    }
    
    /**
     * Espera hasta que se haya llamado onReady, con polling cada 100ms
     */
    fun waitForReady(timeoutSeconds: Long = 10): Boolean {
        val timeout = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(timeoutSeconds)
        val initialCount = readyCallCount.get()
        
        while (System.currentTimeMillis() < timeout) {
            if (readyCallCount.get() > initialCount) {
                return true
            }
            Thread.sleep(100)
        }
        return false
    }
    
    /**
     * Espera hasta que se haya llamado onPlayerViewReady, con polling cada 100ms.
     * Si el callback ya se ejecutó antes de llamar a esta función, retorna true inmediatamente.
     */
    fun waitForPlayerViewReady(timeoutSeconds: Long = 10): Boolean {
        val timeout = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(timeoutSeconds)
        val initialCount = playerViewReadyCallCount.get()
        
        // Si ya se ejecutó antes, retornar true inmediatamente
        if (initialCount > 0) {
            return true
        }
        
        while (System.currentTimeMillis() < timeout) {
            if (playerViewReadyCallCount.get() > initialCount) {
                return true
            }
            Thread.sleep(100)
        }
        return false
    }
    
    /**
     * Reinicia todos los contadores. Debe llamarse antes de cada prueba.
     */
    fun reset() {
        playCallCount.set(0)
        pauseCallCount.set(0)
        readyCallCount.set(0)
        errorCallCount.set(0)
        playerViewReadyCallCount.set(0)
        isPlaying.set(false)
        lastPlayTime.set(0)
        lastPauseTime.set(0)
        lastReadyTime.set(0)
        lastPlayerViewReadyTime.set(0)
    }
}
