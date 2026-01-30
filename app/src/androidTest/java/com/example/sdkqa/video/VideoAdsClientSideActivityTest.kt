package com.example.sdkqa.video

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
import org.hamcrest.Matchers.containsString

/**
 * Pruebas automatizadas para VideoAdsClientSideActivity.
 * 
 * Estas pruebas verifican:
 * 1. Inicialización correcta del player con ads client-side (VAST/VMAP)
 * 2. Configuración correcta de adURL
 * 3. Callbacks de eventos de ads (onAdEvents)
 * 4. Callbacks de errores de ads (onAdErrorEvent)
 * 5. Reproducción del contenido después de ads
 * 6. Cambio dinámico de tipo de ad
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class VideoAdsClientSideActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(VideoAdsClientSideActivity::class.java)
    
    @get:Rule
    val failureWatcher = TestFailureWatcher()

    private lateinit var testCallback: TestablePlayerCallback

    @Before
    fun setUp() {
        // Inicializar LogcatCapture
        LogcatCapture.start()
        
        // Reiniciar el tracker antes de cada prueba
        PlayerCallbackTracker.reset()
        
        // Crear callback de prueba
        testCallback = TestablePlayerCallback()
        
        // Inyectar el callback de prueba en la Activity
        activityRule.scenario.onActivity { activity ->
            try {
                injectTestCallback(activity)
            } catch (e: Exception) {
                android.util.Log.w("VideoAdsClientSideActivityTest", "No se pudo inyectar callback: ${e.message}")
            }
        }
    }
    
    /**
     * Inyecta el callback de prueba en la Activity usando reflection.
     */
    private fun injectTestCallback(activity: VideoAdsClientSideActivity) {
        try {
            val playerField = VideoAdsClientSideActivity::class.java.getDeclaredField("player")
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
            android.util.Log.w("VideoAdsClientSideActivityTest", "Error inyectando callback: ${e.message}")
        }
    }

    @After
    fun tearDown() {
        LogcatCapture.stop()
        PlayerCallbackTracker.reset()
    }

    /**
     * TEST 1: Verificar inicialización del player con configuración de ads.
     * Assert: Player se crea correctamente y tiene configuración de ads.
     */
    @Test
    fun testPlayerInitializationWithAds() {
        // Esperar inicialización
        Thread.sleep(3000)

        // Assert 1: Verificar que la actividad está visible
        onView(isRoot())
            .check(matches(isDisplayed()))

        // Assert 2: Verificar que existe el FrameLayout
        onView(withId(R.id.main_media_frame))
            .check(matches(isDisplayed()))

        // Assert 3: Verificar que el player se inicializó
        onView(isRoot())
            .check { view, _ ->
                val frameLayout = view.findViewById<FrameLayout>(R.id.main_media_frame)
                assert(frameLayout != null) {
                    "El FrameLayout debe existir"
                }
                
                val playerView = findPlayerViewInHierarchy(frameLayout)
                assert(playerView != null) {
                    "PlayerView debe existir"
                }
                
                assert(playerView?.player != null) {
                    "El PlayerView debe tener un player asignado"
                }
            }

        // Assert 4: Verificar en logs que se configuró adURL
        val logs = LogcatCapture.getRecentLogs(50)
        val hasAdConfig = logs.any { it.contains("Ad configuration set") }
        assert(hasAdConfig) {
            "Los logs deben indicar que se configuró la URL de ads"
        }
    }

    /**
     * TEST 2: Verificar que el spinner de tipos de ads existe y es funcional.
     * Assert: Spinner tiene 3 opciones de ads.
     */
    @Test
    fun testAdTypeSelectorExists() {
        // Esperar inicialización
        Thread.sleep(2000)

        // Assert 1: Verificar que el spinner existe y es visible
        onView(withId(R.id.spinnerAdType))
            .check(matches(isDisplayed()))
            .check(matches(isClickable()))

        // Assert 2: Verificar que hay opciones en el spinner
        onView(isRoot())
            .check { view, _ ->
                val spinner = view.findViewById<android.widget.Spinner>(R.id.spinnerAdType)
                assert(spinner != null) {
                    "El spinner debe existir"
                }
                
                assert(spinner.adapter.count == 3) {
                    "El spinner debe tener 3 opciones de ads. Actual: ${spinner.adapter.count}"
                }
            }
    }

    /**
     * TEST 3: Verificar que los TextViews de status están actualizándose.
     * Assert: TextViews de ad status y ad events existen y son visibles.
     */
    @Test
    fun testAdStatusTextViewsExist() {
        // Esperar inicialización
        Thread.sleep(2000)

        // Assert 1: Verificar que tvAdStatus existe
        onView(withId(R.id.tvAdStatus))
            .check(matches(isDisplayed()))
            .check(matches(withText(containsString("Ad Status"))))

        // Assert 2: Verificar que tvAdEvents existe
        onView(withId(R.id.tvAdEvents))
            .check(matches(isDisplayed()))
            .check(matches(withText(containsString("Last Ad Event"))))
    }

    /**
     * TEST 4: Verificar que se reciben eventos de ads o contenido.
     * Assert: Se debe recibir playerViewReady indicando que el player está listo.
     */
    @Test
    fun testPlayerViewReadyCallback() {
        // Esperar a que playerViewReady se ejecute (timeout 15 segundos)
        val playerViewReadyReceived = PlayerCallbackTracker.waitForPlayerViewReady(15)
        
        // Assert: playerViewReady debe haberse ejecutado
        assert(playerViewReadyReceived) {
            "El callback playerViewReady debe ejecutarse dentro de 15 segundos"
        }
        
        // Assert: Debe haber al menos 1 llamada a playerViewReady
        assert(PlayerCallbackTracker.getPlayerViewReadyCallCount() > 0) {
            "Debe haber al menos 1 llamada a playerViewReady. Actual: ${PlayerCallbackTracker.getPlayerViewReadyCallCount()}"
        }
    }

    /**
     * TEST 5: Verificar logs de eventos de ads.
     * Assert: Logs deben contener información de ads (eventos o errores).
     */
    @Test
    fun testAdEventsInLogs() {
        // Esperar más tiempo para que se carguen y reproduzcan ads
        Thread.sleep(20000)  // 20 segundos para dar tiempo a que se carguen ads

        // Obtener logs recientes
        val logs = LogcatCapture.getRecentLogs(100)
        
        // Assert: Debe haber logs relacionados con ads
        // Buscar cualquiera de estos eventos:
        // - "AD EVENT:" (eventos de ads)
        // - "AD ERROR:" (errores de ads)
        // - "Ad configuration set" (configuración de ads)
        // - "onAdEvents" (callback de eventos)
        // - "onAdErrorEvent" (callback de errores)
        
        val hasAdLogs = logs.any { log ->
            log.contains("AD EVENT:") || 
            log.contains("AD ERROR:") ||
            log.contains("Ad configuration set") ||
            log.contains("onAdEvents") ||
            log.contains("onAdErrorEvent") ||
            log.contains("LOADED") ||
            log.contains("STARTED") ||
            log.contains("COMPLETED")
        }
        
        // Si no hay logs de ads, mostrar información útil
        if (!hasAdLogs) {
            val recentLogs = logs.takeLast(20).joinToString("\n")
            android.util.Log.w("VideoAdsClientSideActivityTest", 
                "No se encontraron logs de ads. Últimos 20 logs:\n$recentLogs")
        }
        
        // Assert: Los logs deben contener información de ads
        assert(hasAdLogs) {
            "Los logs deben contener eventos o configuración de ads después de 20 segundos.\n" +
            "Últimos logs: ${logs.takeLast(10).joinToString("\n")}"
        }
    }

    /**
     * TEST 6: Cambiar tipo de ad y verificar que se reconfigura.
     * Assert: Al cambiar tipo de ad, se debe recargar el player con nueva configuración.
     */
    @Test
    fun testAdTypeSwitch() {
        // Esperar inicialización
        Thread.sleep(3000)
        
        val initialLogs = LogcatCapture.getRecentLogs(50)
        val initialAdConfigCount = initialLogs.count { it.contains("Ad configuration set") }

        // Cambiar a "Redirect Error"
        onView(withId(R.id.spinnerAdType))
            .perform(click())
        
        // Esperar a que se abra el dropdown y hacer clic en la segunda opción
        Thread.sleep(500)
        onData(org.hamcrest.Matchers.anything())
            .atPosition(1)
            .perform(click())
        
        // Esperar a que se reconfigure
        Thread.sleep(3000)
        
        // Assert: Debe haber una nueva configuración de ad
        val afterLogs = LogcatCapture.getRecentLogs(50)
        val afterAdConfigCount = afterLogs.count { it.contains("Ad configuration set") }
        
        assert(afterAdConfigCount > initialAdConfigCount) {
            "Debe haber una nueva configuración de ad después de cambiar el tipo. " +
            "Antes: $initialAdConfigCount, Después: $afterAdConfigCount"
        }
        
        // Assert: Los logs deben mencionar "Redirect Error"
        val hasRedirectErrorConfig = afterLogs.any { it.contains("Redirect Error") }
        assert(hasRedirectErrorConfig) {
            "Los logs deben mencionar 'Redirect Error' después del cambio"
        }
    }

    /**
     * TEST 7: Verificar que el contenido se reproduce después de ads (si hay).
     * Assert: El player debe eventualmente entrar en estado de reproducción de contenido.
     */
    @Test
    fun testContentPlaybackAfterAds() {
        // Esperar a que se carguen ads y se reproduzca contenido
        Thread.sleep(30000)  // 30 segundos
        
        onView(isRoot())
            .check { view, _ ->
                val frameLayout = view.findViewById<FrameLayout>(R.id.main_media_frame)
                val playerView = findPlayerViewInHierarchy(frameLayout)
                
                // Assert 1: PlayerView debe existir
                assert(playerView != null) {
                    "PlayerView debe existir"
                }
                
                // Assert 2: Player debe tener estado válido (no idle, no error)
                playerView?.player?.let { player ->
                    val isValidState = player.playbackState != androidx.media3.common.Player.STATE_IDLE
                    assert(isValidState) {
                        "El player debe estar en un estado válido después de 30 segundos. " +
                        "Estado actual: ${player.playbackState}"
                    }
                } ?: throw AssertionError("Player no debe ser null")
            }
        
        // Assert 3: Debe haber logs de reproducción de contenido
        val logs = LogcatCapture.getRecentLogs(100)
        val hasPlaybackLogs = logs.any { 
            it.contains("onPlay") || 
            it.contains("onReady") || 
            it.contains("Content playback")
        }
        
        assert(hasPlaybackLogs) {
            "Debe haber logs de reproducción de contenido"
        }
    }

    /**
     * TEST 8: Verificar que no hay crashes durante la prueba de ads.
     * Assert: La actividad debe seguir viva después de tiempo prolongado con ads.
     */
    @Test
    fun testNoAdRelatedCrashes() {
        // Esperar tiempo significativo para exponer posibles crashes
        Thread.sleep(25000)  // 25 segundos
        
        // Assert 1: La actividad debe seguir viva
        onView(isRoot())
            .check(matches(isDisplayed()))
        
        // Assert 2: El player debe seguir existiendo
        onView(isRoot())
            .check { view, _ ->
                val frameLayout = view.findViewById<FrameLayout>(R.id.main_media_frame)
                val playerView = findPlayerViewInHierarchy(frameLayout)
                
                assert(playerView != null) {
                    "PlayerView debe seguir existiendo después de 25 segundos"
                }
                
                assert(playerView?.player != null) {
                    "Player debe seguir existiendo después de 25 segundos"
                }
            }
        
        // Assert 3: No debe haber mensajes de error críticos en logs
        val logs = LogcatCapture.getRecentLogs(100)
        val hasCriticalErrors = logs.any { 
            it.contains("FATAL") || 
            it.contains("java.lang.RuntimeException") ||
            it.contains("NullPointerException")
        }
        
        assert(!hasCriticalErrors) {
            "No debe haber errores críticos durante la reproducción de ads"
        }
    }

    /**
     * Helper function para encontrar PlayerView en la jerarquía de vistas.
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
