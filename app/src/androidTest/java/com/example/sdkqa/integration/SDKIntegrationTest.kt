package com.example.sdkqa.integration

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.example.sdkqa.audio.AudioLiveActivity
import com.example.sdkqa.testutils.AllureScreenshotRule
import com.example.sdkqa.testutils.LogcatCapture
import com.example.sdkqa.testutils.PlayerCallbackTracker
import com.example.sdkqa.testutils.TestablePlayerCallback
import io.qameta.allure.kotlin.Allure
import io.qameta.allure.kotlin.junit4.DisplayName
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import android.widget.FrameLayout
import androidx.media3.ui.PlayerView
import java.lang.reflect.Field

/**
 * Suite de pruebas de integración que valida el ciclo de vida básico del SDK Mediastream.
 * 
 * Estas pruebas verifican:
 * 1. Inicialización con MediastreamPlayerConfig
 * 2. Llamada al método play()
 * 3. Verificación de callbacks onPlay() o onError()
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class SDKIntegrationTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(AudioLiveActivity::class.java)
    
    @get:Rule
    val allureScreenshotRule = AllureScreenshotRule()

    private lateinit var testCallback: TestablePlayerCallback

    @Before
    fun setUp() {
        Allure.step("Reiniciar tracker y configurar callback de prueba") {
            PlayerCallbackTracker.reset()
            testCallback = TestablePlayerCallback()
            
            // Inyectar callback de prueba
            activityRule.scenario.onActivity { activity ->
                try {
                    injectTestCallback(activity)
                } catch (e: Exception) {
                    android.util.Log.w("SDKIntegrationTest", "No se pudo inyectar callback: ${e.message}")
                }
            }
            
            // Iniciar captura de logs
            LogcatCapture.startCapture("SDKIntegrationTest")
        }
    }
    
    private fun injectTestCallback(activity: AudioLiveActivity) {
        val playerField: Field = AudioLiveActivity::class.java.getDeclaredField("player")
        playerField.isAccessible = true
        val player = playerField.get(activity)
        
        if (player != null) {
            val addCallbackMethod = player.javaClass.getMethod(
                "addPlayerCallback",
                am.mediastre.mediastreamplatformsdkandroid.MediastreamPlayerCallback::class.java
            )
            addCallbackMethod.invoke(player, testCallback)
        }
    }

    @After
    fun tearDown() {
        LogcatCapture.stopCapture()
        PlayerCallbackTracker.reset()
    }

    /**
     * Test que valida el ciclo de vida básico del SDK:
     * 1. Inicialización con MediastreamPlayerConfig
     * 2. Llamada a play()
     * 3. Verificación de callbacks onPlay() o onError()
     */
    @Test
    @DisplayName("Validar ciclo de vida básico del SDK: Inicialización -> Play -> Callbacks")
    fun testSDKBasicLifecycle() {
        Allure.step("Esperar a que el player se inicialice completamente") {
            Thread.sleep(3000)
        }

        Allure.step("Verificar que el player se inicializó correctamente") {
            onView(isRoot())
                .check { view, _ ->
                    val frameLayout = view as FrameLayout
                    val playerView = findPlayerViewInHierarchy(frameLayout)
                    
                    Allure.step("Verificar que PlayerView existe y tiene player asignado") {
                        assert(playerView != null) {
                            "PlayerView debe existir después de la inicialización"
                        }
                        
                        assert(playerView?.player != null) {
                            "El PlayerView debe tener un player asignado (MediastreamPlayer inicializado)"
                        }
                    }
                }
        }

        Allure.step("Verificar que playerViewReady se haya llamado") {
            assert(PlayerCallbackTracker.waitForPlayerViewReady(10)) {
                "El callback playerViewReady debe haberse ejecutado durante la inicialización"
            }
        }

        Allure.step("Ejecutar play() y verificar callbacks") {
            onView(isRoot())
                .perform(object : androidx.test.espresso.ViewAction {
                    override fun getConstraints(): org.hamcrest.Matcher<android.view.View> {
                        return isRoot()
                    }

                    override fun getDescription(): String {
                        return "Ejecutar play() y verificar callbacks"
                    }

                    override fun perform(
                        uiController: androidx.test.espresso.UiController,
                        view: android.view.View
                    ) {
                        val frameLayout = view as FrameLayout
                        val playerView = findPlayerViewInHierarchy(frameLayout)
                        
                        playerView?.player?.let { player ->
                            val initialPlayCount = PlayerCallbackTracker.getPlayCallCount()
                            val initialErrorCount = PlayerCallbackTracker.getErrorCallCount()
                            val wasAlreadyPlaying = player.isPlaying
                            
                            Allure.step("Llamar a player.play()") {
                                if (!wasAlreadyPlaying) {
                                    player.play()
                                }
                            }
                            
                            Allure.step("Esperar confirmación de reproducción o error") {
                                val timeoutMs = 10_000L
                                val pollMs = 250L
                                val deadline = System.currentTimeMillis() + timeoutMs

                                // Caso 1: ya estaba reproduciendo antes de que el test intente play()
                                if (wasAlreadyPlaying) return@step

                                // Caso 2: arranca después de play() -> esperar señal (estado o callback)
                                while (System.currentTimeMillis() < deadline) {
                                    val playCountAfter = PlayerCallbackTracker.getPlayCallCount()
                                    val errorCountAfter = PlayerCallbackTracker.getErrorCallCount()

                                    val nowPlaying = player.isPlaying || PlayerCallbackTracker.isPlaying()
                                    val playCalled = playCountAfter > initialPlayCount
                                    val errorCalled = errorCountAfter > initialErrorCount

                                    if (nowPlaying || playCalled || errorCalled) {
                                        if (errorCalled) {
                                            Allure.step("onError() se ejecutó - verificar logs") {
                                                val errorLogs = LogcatCapture.getErrorLogs()
                                                assert(errorLogs.isNotEmpty()) {
                                                    "Debe haber logs de error disponibles"
                                                }
                                            }
                                        }
                                        return@step
                                    }

                                    uiController.loopMainThreadForAtLeast(pollMs)
                                }

                                val playCountAfter = PlayerCallbackTracker.getPlayCallCount()
                                val errorCountAfter = PlayerCallbackTracker.getErrorCallCount()
                                throw AssertionError(
                                    "Debe haberse confirmado reproducción o error después de play(). " +
                                        "wasAlreadyPlaying: $wasAlreadyPlaying, " +
                                        "isPlaying: ${player.isPlaying}, " +
                                        "playCount: $playCountAfter (initial: $initialPlayCount), " +
                                        "errorCount: $errorCountAfter (initial: $initialErrorCount)"
                                )
                            }
                        } ?: throw AssertionError("Player no debe ser null")
                    }
                })
        }
    }

    /**
     * Test que valida la inicialización del MediastreamPlayerConfig.
     */
    @Test
    @DisplayName("Validar inicialización de MediastreamPlayerConfig")
    fun testMediastreamPlayerConfigInitialization() {
        Allure.step("Esperar a que la actividad se inicialice") {
            Thread.sleep(3000)
        }

        Allure.step("Verificar que el player se creó con la configuración correcta") {
            onView(isRoot())
                .check { view, _ ->
                    val frameLayout = view as FrameLayout
                    val playerView = findPlayerViewInHierarchy(frameLayout)
                    
                    assert(playerView != null) {
                        "PlayerView debe existir"
                    }
                    
                    assert(playerView?.player != null) {
                        "El player debe estar inicializado"
                    }
                    
                    // Verificar que el player está en un estado válido
                    val player = playerView?.player
                    assert(
                        player?.playbackState == androidx.media3.common.Player.STATE_IDLE ||
                        player?.playbackState == androidx.media3.common.Player.STATE_BUFFERING ||
                        player?.playbackState == androidx.media3.common.Player.STATE_READY
                    ) {
                        "El player debe estar en un estado válido. Estado actual: ${player?.playbackState}"
                    }
                }
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
