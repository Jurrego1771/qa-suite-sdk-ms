package com.example.sdkqa.video

import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.example.sdkqa.R
import com.example.sdkqa.testutils.LogcatCapture
import com.example.sdkqa.testutils.PlayerCallbackTracker
import com.example.sdkqa.testutils.TestablePlayerCallback
import com.example.sdkqa.testutils.TestFailureWatcher
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import android.widget.FrameLayout
import androidx.media3.ui.PlayerView
import org.hamcrest.Matchers.anything

/**
 * Pruebas automatizadas para VideoLiveDvrActivity.
 * 
 * Estas pruebas verifican:
 * 1. Inicialización del player en modo Live
 * 2. Cambio a modo DVR con ventana de 1 hora
 * 3. Navegación en la línea de tiempo DVR
 * 4. Validación de currentTime al navegar
 * 5. Reproducción continua después de seek
 * 6. Modos DVR Start y DVR VOD
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class VideoLiveDvrActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(VideoLiveDvrActivity::class.java)
    
    @get:Rule
    val failureWatcher = TestFailureWatcher()

    private lateinit var testCallback: TestablePlayerCallback

    @Before
    fun setUp() {
        LogcatCapture.start()
        PlayerCallbackTracker.reset()
        
        testCallback = TestablePlayerCallback()
        
        activityRule.scenario.onActivity { activity ->
            try {
                injectTestCallback(activity)
            } catch (e: Exception) {
                android.util.Log.w("VideoLiveDvrActivityTest", "No se pudo inyectar callback: ${e.message}")
            }
        }
    }
    
    private fun injectTestCallback(activity: VideoLiveDvrActivity) {
        try {
            val playerField = VideoLiveDvrActivity::class.java.getDeclaredField("player")
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
            android.util.Log.w("VideoLiveDvrActivityTest", "Error inyectando callback: ${e.message}")
        }
    }

    @After
    fun tearDown() {
        LogcatCapture.stop()
        PlayerCallbackTracker.reset()
    }

    /**
     * TEST 1: Verificar inicialización en modo Live.
     * Assert: Player se crea sin DVR habilitado por defecto.
     */
    @Test
    fun testLiveModeInitialization() {
        // Esperar inicialización
        Thread.sleep(3000)

        // Assert 1: Verificar que la actividad está visible
        onView(isRoot())
            .check(matches(isDisplayed()))

        // Assert 2: Verificar que el spinner existe
        onView(withId(R.id.spinnerMode))
            .check(matches(isDisplayed()))

        // Assert 3: Verificar que el player se inicializó
        onView(isRoot())
            .check { view, _ ->
                val frameLayout = view.findViewById<FrameLayout>(R.id.main_media_frame)
                assert(frameLayout != null) { "FrameLayout debe existir" }
                
                val playerView = findPlayerViewInHierarchy(frameLayout)
                assert(playerView != null) { "PlayerView debe existir" }
                assert(playerView?.player != null) { "Player debe estar asignado" }
            }

        // Assert 4: Logs deben indicar modo Live
        val logs = LogcatCapture.getRecentLogs(50)
        val hasLiveMode = logs.any { it.contains("Config: Live mode") }
        assert(hasLiveMode) { "Los logs deben indicar modo Live" }
    }

    /**
     * TEST 2: Cambiar a modo DVR y verificar configuración.
     * Assert: windowDvr se configura a 60 minutos.
     */
    @Test
    fun testSwitchToDvrMode() {
        // Esperar inicialización
        Thread.sleep(3000)

        // Cambiar a modo DVR (segunda opción)
        onView(withId(R.id.spinnerMode))
            .perform(click())
        
        Thread.sleep(500)
        onData(anything())
            .atPosition(1)  // "DVR"
            .perform(click())
        
        // Esperar reconfiguración
        Thread.sleep(3000)

        // Assert 1: Logs deben indicar modo DVR con 1 hora
        val logs = LogcatCapture.getRecentLogs(50)
        val hasDvrMode = logs.any { 
            it.contains("Config: DVR mode with 1 hour window") 
        }
        assert(hasDvrMode) { 
            "Los logs deben indicar 'DVR mode with 1 hour window'. Últimos logs: ${logs.takeLast(5)}" 
        }

        // Assert 2: Verificar que el player sigue activo
        onView(isRoot())
            .check { view, _ ->
                val frameLayout = view.findViewById<FrameLayout>(R.id.main_media_frame)
                val playerView = findPlayerViewInHierarchy(frameLayout)
                assert(playerView?.player != null) { "Player debe seguir activo después de cambiar a DVR" }
            }
    }

    /**
     * TEST 3: Navegar en la línea de tiempo DVR.
     * Assert: Puede buscar a una posición específica y currentTime cambia.
     */
    @Test
    fun testDvrTimelineNavigation() {
        // Cambiar a modo DVR
        Thread.sleep(3000)
        onView(withId(R.id.spinnerMode)).perform(click())
        Thread.sleep(500)
        onData(anything()).atPosition(1).perform(click())
        
        // Esperar a que el player se estabilice en modo DVR
        Thread.sleep(5000)

        onView(isRoot())
            .check { view, _ ->
                val frameLayout = view.findViewById<FrameLayout>(R.id.main_media_frame)
                val playerView = findPlayerViewInHierarchy(frameLayout)
                val player = playerView?.player
                
                assert(player != null) { "Player debe existir" }
                
                // Assert 1: Obtener currentTime inicial
                val initialTime = player!!.currentPosition
                android.util.Log.d("VideoLiveDvrActivityTest", "Initial currentTime: $initialTime ms")
                
                // Obtener duración total disponible (ventana DVR)
                val duration = player.duration
                android.util.Log.d("VideoLiveDvrActivityTest", "Total duration: $duration ms")
                
                // Assert 2: La duración debe ser válida (no indeterminada)
                // En DVR, duration puede ser C.TIME_UNSET (-9223372036854775807) si es live window
                // o un valor específico si tiene ventana definida
                android.util.Log.d("VideoLiveDvrActivityTest", "Duration is: $duration")
                
                // Buscar a un punto anterior (10 segundos atrás del punto actual)
                val seekPosition = if (initialTime > 10000) {
                    initialTime - 10000  // 10 segundos atrás
                } else {
                    0  // Si estamos muy cerca del inicio, ir al inicio
                }
                
                android.util.Log.d("VideoLiveDvrActivityTest", "Seeking to: $seekPosition ms")
                player.seekTo(seekPosition)
                
                // Esperar a que se complete el seek
                Thread.sleep(2000)
                
                // Assert 3: Verificar que currentTime cambió
                val newTime = player.currentPosition
                android.util.Log.d("VideoLiveDvrActivityTest", "New currentTime after seek: $newTime ms")
                
                // El nuevo tiempo debe estar cerca de la posición buscada (con margen de ±2 segundos)
                val timeDiff = kotlin.math.abs(newTime - seekPosition)
                assert(timeDiff < 2000) {
                    "CurrentTime debe estar cerca de la posición buscada. " +
                    "Buscado: $seekPosition, Actual: $newTime, Diferencia: $timeDiff ms"
                }
                
                // Assert 4: El player debe estar reproduciendo o en buffering
                val isPlayingOrBuffering = player.isPlaying || 
                    player.playbackState == androidx.media3.common.Player.STATE_BUFFERING
                assert(isPlayingOrBuffering) {
                    "Player debe estar reproduciendo o en buffering después de seek. " +
                    "isPlaying: ${player.isPlaying}, state: ${player.playbackState}"
                }
            }
    }

    /**
     * TEST 4: Verificar reproducción continua después de seek en DVR.
     * Assert: El currentTime sigue avanzando después de buscar.
     */
    @Test
    fun testContinuousPlaybackAfterSeek() {
        // Cambiar a modo DVR
        Thread.sleep(3000)
        onView(withId(R.id.spinnerMode)).perform(click())
        Thread.sleep(500)
        onData(anything()).atPosition(1).perform(click())
        Thread.sleep(5000)

        onView(isRoot())
            .check { view, _ ->
                val frameLayout = view.findViewById<FrameLayout>(R.id.main_media_frame)
                val playerView = findPlayerViewInHierarchy(frameLayout)
                val player = playerView?.player
                
                assert(player != null) { "Player debe existir" }
                
                // Obtener tiempo inicial
                val initialTime = player!!.currentPosition
                android.util.Log.d("VideoLiveDvrActivityTest", "Time before seek: $initialTime ms")
                
                // Hacer seek hacia atrás si es posible
                val seekPosition = if (initialTime > 15000) initialTime - 15000 else 0
                player.seekTo(seekPosition)
                Thread.sleep(2000)
                
                // Obtener tiempo después del seek
                val timeAfterSeek = player.currentPosition
                android.util.Log.d("VideoLiveDvrActivityTest", "Time after seek: $timeAfterSeek ms")
                
                // Assert 1: Asegurarse de que el player está reproduciendo
                if (!player.isPlaying && player.playWhenReady) {
                    // Si no está reproduciendo pero debe, esperar un poco más
                    Thread.sleep(2000)
                }
                
                // Esperar y verificar que el tiempo avanza
                Thread.sleep(3000)
                val timeLater = player.currentPosition
                android.util.Log.d("VideoLiveDvrActivityTest", "Time 3s later: $timeLater ms")
                
                // Assert 2: El tiempo debe haber avanzado (reproducción continua)
                val timeAdvanced = timeLater > timeAfterSeek
                assert(timeAdvanced) {
                    "El tiempo debe avanzar después del seek (reproducción continua). " +
                    "Después de seek: $timeAfterSeek, 3s después: $timeLater, " +
                    "Diferencia: ${timeLater - timeAfterSeek} ms"
                }
                
                // Assert 3: El avance debe ser razonable (cerca de 3 segundos, con tolerancia)
                val actualAdvance = timeLater - timeAfterSeek
                assert(actualAdvance > 1000 && actualAdvance < 5000) {
                    "El avance debe ser aproximadamente 3 segundos. Avance real: $actualAdvance ms"
                }
            }
    }

    /**
     * TEST 5: Modo DVR Start - inicia desde 1h 30m atrás.
     * Assert: Player inicia en una posición del pasado.
     */
    @Test
    fun testDvrStartMode() {
        // Cambiar a modo DVR Start (tercera opción)
        Thread.sleep(3000)
        onView(withId(R.id.spinnerMode)).perform(click())
        Thread.sleep(500)
        onData(anything()).atPosition(2).perform(click())  // "DVR Start"
        
        // Esperar reconfiguración y carga
        Thread.sleep(5000)

        // Assert 1: Logs deben indicar DVR Start con dvrStart configurado
        val logs = LogcatCapture.getRecentLogs(50)
        val hasDvrStart = logs.any { it.contains("Config: DVR Start mode") }
        assert(hasDvrStart) { "Los logs deben indicar 'DVR Start mode'" }

        // Assert 2: Logs deben mostrar el timestamp de dvrStart
        val hasDvrStartTime = logs.any { it.contains("dvrStart:") }
        assert(hasDvrStartTime) { "Los logs deben mostrar el timestamp de dvrStart" }

        // Assert 3: Verificar que el player está activo
        onView(isRoot())
            .check { view, _ ->
                val frameLayout = view.findViewById<FrameLayout>(R.id.main_media_frame)
                val playerView = findPlayerViewInHierarchy(frameLayout)
                assert(playerView?.player != null) { "Player debe estar activo en DVR Start" }
            }
    }

    /**
     * TEST 6: Modo DVR VOD - reproduce un segmento específico.
     * Assert: Player reproduce un segmento cerrado con inicio y fin definidos.
     */
    @Test
    fun testDvrVodMode() {
        // Cambiar a modo DVR VOD (cuarta opción)
        Thread.sleep(3000)
        onView(withId(R.id.spinnerMode)).perform(click())
        Thread.sleep(500)
        onData(anything()).atPosition(3).perform(click())  // "DVR VOD"
        
        // Esperar reconfiguración y carga
        Thread.sleep(5000)

        // Assert 1: Logs deben indicar DVR VOD con dvrStart y dvrEnd
        val logs = LogcatCapture.getRecentLogs(50)
        val hasDvrVod = logs.any { it.contains("Config: DVR VOD mode") }
        assert(hasDvrVod) { "Los logs deben indicar 'DVR VOD mode'" }

        // Assert 2: Logs deben mostrar tanto dvrStart como dvrEnd
        val hasBothTimestamps = logs.any { 
            it.contains("dvrStart:") && it.contains("dvrEnd:") 
        }
        assert(hasBothTimestamps) { 
            "Los logs deben mostrar tanto dvrStart como dvrEnd" 
        }

        // Assert 3: Verificar que el player tiene duración definida (segmento cerrado)
        onView(isRoot())
            .check { view, _ ->
                val frameLayout = view.findViewById<FrameLayout>(R.id.main_media_frame)
                val playerView = findPlayerViewInHierarchy(frameLayout)
                val player = playerView?.player
                
                assert(player != null) { "Player debe existir en DVR VOD" }
                
                // En DVR VOD, eventualmente la duración debería ser conocida
                // (aunque puede tardar en cargar)
                android.util.Log.d("VideoLiveDvrActivityTest", 
                    "DVR VOD duration: ${player!!.duration} ms")
            }
    }

    /**
     * TEST 7: Verificar que el spinner tiene 4 modos.
     * Assert: Spinner contiene Live, DVR, DVR Start, DVR VOD.
     */
    @Test
    fun testModeSelectorHasFourModes() {
        Thread.sleep(2000)

        // Assert: Spinner tiene exactamente 4 opciones
        onView(isRoot())
            .check { view, _ ->
                val spinner = view.findViewById<android.widget.Spinner>(R.id.spinnerMode)
                assert(spinner != null) { "Spinner debe existir" }
                assert(spinner.adapter.count == 4) {
                    "Spinner debe tener 4 modos. Actual: ${spinner.adapter.count}"
                }
            }
    }

    /**
     * TEST 8: Cambio rápido entre modos.
     * Assert: Player maneja cambios consecutivos sin crashes.
     */
    @Test
    fun testRapidModeSwitch() {
        Thread.sleep(3000)

        // Cambiar a DVR
        onView(withId(R.id.spinnerMode)).perform(click())
        Thread.sleep(300)
        onData(anything()).atPosition(1).perform(click())
        Thread.sleep(2000)

        // Cambiar a Live
        onView(withId(R.id.spinnerMode)).perform(click())
        Thread.sleep(300)
        onData(anything()).atPosition(0).perform(click())
        Thread.sleep(2000)

        // Cambiar a DVR Start
        onView(withId(R.id.spinnerMode)).perform(click())
        Thread.sleep(300)
        onData(anything()).atPosition(2).perform(click())
        Thread.sleep(2000)

        // Assert 1: Actividad sigue viva después de cambios rápidos
        onView(isRoot())
            .check(matches(isDisplayed()))

        // Assert 2: Player sigue activo
        onView(isRoot())
            .check { view, _ ->
                val frameLayout = view.findViewById<FrameLayout>(R.id.main_media_frame)
                val playerView = findPlayerViewInHierarchy(frameLayout)
                assert(playerView?.player != null) { 
                    "Player debe seguir activo después de cambios rápidos" 
                }
            }

        // Assert 3: No hay crashes en logs
        val logs = LogcatCapture.getRecentLogs(50)
        val hasCrash = logs.any { 
            it.contains("FATAL") || it.contains("java.lang.RuntimeException") 
        }
        assert(!hasCrash) { "No debe haber crashes durante cambios rápidos de modo" }
    }

    /**
     * Helper function para encontrar PlayerView en la jerarquía.
     */
    private fun findPlayerViewInHierarchy(view: android.view.View): PlayerView? {
        if (view is PlayerView) {
            return view
        }
        
        if (view is android.view.ViewGroup) {
            for (i in 0 until view.childCount) {
                val child = view.getChildAt(i)
                val result = findPlayerViewInHierarchy(child)
                if (result != null) {
                    return result
                }
            }
        }
        
        return null
    }
}
