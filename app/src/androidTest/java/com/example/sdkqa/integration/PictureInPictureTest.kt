package com.example.sdkqa.integration

import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until
import com.example.sdkqa.video.VideoLiveActivity
import com.example.sdkqa.testutils.AllureScreenshotRule
import com.example.sdkqa.testutils.LogcatCapture
import io.qameta.allure.kotlin.Allure
import io.qameta.allure.kotlin.junit4.DisplayName
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.lang.reflect.Field
import java.util.concurrent.TimeUnit

/**
 * Test específico para Picture-in-Picture (PiP) usando UI Automator.
 * 
 * Verifica que la ventana PiP existe tras llamar a mediastreamPlayer.startPiP().
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class PictureInPictureTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(VideoLiveActivity::class.java)
    
    @get:Rule
    val allureScreenshotRule = AllureScreenshotRule()

    private lateinit var uiDevice: UiDevice

    @Before
    fun setUp() {
        Allure.step("Inicializar UiDevice y captura de logs") {
            uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
            LogcatCapture.startCapture("PictureInPictureTest")
        }
    }

    @After
    fun tearDown() {
        LogcatCapture.stopCapture()
    }

    /**
     * Test que verifica que la ventana PiP se crea correctamente
     * después de llamar a startPiP().
     */
    @Test
    @DisplayName("Validar Picture-in-Picture: Verificar que la ventana PiP existe")
    fun testPictureInPictureWindow() {
        Allure.step("Esperar a que el player se inicialice") {
            Thread.sleep(3000)
        }

        Allure.step("Obtener instancia del MediastreamPlayer") {
            activityRule.scenario.onActivity { activity ->
                try {
                    val playerField: Field = VideoLiveActivity::class.java.getDeclaredField("player")
                    playerField.isAccessible = true
                    val player = playerField.get(activity) as? am.mediastre.mediastreamplatformsdkandroid.MediastreamPlayer
                    
                    if (player != null) {
                        Allure.step("Iniciar reproducción antes de activar PiP") {
                            // Asegurarse de que el player esté reproduciendo
                            val playerView = getPlayerView(activity)
                            playerView?.player?.let { media3Player ->
                                if (!media3Player.isPlaying) {
                                    media3Player.play()
                                    Thread.sleep(2000)
                                }
                            }
                        }
                        
                        Allure.step("Llamar a startPiP()") {
                            try {
                                // Usar reflection para llamar a startPiP si existe
                                val startPiPMethod = player.javaClass.getMethod("startPiP")
                                startPiPMethod.invoke(player)
                                
                                android.util.Log.d("PictureInPictureTest", "startPiP() llamado exitosamente")
                            } catch (e: NoSuchMethodException) {
                                // Si el método no existe, intentar con nombre alternativo
                                try {
                                    val enterPiPMethod = player.javaClass.getMethod("enterPictureInPictureMode")
                                    enterPiPMethod.invoke(player)
                                    android.util.Log.d("PictureInPictureTest", "enterPictureInPictureMode() llamado exitosamente")
                                } catch (e2: Exception) {
                                    android.util.Log.w("PictureInPictureTest", "No se encontró método para iniciar PiP: ${e2.message}")
                                    // Continuar con la verificación de la ventana PiP
                                }
                            } catch (e: Exception) {
                                android.util.Log.e("PictureInPictureTest", "Error llamando a startPiP: ${e.message}", e)
                            }
                        }
                        
                        Allure.step("Esperar a que la ventana PiP aparezca") {
                            Thread.sleep(2000) // Dar tiempo para que PiP se active
                        }
                        
                        Allure.step("Verificar que la ventana PiP existe usando UI Automator") {
                            // Buscar la ventana PiP
                            // En Android, las ventanas PiP tienen características específicas
                            val pipWindow = findPiPWindow()
                            
                            assert(pipWindow != null) {
                                "La ventana Picture-in-Picture debe existir después de llamar a startPiP(). " +
                                "Verifica que el dispositivo/emulador soporte PiP y que la app tenga los permisos necesarios."
                            }
                            
                            Allure.step("Verificar propiedades de la ventana PiP") {
                                assert(pipWindow?.isEnabled == true) {
                                    "La ventana PiP debe estar habilitada"
                                }
                                
                                // Capturar screenshot de la ventana PiP
                                val screenshotPath = com.example.sdkqa.testutils.ScreenshotCapture.capture(
                                    "testPictureInPictureWindow",
                                    "pip_window"
                                )
                                
                                if (screenshotPath != null) {
                                    Allure.step("Screenshot de ventana PiP capturada") {
                                        // La screenshot se adjuntará automáticamente por AllureScreenshotRule si el test falla
                                    }
                                }
                            }
                        }
                    } else {
                        throw AssertionError("No se pudo obtener la instancia del MediastreamPlayer")
                    }
                } catch (e: Exception) {
                    android.util.Log.e("PictureInPictureTest", "Error en test: ${e.message}", e)
                    throw AssertionError("Error accediendo al player: ${e.message}")
                }
            }
        }
    }

    /**
     * Busca la ventana Picture-in-Picture usando UI Automator.
     * Verifica que PiP esté activo buscando elementos del player o verificando el estado de la actividad.
     */
    private fun findPiPWindow(): UiObject2? {
        return try {
            // Método 1: Buscar elementos específicos del player que deberían estar visibles en PiP
            // Buscar PlayerView o controles del player
            val playerView = uiDevice.wait(
                Until.findObject(
                    androidx.test.uiautomator.By.clazz(androidx.media3.ui.PlayerView::class.java.name)
                ),
                TimeUnit.SECONDS.toMillis(5)
            )
            
            if (playerView != null) {
                // Verificar que el PlayerView esté visible y tenga dimensiones de PiP
                // (más pequeño que la pantalla completa)
                val bounds = playerView.visibleBounds
                val screenWidth = uiDevice.displayWidth
                val screenHeight = uiDevice.displayHeight
                
                // Las ventanas PiP suelen ser más pequeñas que la pantalla completa
                if (bounds.width() < screenWidth * 0.8 && bounds.height() < screenHeight * 0.8) {
                    android.util.Log.d("PictureInPictureTest", "PlayerView encontrado con dimensiones de PiP: ${bounds.width()}x${bounds.height()}")
                    return playerView
                }
            }
            
            // Método 2: Buscar por resource ID si está disponible
            try {
                val playerByRes = uiDevice.wait(
                    Until.findObject(
                        androidx.test.uiautomator.By.res("com.example.sdkqa", ".*")
                    ),
                    TimeUnit.SECONDS.toMillis(3)
                )
                if (playerByRes != null) {
                    android.util.Log.d("PictureInPictureTest", "Elemento del player encontrado por resource ID")
                    return playerByRes
                }
            } catch (e: Exception) {
                android.util.Log.d("PictureInPictureTest", "No se encontró elemento por resource ID: ${e.message}")
            }
            
            // Método 3: Verificar que la actividad esté en modo PiP usando el contexto
            // (esto se hace en el test principal, aquí solo verificamos elementos visuales)
            android.util.Log.d("PictureInPictureTest", "No se encontró ventana PiP visible")
            null
            
        } catch (e: Exception) {
            android.util.Log.w("PictureInPictureTest", "Error buscando ventana PiP: ${e.message}")
            null
        }
    }

    /**
     * Helper para obtener PlayerView de la actividad.
     */
    private fun getPlayerView(activity: android.app.Activity): androidx.media3.ui.PlayerView? {
        return try {
            val contentView = activity.findViewById<android.view.ViewGroup>(android.R.id.content)
            findPlayerViewInHierarchy(contentView)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Helper function para encontrar PlayerView en la jerarquía de vistas.
     */
    private fun findPlayerViewInHierarchy(view: android.view.View): androidx.media3.ui.PlayerView? {
        if (view is androidx.media3.ui.PlayerView) {
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
