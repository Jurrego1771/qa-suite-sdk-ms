package com.example.sdkqa.audio

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.sdkqa.R
import com.example.sdkqa.testutils.LogcatCapture
import com.example.sdkqa.testutils.PlayerCallbackTracker
import com.example.sdkqa.testutils.TestFailureWatcher
import com.example.sdkqa.testutils.TestablePlayerCallback
import org.hamcrest.CoreMatchers.containsString
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AudioAodCustomUIActivityTest {

    @get:Rule
    val failureWatcher = TestFailureWatcher()

    private lateinit var testCallback: TestablePlayerCallback

    @Before
    fun setUp() {
        // Iniciar captura de logs - CRÍTICO para que funcione countLogsContaining()
        LogcatCapture.startCapture("AudioAodCustomUIActivityTest")
        
        // Reiniciar tracker de callbacks
        PlayerCallbackTracker.reset()
        
        // Crear callback de prueba
        testCallback = TestablePlayerCallback()
    }

    @After
    fun tearDown() {
        // Detener captura de logs
        LogcatCapture.stopCapture()
        
        // Limpiar tracker
        PlayerCallbackTracker.reset()
    }

    @Test
    fun launches_and_emits_ready_callbacks() {
        ActivityScenario.launch(AudioAodCustomUIActivity::class.java).use {
            // Validar que el SDK llega a estados básicos
            waitForLogIncrement("onReady", timeoutMs = 30_000)
            waitForLogIncrement("playerViewReady", timeoutMs = 30_000)
        }
    }

    @Test
    fun play_pause_button_triggers_callbacks() {
        ActivityScenario.launch(AudioAodCustomUIActivity::class.java).use { scenario ->
            // Inyectar callback de prueba en la Activity
            scenario.onActivity { activity ->
                try {
                    injectTestCallback(activity)
                } catch (e: Exception) {
                    android.util.Log.w("AudioAodCustomUIActivityTest", "No se pudo inyectar callback: ${e.message}")
                }
            }
            
            // Esperar a que el player esté listo
            waitForLogIncrement("playerViewReady", timeoutMs = 30_000)

            // Capturar contadores ANTES del click
            val playActionBefore = countLogsContaining("USER ACTION: play()")
            val pauseActionBefore = countLogsContaining("USER ACTION: pause()")
            val onPlayBefore = countLogsContaining("onPlay")
            val onPauseBefore = countLogsContaining("onPause")
            val playCallbackBefore = PlayerCallbackTracker.getPlayCallCount()
            val pauseCallbackBefore = PlayerCallbackTracker.getPauseCallCount()

            // Hacer click en el botón
            onView(withId(R.id.btnPlayPause)).perform(click())

            // Estrategia híbrida: verificar tanto en logs como en callbacks
            // El botón puede ejecutar play() o pause() dependiendo del estado inicial
            val playedViaLog = waitForCountIncreaseNoThrow("USER ACTION: play()", playActionBefore, timeoutMs = 10_000)
            val pausedViaLog = if (!playedViaLog) waitForCountIncreaseNoThrow("USER ACTION: pause()", pauseActionBefore, timeoutMs = 10_000) else false
            
            // Verificar también callbacks como respaldo
            val playedViaCallback = PlayerCallbackTracker.getPlayCallCount() > playCallbackBefore
            val pausedViaCallback = PlayerCallbackTracker.getPauseCallCount() > pauseCallbackBefore

            if (playedViaLog || playedViaCallback) {
                // Se ejecutó play
                waitForCountIncrease("onPlay", onPlayBefore, timeoutMs = 20_000)
            } else if (pausedViaLog || pausedViaCallback) {
                // Se ejecutó pause
                waitForCountIncrease("onPause", onPauseBefore, timeoutMs = 20_000)
            } else {
                // No se detectó ninguna acción - error de test
                val currentLogs = LogcatCapture.getRecentLogs(50).joinToString("\n")
                throw AssertionError(
                    "No se detectó acción de play() ni pause() tras el click.\n" +
                    "Logs: playAction=$playActionBefore->$playActionBefore, pauseAction=$pauseActionBefore->$pauseActionBefore\n" +
                    "Callbacks: play=$playCallbackBefore->${PlayerCallbackTracker.getPlayCallCount()}, pause=$pauseCallbackBefore->${PlayerCallbackTracker.getPauseCallCount()}\n" +
                    "Últimos logs:\n$currentLogs"
                )
            }
        }
    }
    
    /**
     * Inyecta el callback de prueba en la Activity usando reflection.
     */
    private fun injectTestCallback(activity: AudioAodCustomUIActivity) {
        try {
            val playerField = AudioAodCustomUIActivity::class.java.getDeclaredField("player")
            playerField.isAccessible = true
            val player = playerField.get(activity)
            
            if (player != null) {
                val addCallbackMethod = player.javaClass.getMethod(
                    "addPlayerCallback",
                    am.mediastre.mediastreamplatformsdkandroid.MediastreamPlayerCallback::class.java
                )
                addCallbackMethod.invoke(player, testCallback)
            }
        } catch (e: Exception) {
            android.util.Log.w("AudioAodCustomUIActivityTest", "Error inyectando callback: ${e.message}")
        }
    }

    @Test
    fun speed_forward_backward_and_reload_radio_generate_events() {
        ActivityScenario.launch(AudioAodCustomUIActivity::class.java).use {
            waitForLogIncrement("playerViewReady", timeoutMs = 30_000)

            // Speed
            val speedBefore = countLogsContaining("USER ACTION: changeSpeed(")
            onView(withId(R.id.btnSpeed)).perform(click())
            onView(withId(R.id.btnSpeed)).check(matches(withText("1.25x")))
            waitForCountIncrease("USER ACTION: changeSpeed(", speedBefore, timeoutMs = 10_000)

            // Forward / Backward
            val forwardBefore = countLogsContaining("USER ACTION: forward(")
            val backwardBefore = countLogsContaining("USER ACTION: backward(")
            onView(withId(R.id.btnForward)).perform(click())
            waitForCountIncrease("USER ACTION: forward(", forwardBefore, timeoutMs = 10_000)
            onView(withId(R.id.btnBackward)).perform(click())
            waitForCountIncrease("USER ACTION: backward(", backwardBefore, timeoutMs = 10_000)

            // Reload radio B
            val reloadBefore = countLogsContaining("Reload player with contentId=")
            val onReloadBefore = countLogsContaining("onPlayerReload")

            onView(withId(R.id.rbStationB)).perform(click())
            onView(withId(R.id.tvContentId)).check(matches(withText(containsString("contentId (B):"))))

            waitForCountIncrease("Reload player with contentId=", reloadBefore, timeoutMs = 30_000)
            waitForCountIncrease("onPlayerReload", onReloadBefore, timeoutMs = 30_000)
        }
    }

    private fun countLogsContaining(needle: String): Int {
        return LogcatCapture.getRecentLogs(1000).count { it.contains(needle) }
    }

    private fun waitForLogIncrement(needle: String, timeoutMs: Long): Boolean {
        val before = countLogsContaining(needle)
        return waitForCountIncrease(needle, before, timeoutMs)
    }

    private fun waitForCountIncrease(needle: String, before: Int, timeoutMs: Long): Boolean {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            if (countLogsContaining(needle) > before) return true
            Thread.sleep(100)
        }
        throw AssertionError("Timeout esperando log '$needle' (before=$before).")
    }
    
    /**
     * Versión sin excepción de waitForCountIncrease - retorna true/false sin lanzar error.
     */
    private fun waitForCountIncreaseNoThrow(needle: String, before: Int, timeoutMs: Long): Boolean {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            if (countLogsContaining(needle) > before) return true
            Thread.sleep(100)
        }
        return false
    }
}

